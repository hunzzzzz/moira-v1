package com.hunzz.userserver.service

import com.hunzz.common.domain.user.model.CachedUser
import com.hunzz.common.domain.user.model.User
import com.hunzz.common.domain.user.model.UserRole
import com.hunzz.common.domain.user.repository.UserRepository
import com.hunzz.common.global.exception.ErrorCode.DIFFERENT_TWO_PASSWORDS
import com.hunzz.common.global.exception.ErrorCode.USER_NOT_FOUND
import com.hunzz.common.global.exception.custom.InvalidUserInfoException
import com.hunzz.common.global.utility.KafkaProducer
import com.hunzz.common.global.utility.PasswordEncoder
import com.hunzz.userserver.dto.request.SignUpRequest
import com.hunzz.userserver.dto.response.UserResponse
import com.hunzz.userserver.utility.ImageHandler
import com.hunzz.userserver.utility.UserCacheManager
import com.hunzz.userserver.utility.UserRedisHandler
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.util.*

@Component
class UserHandler(
    private val imageHandler: ImageHandler,
    private val kafkaProducer: KafkaProducer,
    private val passwordEncoder: PasswordEncoder,
    private val userCacheManager: UserCacheManager,
    private val userRedisHandler: UserRedisHandler,
    private val userRepository: UserRepository
) {
    @Transactional
    fun save(request: SignUpRequest): UUID {
        // validate
        if (request.password != request.password2)
            throw InvalidUserInfoException(DIFFERENT_TWO_PASSWORDS)
        userRedisHandler.validateSignupRequest(inputEmail = request.email!!, inputAdminCode = request.adminCode)

        // save (db)
        val user =
            userRepository.save(
                User(
                    role = if (request.adminCode != null) UserRole.ADMIN else UserRole.USER,
                    email = request.email!!,
                    password = passwordEncoder.encodePassword(rawPassword = request.password!!),
                    name = request.name!!
                )
            )

        // send kafka message (add additional info in redis)
        kafkaProducer.send(topic = "signup", data = user)

        return user.id!!
    }

    private fun get(userId: UUID): User {
        val user = userRepository.findByIdOrNull(id = userId)
            ?: throw InvalidUserInfoException(USER_NOT_FOUND)

        return user
    }

    fun getProfile(userId: UUID, targetId: UUID): UserResponse {
        val user = userCacheManager.getWithLocalCache(userId = targetId)
        val relationInfo = userRedisHandler.getRelationInfo(userId = targetId)

        return UserResponse(
            id = user.userId,
            status = user.status,
            name = user.name,
            imageUrl = user.imageUrl,
            thumbnailUrl = user.thumbnailUrl,
            numOfFollowings = relationInfo.numOfFollowings,
            numOfFollowers = relationInfo.numOfFollowers,
            isMyProfile = userId == targetId
        )
    }

    @Transactional
    fun uploadImage(userId: UUID, image: MultipartFile) {
        // send request (to image-server)
        val imageInfos = imageHandler.sendRequest(image = image)

        // update
        val user = get(userId = userId)
        user.updateImage(imageInfos.imageUrl, imageInfos.thumbnailUrl)

        // send kafka message (re-add cache)
        val newUserCache = CachedUser(
            userId = userId,
            status = user.status,
            name = user.name,
            imageUrl = imageInfos.imageUrl,
            thumbnailUrl = imageInfos.thumbnailUrl
        )
        kafkaProducer.send(topic = "upload-image", data = newUserCache)
    }
}