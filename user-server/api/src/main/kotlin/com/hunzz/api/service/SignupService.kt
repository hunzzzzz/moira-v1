package com.hunzz.api.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.hunzz.api.component.UserKafkaHandler
import com.hunzz.api.component.UserRedisHandler
import com.hunzz.api.dto.request.SignUpRequest
import com.hunzz.common.kafka.dto.SocialSignupKafkaRequest
import com.hunzz.common.model.property.UserType
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service
import java.util.*

@Service
class SignupService(
    private val objectMapper: ObjectMapper,
    private val userKafkaHandler: UserKafkaHandler,
    private val userRedisHandler: UserRedisHandler
) {
    fun signup(request: SignUpRequest): UUID {
        // 검증
        userRedisHandler.validateSignupRequest(request = request)

        // Redis에 유저 정보 저장
        val userId = UUID.randomUUID()
        userRedisHandler.signup(userId = userId, request = request)

        // Kafka 메시지 전송 (user-api -> user-consumer)
        userKafkaHandler.signup(userId = userId, request = request)

        return userId
    }

    @KafkaListener(topics = ["kakao-signup"], groupId = "kakao-signup")
    fun kakaoSignup(message: String) {
        val request = objectMapper.readValue(message, SocialSignupKafkaRequest::class.java)

        // Redis에 유저 정보 저장
        userRedisHandler.socialUserSignup(request = request, type = UserType.KAKAO)

        // Kafka 메시지 전송 (user-api -> user-consumer)
        userKafkaHandler.kakaoSignup(request = request)
    }

    @KafkaListener(topics = ["naver-signup"], groupId = "naver-signup")
    fun naverSignup(message: String) {
        val request = objectMapper.readValue(message, SocialSignupKafkaRequest::class.java)

        // Redis에 유저 정보 저장
        userRedisHandler.socialUserSignup(request = request, type = UserType.NAVER)

        // Kafka 메시지 전송 (user-api -> user-consumer)
        userKafkaHandler.naverSignup(request = request)
    }
}