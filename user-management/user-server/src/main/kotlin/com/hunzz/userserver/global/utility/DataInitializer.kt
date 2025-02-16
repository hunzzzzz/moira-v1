package com.hunzz.userserver.global.utility

import com.hunzz.common.global.utility.RedisKeyProvider
import com.hunzz.userserver.domain.user.dto.request.SignUpRequest
import com.hunzz.userserver.domain.user.service.UserHandler
import org.springframework.boot.CommandLineRunner
import org.springframework.core.env.Environment
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component

@Component
class DataInitializer(
    private val env: Environment,
    private val redisKeyProvider: RedisKeyProvider,
    private val redisTemplate: RedisTemplate<String, String>,
    private val userHandler: UserHandler
) : CommandLineRunner {
    private fun add1000Users() {
        repeat(1000) {
            val request = SignUpRequest(
                email = "dummy${it + 1}@example.com",
                password = "Test1234!",
                password2 = "Test1234!",
                name = "${it + 1}번 더미 유저"
            )
            userHandler.save(request = request)
        }
    }

    override fun run(vararg args: String?) {
        val ddlAuto = env.getProperty("spring.jpa.hibernate.ddl-auto", String::class.java)
        val profile = env.getProperty("spring.profiles.active", String::class.java)

        if (ddlAuto == "create" && profile != "test") {
            redisTemplate.keys("*").forEach { redisTemplate.delete(it) }

            // set admin code
            val adminCodeKey = redisKeyProvider.adminCode()
            redisTemplate.opsForValue().set(adminCodeKey, "admin_code")

            // signup 1000 users
            add1000Users()
        }
    }
}