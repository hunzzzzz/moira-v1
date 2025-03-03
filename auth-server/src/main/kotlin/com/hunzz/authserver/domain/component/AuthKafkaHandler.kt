package com.hunzz.authserver.domain.component

import com.hunzz.authserver.utility.kafka.KafkaProducer
import com.hunzz.authserver.utility.kafka.dto.SignupKafkaRequest
import org.springframework.stereotype.Component
import java.util.*

@Component
class AuthKafkaHandler(
    private val kafkaProducer: KafkaProducer
) {
    fun kakaoUserSignup(email: String, name: String): UUID {
        val userId = UUID.randomUUID()

        val data = SignupKafkaRequest(
            userId = userId,
            email = email,
            name = name
        )
        kafkaProducer.send(topic = "kakao-signup", data = data)

        return userId
    }

    fun naverUserSignup(email: String, name: String): UUID {
        val userId = UUID.randomUUID()

        val data = SignupKafkaRequest(
            userId = userId,
            email = email,
            name = name
        )
        kafkaProducer.send(topic = "naver-signup", data = data)

        return userId
    }
}