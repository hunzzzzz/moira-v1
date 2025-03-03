package com.hunzz.api.component

import com.hunzz.api.dto.request.SignUpRequest
import com.hunzz.common.kafka.KafkaProducer
import com.hunzz.common.kafka.dto.*
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.util.*

@Component
class UserKafkaHandler(
    private val kafkaProducer: KafkaProducer,
    private val passwordEncoder: PasswordEncoder
) {
    fun signup(userId: UUID, request: SignUpRequest) {
        val data = KafkaSignupRequest(
            userId = userId,
            email = request.email!!,
            password = passwordEncoder.encodePassword(rawPassword = request.password!!),
            name = request.name!!,
            adminCode = request.adminCode
        )
        kafkaProducer.send(topic = "save-user-in-db", data = data)
    }

    fun kakaoSignup(request: KafkaSocialSignupRequest) {
        kafkaProducer.send(topic = "save-kakao-user-in-db", data = request)
    }

    fun naverSignup(request: KafkaSocialSignupRequest) {
        kafkaProducer.send(topic = "save-naver-user-in-db", data = request)
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