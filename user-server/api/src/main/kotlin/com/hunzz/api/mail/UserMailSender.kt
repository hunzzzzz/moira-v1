package com.hunzz.api.mail

import com.hunzz.common.exception.ErrorCode.MAIL_SYSTEM_ERROR
import com.hunzz.common.exception.custom.InternalSystemException
import org.slf4j.LoggerFactory
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class UserMailSender(
    private val mailSender: JavaMailSender
) {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private fun getSubjectOfSignupCode() = "[Moira] 본인 인증을 위한 메일입니다."
    private fun getTextOfSignupCode(code: String) = "다음 인증 코드 [${code}]를 웹 화면에 입력하면 인증이 완료됩니다."

    @Async
    fun sendSignupCode(email: String, code: String) {
        try {
            return mailSender.createMimeMessage().apply {
                MimeMessageHelper(this, false, "UTF-8").apply {
                    setTo(email)
                    setSubject(getSubjectOfSignupCode())
                    setText(getTextOfSignupCode(code = code))
                }
            }.let(mailSender::send)
        } catch (e: Exception) {
            logger.error("이메일 전송 실패.")
            throw InternalSystemException(MAIL_SYSTEM_ERROR)
        }
    }
}