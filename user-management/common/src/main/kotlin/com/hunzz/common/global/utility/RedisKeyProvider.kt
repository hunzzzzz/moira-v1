package com.hunzz.common.global.utility

import org.springframework.stereotype.Component
import java.util.*

@Component
class RedisKeyProvider {
    // admin
    fun adminCode() = "admin_signup_code"
    fun bannedUsers() = "banned_users"

    // auth
    fun blockedAtk(atk: String) = "blocked_atk:$atk"
    fun rtk(email: String) = "rtk:$email"

    // user
    fun auth(email: String) = "auth:$email"
    fun emails() = "emails"
    fun ids() = "ids"
    fun user(userId: UUID) = "user:$userId"
}