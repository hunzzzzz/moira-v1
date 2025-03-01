package com.hunzz.userserver.service

import com.hunzz.common.domain.user.model.CachedUser
import com.hunzz.common.domain.user.model.entity.User
import com.hunzz.common.domain.user.model.property.UserRole
import com.hunzz.common.domain.user.repository.UserRepository
import com.hunzz.common.global.exception.ErrorCode.DIFFERENT_TWO_PASSWORDS
import com.hunzz.common.global.exception.ErrorCode.USER_NOT_FOUND
import com.hunzz.common.global.exception.custom.InvalidUserInfoException
import com.hunzz.common.global.utility.KafkaProducer
import com.hunzz.common.global.utility.PasswordEncoder
import com.hunzz.userserver.dto.request.KafkaImageRequest
import com.hunzz.userserver.dto.request.SignUpRequest
import com.hunzz.userserver.dto.response.UserResponse
import com.hunzz.userserver.utility.UserCacheManager
import com.hunzz.userserver.utility.UserRedisHandler
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.util.*

@Component
class UserHandler(
    @Value("\${cloud.aws.s3.bucketName}")
    private val bucketName: String,

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

    private fun get(userId: UUID): User {
        val user = userRepository.findByIdOrNull(id = userId)
            ?: throw InvalidUserInfoException(USER_NOT_FOUND)

        return user
    }

    private fun getImageFileNames(): Pair<String, String> {
        val imageId = UUID.randomUUID()

        val originalFileName = "${imageId}.jpg"
        val thumbnailFileName = "${imageId}-thumbnail.jpg"

        return Pair(originalFileName, thumbnailFileName)
    }

    private fun getImageUrl(fileName: String): String {
        return "https://${bucketName}.s3.amazonaws.com/$fileName"
    }

    @Transactional
    fun uploadImage(userId: UUID, image: MultipartFile) {
        // send kafka message (to image server / save image)
        val (originalFileName, thumbnailFileName) = getImageFileNames()
        val data = KafkaImageRequest(
            originalFileName = originalFileName,
            thumbnailFileName = thumbnailFileName,
            image = image.bytes
        )
        kafkaProducer.send(topic = "add-image", data)

        // update
        val originalUrl = getImageUrl(fileName = originalFileName)
        val thumbnailUrl = getImageUrl(fileName = thumbnailFileName)

        val user = get(userId = userId)
        user.updateImage(imageUrl = originalUrl, thumbnailUrl = thumbnailUrl)

        // send kafka message (re-add cache)
        val newUserCache = CachedUser(
            userId = userId,
            status = user.status,
            name = user.name,
            imageUrl = originalUrl,
            thumbnailUrl = thumbnailUrl
        )
        kafkaProducer.send(topic = "re-add-user-cache", data = newUserCache)
    }
}