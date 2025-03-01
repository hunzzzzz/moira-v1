package com.hunzz.authserver.domain.component

import com.hunzz.authserver.utility.kafka.KafkaProducer
import com.hunzz.authserver.utility.kafka.dto.KakaoSignupKafkaRequest
import org.springframework.context.annotation.Description
import org.springframework.stereotype.Component
import java.util.*

@Component
class AuthKafkaHandler(
    private val kafkaProducer: KafkaProducer
) {
    @Description("auth-server -> user-server")
    fun kakaoUserSignup(email: String, name: String): UUID {
        // auth-server에서 userId를 먼저 만들어서 전달 (이유: 토큰 생성 시 userId가 필요)
        val userId = UUID.randomUUID()

        // Kafka 메시지 전송
        val data = KakaoSignupKafkaRequest(
            userId = userId,
            email = email,
            name = name
        )
        kafkaProducer.send(topic = "kakao-signup", data = data)

        return userId
    }
}