package com.hunzz.api.component

import com.hunzz.api.dto.request.SignUpRequest
import com.hunzz.common.kafka.KafkaProducer
import com.hunzz.common.kafka.dto.SignupKafkaRequest
import com.hunzz.common.kafka.dto.SocialSignupKafkaRequest
import org.springframework.stereotype.Component
import java.util.*

@Component
class UserKafkaHandler(
    private val kafkaProducer: KafkaProducer,
    private val passwordEncoder: PasswordEncoder
) {
    fun signup(userId: UUID, request: SignUpRequest) {
        val data = SignupKafkaRequest(
            userId = userId,
            email = request.email!!,
            password = passwordEncoder.encodePassword(rawPassword = request.password!!),
            name = request.name!!,
            adminCode = request.adminCode
        )
        kafkaProducer.send(topic = "save-user-in-db", data = data)
    }

    fun kakaoSignup(request: SocialSignupKafkaRequest) {
        kafkaProducer.send(topic = "save-kakao-user-in-db", data = request)
    }

    fun naverSignup(request: SocialSignupKafkaRequest) {
        kafkaProducer.send(topic = "save-naver-user-in-db", data = request)
    }
}