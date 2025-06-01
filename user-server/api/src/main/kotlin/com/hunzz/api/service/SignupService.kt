package com.hunzz.api.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.hunzz.api.component.UserKafkaHandler
import com.hunzz.api.component.UserRedisHandler
import com.hunzz.api.dto.request.SignUpRequest
import com.hunzz.api.mail.UserMailSender
import com.hunzz.common.exception.ErrorCode.*
import com.hunzz.common.exception.custom.InvalidSignupException
import com.hunzz.common.kafka.dto.KafkaSocialSignupRequest
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service

@Service
class SignupService(
    private val objectMapper: ObjectMapper,
    private val userKafkaHandler: UserKafkaHandler,
    private val userMailSender: UserMailSender,
    private val userRedisHandler: UserRedisHandler
) {
    fun sendSignupCode(email: String) {
        // 이메일 중복 여부 확인
        userRedisHandler.checkEmailDuplication(email = email)

        // 본인인증 코드 생성 및 전송
        val signupCode = userRedisHandler.setSignupCode(email = email)
        userMailSender.sendSignupCode(email = email, code = signupCode)
    }

    fun checkSignupCode(email: String, code: String) {
        // Redis에서 본인인증 코드 가져오기
        val signupCode = userRedisHandler.getSignupCode(email = email)
            ?: throw InvalidSignupException(EXPIRED_SIGNUP_CODE)

        // 본인인증 코드 일치 여부 확인
        if (signupCode != code)
            throw InvalidSignupException(INVALID_SIGNUP_CODE)
    }

    fun signup(request: SignUpRequest) {
        // 비밀번호 일치 여부 확인
        if (request.password != request.password2)
            throw InvalidSignupException(DIFFERENT_TWO_PASSWORDS)

        // Redis에 이메일 저장 (Set)
        userRedisHandler.addEmailIntoRedisSet(email = request.email!!)

        // Kafka 메시지 전송 (user-api -> user-data)
        userKafkaHandler.saveUser(request = request)
    }

    @KafkaListener(topics = ["kakao-signup"], groupId = "kakao-signup")
    fun kakaoSignup(message: String) {
        val data = objectMapper.readValue(message, KafkaSocialSignupRequest::class.java)

        // Redis에 이메일 저장 (Set)
        userRedisHandler.addEmailIntoRedisSet(email = data.email)

        // Kafka 메시지 전송 (user-api -> user-data)
        userKafkaHandler.kakaoSignup(request = data)
    }

    @KafkaListener(topics = ["naver-signup"], groupId = "naver-signup")
    fun naverSignup(message: String) {
        val data = objectMapper.readValue(message, KafkaSocialSignupRequest::class.java)

        // Redis에 이메일 저장 (Set)
        userRedisHandler.addEmailIntoRedisSet(email = data.email)

        // Kafka 메시지 전송 (user-api -> user-data)
        userKafkaHandler.naverSignup(request = data)
    }
}