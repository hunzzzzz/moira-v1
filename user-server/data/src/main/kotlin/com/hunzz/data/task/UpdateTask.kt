package com.hunzz.data.task

import com.fasterxml.jackson.databind.ObjectMapper
import com.hunzz.common.exception.ErrorCode.USER_NOT_FOUND
import com.hunzz.common.exception.custom.InvalidUserInfoException
import com.hunzz.common.kafka.dto.KafkaUpdateUserImageUrlsRequest
import com.hunzz.common.repository.UserRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class UpdateTask(
    private val objectMapper: ObjectMapper,
    private val userRepository: UserRepository,
) {
    @KafkaListener(topics = ["update-user-image-urls"], groupId = "update-user-image-urls")
    @Transactional
    fun updateUserImageUrls(message: String) {
        val data = objectMapper.readValue(message, KafkaUpdateUserImageUrlsRequest::class.java)

        val user = userRepository.findByIdOrNull(id = data.userId)
            ?: throw InvalidUserInfoException(USER_NOT_FOUND)

        // DB에 유저 이미지 url 업데이트
        user.updateImageUrls(imageUrl = data.originalUrl, thumbnailUrl = data.thumbnailUrl)
    }
}