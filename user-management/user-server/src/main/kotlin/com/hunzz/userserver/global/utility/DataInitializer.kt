package com.hunzz.userserver.global.utility

import com.hunzz.userserver.domain.user.dto.request.SignUpRequest
import com.hunzz.userserver.domain.user.service.UserHandler
import com.hunzz.common.global.utility.RedisCommands
import com.hunzz.common.global.utility.RedisKeyProvider
import org.springframework.boot.CommandLineRunner
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component

@Component
class DataInitializer(
    private val env: Environment,
    private val redisCommands: RedisCommands,
    private val redisKeyProvider: RedisKeyProvider,
    private val userHandler: UserHandler
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
    }
}