package com.hunzz.common.global.utility

import org.springframework.stereotype.Component

@Component
class RedisKeyProvider {
    // admin
    fun adminCode() = "admin_signup_code"

    // user
    fun emails() = "emails"
    fun ids() = "ids"
}