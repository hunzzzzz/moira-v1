package com.hunzz.common.redis

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

    // relation
    fun following(userId: UUID) = "following:$userId"
    fun follower(userId: UUID) = "follower:$userId"
}