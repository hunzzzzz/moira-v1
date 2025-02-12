package com.hunzz.common.global.utility

import org.springframework.stereotype.Component

@Component
class RedisKeyProvider {
    // admin
    fun adminCode() = "admin_signup_code"
    fun bannedUsers() = "banned_users"
    fun rtk(email: String) = "rtk::$email"

    // user
    fun auth(email: String) = "auth:$email"
    fun emails() = "emails"
    fun ids() = "ids"
}