package com.hunzz.userserver.domain.user.service

import com.hunzz.common.domain.user.model.CachedUser
import com.hunzz.common.domain.user.model.User
import com.hunzz.common.domain.user.model.UserRole
import com.hunzz.common.domain.user.repository.UserRepository
import com.hunzz.common.global.exception.ErrorCode.*
import com.hunzz.common.global.exception.custom.InternalSystemException
import com.hunzz.common.global.exception.custom.InvalidUserInfoException
import com.hunzz.common.global.utility.KafkaProducer
import com.hunzz.common.global.utility.PasswordEncoder
import com.hunzz.userserver.domain.user.dto.request.SignUpRequest
import com.hunzz.userserver.domain.user.dto.response.UserImageResponse
import com.hunzz.userserver.domain.user.dto.response.UserResponse
import com.hunzz.userserver.global.utility.MultipartFileResource
import org.springframework.aop.framework.AopContext
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate
import org.springframework.web.multipart.MultipartFile
import java.util.*

@Component
class UserHandler(
    private val kafkaProducer: KafkaProducer,
    private val passwordEncoder: PasswordEncoder,
    private val userRedisHandler: UserRedisHandler,
    private val userRepository: UserRepository
) {
    private fun proxy() = AopContext.currentProxy() as UserHandler

    @Transactional
    fun save(request: SignUpRequest): UUID {
        // validate
        if (request.password != request.password2)
            throw InvalidUserInfoException(DIFFERENT_TWO_PASSWORDS)
        userRedisHandler.checkSignupRequest(inputEmail = request.email!!, inputAdminCode = request.adminCode)

        // encrypt
        val encodedPassword = passwordEncoder.encodePassword(rawPassword = request.password!!)

        // save (db)
        val user =
            userRepository.save(
                User(
                    role = if (request.adminCode != null) UserRole.ADMIN else UserRole.USER,
                    email = request.email!!,
                    password = encodedPassword,
                    name = request.name!!
                )
            )

        // send kafka message (redis command)
        kafkaProducer.send(topic = "signup", data = user)

        return user.id!!
    }

    fun getProfile(userId: UUID, targetId: UUID): UserResponse {
        val user = proxy().getWithLocalCache(userId = targetId)
        val relationInfo = userRedisHandler.getRelationInfo(userId = targetId)

        return UserResponse(
            id = user.userId,
            status = user.status,
            name = user.name,
            imageUrl = user.thumbnailUrl,
            numOfFollowings = relationInfo.numOfFollowings,
            numOfFollowers = relationInfo.numOfFollowers,
            isMyProfile = userId == targetId
        )
    }

    fun getEntity(userId: UUID): User {
        val user = userRepository.findByIdOrNull(id = userId)
            ?: throw InvalidUserInfoException(USER_NOT_FOUND)

        return user
    }

    fun get(userId: UUID): CachedUser {
        val user = userRepository.findUserProfile(userId = userId)
            ?: throw InvalidUserInfoException(USER_NOT_FOUND)

        return CachedUser(
            userId = userId,
            status = user.status,
            name = user.name,
            imageUrl = user.imageUrl,
            thumbnailUrl = user.thumbnailUrl
        )
    }

    fun getWithRedisCache(userId: UUID): CachedUser {
        val userCache = userRedisHandler.getUserCache(userId = userId)

        return if (userCache == null) {
            val cachedUser = get(userId = userId)
            userRedisHandler.setUserCache(userId = userId, cachedUser = cachedUser)

            cachedUser
        } else userCache
    }

    @Cacheable(cacheNames = ["user"], key = "#userId", cacheManager = "localCacheManager")
    fun getWithLocalCache(userId: UUID): CachedUser {
        return getWithRedisCache(userId = userId)
    }

    fun getAll(missingIds: List<UUID>): HashMap<UUID, CachedUser> {
        val hashMap = hashMapOf<UUID, CachedUser>()

        missingIds.forEach {
            hashMap[it] = proxy().getWithLocalCache(userId = it)
        }

        return hashMap
    }

    @Transactional
    fun uploadImage(userId: UUID, image: MultipartFile) {
        // validate
        if (image.isEmpty || image.originalFilename.isNullOrBlank()) {
            throw InvalidUserInfoException(INVALID_IMAGE_FILE)
        }

        val validExtensions = listOf("png", "jpg", "jpeg", "svg")
        val indexOfDot = image.originalFilename!!.lastIndexOf('.')
        val extension = image.originalFilename!!.substring(indexOfDot + 1)

        if (!validExtensions.contains(extension)) {
            throw InvalidUserInfoException(INVALID_IMAGE_EXTENSION)
        }

        // settings
        val headers = HttpHeaders().apply {
            contentType = MediaType.MULTIPART_FORM_DATA
        }
        val body = LinkedMultiValueMap<String, Any>().apply {
            add("image", MultipartFileResource(file = image))
        }
        val requestEntity = HttpEntity(body, headers)

        // send request (to image-server)
        val response = RestTemplate().postForObject(
            "http://localhost:8080/image-server/images", // TODO: 추후 수정
            requestEntity,
            UserImageResponse::class.java
        ) ?: throw InternalSystemException(IMAGE_SYSTEM_ERROR)

        // update
        val user = getEntity(userId = userId)
        user.updateImage(response.imageUrl, response.thumbnailUrl)

        // re-insert cache
        TODO()
    }
}