package com.hunzz.consumer.task

import com.fasterxml.jackson.databind.ObjectMapper
import com.hunzz.common.kafka.dto.KafkaSignupRequest
import com.hunzz.common.kafka.dto.KafkaSocialSignupRequest
import com.hunzz.common.model.entity.User
import com.hunzz.common.model.property.UserRole
import com.hunzz.common.model.property.UserType
import com.hunzz.common.repository.UserRepository
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class SignupTask(
    private val objectMapper: ObjectMapper,
    private val userRepository: UserRepository
) {
    @KafkaListener(topics = ["save-user-in-db"], groupId = "save-user-in-db")
    @Transactional
    fun saveUser(message: String) {
        val data = objectMapper.readValue(message, KafkaSignupRequest::class.java)

        // DB에 유저 정보 저장
        userRepository.save(
            User(
                id = data.userId,
                type = UserType.NORMAL,
                email = data.email,
                role = if (data.adminCode != null) UserRole.ADMIN else UserRole.USER,
                name = data.name,
                password = data.password,
            )
        )
    }

    @KafkaListener(topics = ["save-kakao-user-in-db"], groupId = "save-kakao-user-in-db")
    @Transactional
    fun saveKakaoUser(message: String) {
        val data = objectMapper.readValue(message, KafkaSocialSignupRequest::class.java)

        // DB에 카카오 유저 정보 저장
        userRepository.save(
            User(
                id = data.userId,
                type = UserType.KAKAO,
                email = data.email,
                password = null,
                name = data.name
            )
        )
    }

    @KafkaListener(topics = ["save-naver-user-in-db"], groupId = "save-naver-user-in-db")
    @Transactional
    fun saveNaverUser(message: String) {
        val data = objectMapper.readValue(message, KafkaSocialSignupRequest::class.java)

        // DB에 네이버 유저 정보 저장
        userRepository.save(
            User(
                id = data.userId,
                type = UserType.NAVER,
                email = data.email,
                password = null,
                name = data.name
            )
        )
    }
}