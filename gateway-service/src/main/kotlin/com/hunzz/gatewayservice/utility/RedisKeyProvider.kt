package com.hunzz.gatewayservice.utility

import org.springframework.stereotype.Component

@Component
class RedisKeyProvider {
    fun blockedAtk(atk: String) = "blocked_atk::$atk"
}