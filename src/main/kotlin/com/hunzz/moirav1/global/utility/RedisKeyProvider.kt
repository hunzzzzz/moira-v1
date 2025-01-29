package com.hunzz.moirav1.global.utility

import org.springframework.stereotype.Component

@Component
class RedisKeyProvider {
    // auth
    fun userAuth(email: String) = "auth::$email"
    fun rtk(email: String) = "rtk::$email"

    // admin
    fun adminCode() = "admin_signup_code"
    fun bannedUsers() = "banned_users"
}