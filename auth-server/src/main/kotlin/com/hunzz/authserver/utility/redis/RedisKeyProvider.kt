package com.hunzz.authserver.utility.redis

import org.springframework.stereotype.Component

@Component
class RedisKeyProvider {
    fun auth(email: String) = "auth:$email"
    fun blockedAtk(atk: String) = "blocked_atk:$atk"
    fun emails() = "emails"
    fun rtk(email: String) = "rtk:$email"
}