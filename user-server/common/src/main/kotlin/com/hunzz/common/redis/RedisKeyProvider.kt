package com.hunzz.common.redis

import org.springframework.stereotype.Component
import java.util.*

@Component
class RedisKeyProvider {
    // admin code
    fun adminCode() = "admin_code"

    // signup
    fun signupCode(email: String) = "signup:$email"

    // user
    fun auth(email: String) = "auth:$email"
    fun emails() = "emails"
    fun user(userId: UUID) = "user:$userId"

    // relation
    fun following(userId: UUID) = "following:$userId"
    fun follower(userId: UUID) = "follower:$userId"
}