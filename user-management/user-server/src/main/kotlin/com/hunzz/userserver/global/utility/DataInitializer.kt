package com.hunzz.userserver.global.utility

import com.hunzz.common.global.utility.RedisKeyProvider
import org.springframework.boot.CommandLineRunner
import org.springframework.core.env.Environment
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component

@Component
class DataInitializer(
    private val env: Environment,
    private val redisKeyProvider: RedisKeyProvider,
    private val redisTemplate: RedisTemplate<String, String>,
) : CommandLineRunner {
    override fun run(vararg args: String?) {
        val ddlAuto = env.getProperty("spring.jpa.hibernate.ddl-auto", String::class.java)
        val profile = env.getProperty("spring.profiles.active", String::class.java)

        if (ddlAuto == "create" && profile != "test") {
            // set admin code
            val adminCodeKey = redisKeyProvider.adminCode()
            redisTemplate.opsForValue().set(adminCodeKey, "admin_code")

            // delete all data in redis
            val keys = redisTemplate.keys("*")
            redisTemplate.delete(keys)
        }
    }
}