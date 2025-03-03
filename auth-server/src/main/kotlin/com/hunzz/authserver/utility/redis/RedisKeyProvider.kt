package com.hunzz.authserver.utility.redis

import org.springframework.stereotype.Component
import java.util.*

@Component
class RedisKeyProvider {
    // user
    fun emails() = "emails"
    fun user(userId: UUID) = "user:$userId"

    // auth
    fun auth(email: String) = "auth:$email"
    fun blockedAtk(atk: String) = "blocked_atk:$atk"
    fun rtk(email: String) = "rtk:$email"
}