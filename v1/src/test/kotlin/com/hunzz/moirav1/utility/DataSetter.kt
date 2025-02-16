package com.hunzz.moirav1.utility

import com.hunzz.moirav1.global.utility.RedisCommands
import com.hunzz.moirav1.global.utility.RedisKeyProvider
import org.springframework.stereotype.Component

@Component
class DataSetter(
    private val redisCommands: RedisCommands,
    private val redisKeyProvider: RedisKeyProvider
) {
    companion object {
        const val TEST_ADMIN_SIGNUP_CODE = "TEST_ADMIN_CODE"
    }

    fun set() {
        // set admin signup code
        val adminCodeKey = redisKeyProvider.adminCode()
        redisCommands.set(key = adminCodeKey, value = TEST_ADMIN_SIGNUP_CODE)
    }
}