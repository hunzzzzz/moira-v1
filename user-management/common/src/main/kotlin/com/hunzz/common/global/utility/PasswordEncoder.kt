package com.hunzz.common.global.utility

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.security.MessageDigest
import java.util.*

@Component
class PasswordEncoder(
    @Value("\${encrypt.password.salt}")
    val salt: String
) {
    fun encodePassword(rawPassword: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashedPassword = digest.digest((rawPassword + salt).toByteArray(charset("UTF-8")))

        return Base64.getEncoder().encodeToString(hashedPassword)
    }

    fun matches(rawPassword: String, encodedPassword: String?): Boolean {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashedPassword = digest.digest((rawPassword + salt).toByteArray(charset("UTF-8")))

        return MessageDigest.isEqual(hashedPassword, Base64.getDecoder().decode(encodedPassword))
    }
}