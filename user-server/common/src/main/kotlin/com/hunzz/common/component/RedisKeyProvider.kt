package com.hunzz.common.component

import org.springframework.stereotype.Component
import java.util.*

@Component
class RedisKeyProvider {
    // admin
    fun adminCode() = "admin_signup_code"

    // user
    fun auth(email: String) = "auth:$email"
    fun emails() = "emails"
    fun user(userId: UUID) = "user:$userId"
}