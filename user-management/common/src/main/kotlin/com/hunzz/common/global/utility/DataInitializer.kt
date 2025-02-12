package com.hunzz.common.global.utility

import org.springframework.boot.CommandLineRunner
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component

@Component
class DataInitializer(
    private val env: Environment,
    private val redisCommands: RedisCommands,
    private val redisKeyProvider: RedisKeyProvider
) : CommandLineRunner {
    override fun run(vararg args: String?) {
        val ddlAuto = env.getProperty("spring.jpa.hibernate.ddl-auto", String::class.java)
        val profile = env.getProperty("spring.profiles.active", String::class.java)

        if (ddlAuto == "create" && profile != "test") {
            redisCommands.deleteAll()

            redisCommands.set(
                key = redisKeyProvider.adminCode(),
                value = "admin_code"
            )
        }
    }
}