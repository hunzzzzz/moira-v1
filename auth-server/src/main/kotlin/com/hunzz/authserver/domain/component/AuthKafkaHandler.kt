package com.hunzz.authserver.domain.component

import com.hunzz.authserver.utility.kafka.KafkaProducer
import com.hunzz.authserver.utility.kafka.dto.SignupKafkaRequest
import org.springframework.context.annotation.Description
import org.springframework.stereotype.Component
import java.util.*

@Component
class AuthKafkaHandler(
    private val kafkaProducer: KafkaProducer
) {
    @Description("auth-server -> user-consumer")
    fun kakaoUserSignup(email: String, name: String): UUID {
        val userId = UUID.randomUUID()

        // Kafka 메시지 전송
        val data = SignupKafkaRequest(
            userId = userId,
            email = email,
            name = name
        )
        kafkaProducer.send(topic = "kakao-signup", data = data)

        return userId
    }

    @Description("auth-server -> user-consumer")
    fun naverUserSignup(email: String, name: String): UUID {
        val userId = UUID.randomUUID()

        // Kafka 메시지 전송
        val data = SignupKafkaRequest(
            userId = userId,
            email = email,
            name = name
        )
        kafkaProducer.send(topic = "naver-signup", data = data)

        return userId
    }
}