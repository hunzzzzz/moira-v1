package com.hunzz.api.component

import com.hunzz.api.dto.request.SignUpRequest
import com.hunzz.common.kafka.KafkaProducer
import com.hunzz.common.kafka.dto.*
import org.springframework.context.annotation.Description
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.util.*

@Component
class UserKafkaHandler(
    private val kafkaProducer: KafkaProducer,
    private val passwordEncoder: PasswordEncoder
) {
    @Description("user-api -> user-data")
    fun saveUser(request: SignUpRequest) {
        val data = KafkaSignupRequest(
            email = request.email!!,
            password = passwordEncoder.encodePassword(rawPassword = request.password!!),
            name = request.name!!
        )
        kafkaProducer.send(topic = "save-user", data = data)
    }

    fun kakaoSignup(request: KafkaSocialSignupRequest) {
        kafkaProducer.send(topic = "save-kakao-user", data = request)
    }

    fun naverSignup(request: KafkaSocialSignupRequest) {
        kafkaProducer.send(topic = "save-naver-user", data = request)
    }

    fun uploadImage(originalFileName: String, thumbnailFileName: String, image: MultipartFile) {
        val data = KafkaImageUploadRequest(
            originalFileName = originalFileName,
            thumbnailFileName = thumbnailFileName,
            image = image.bytes
        )
        kafkaProducer.send(topic = "upload-image", data)
    }

    fun updateUserImageUrls(userId: UUID, originalUrl: String, thumbnailUrl: String) {
        val data = KafkaUpdateUserImageUrlsRequest(
            userId = userId,
            originalUrl = originalUrl,
            thumbnailUrl = thumbnailUrl
        )
        kafkaProducer.send(topic = "update-user-image-urls", data = data)
    }

    fun reAddUserCache(userId: UUID) {
        val data = KafkaAddUserCacheRequest(
            userId = userId
        )
        kafkaProducer.send(topic = "re-add-user-cache", data = data)
    }
}