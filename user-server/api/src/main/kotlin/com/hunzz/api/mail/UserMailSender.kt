package com.hunzz.api.mail

import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class UserMailSender(
    private val mailSender: JavaMailSender
) {
    private fun getSubjectOfSignupCode() = "[Moira] 본인 인증을 위한 메일입니다."
    private fun getTextOfSignupCode(code: String) = "다음 인증 코드 [${code}]를 웹 화면에 입력하면 인증이 완료됩니다."

    @Async
    fun sendSignupCode(email: String, code: String): Result<Unit> {
        return kotlin.runCatching {
            mailSender.createMimeMessage().apply {
                MimeMessageHelper(this, false, "UTF-8").apply {
                    setTo(email)
                    setSubject(getSubjectOfSignupCode())
                    setText(getTextOfSignupCode(code = code))
                }
            }.let(mailSender::send)
        }
    }
}