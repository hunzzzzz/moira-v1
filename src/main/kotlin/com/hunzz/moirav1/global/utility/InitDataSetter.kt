package com.hunzz.moirav1.global.utility

import com.hunzz.moirav1.domain.relation.service.RelationHandler
import com.hunzz.moirav1.domain.user.dto.request.SignUpRequest
import com.hunzz.moirav1.domain.user.service.UserHandler
import org.springframework.boot.CommandLineRunner
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component

@Component
class InitDataSetter(
    private val env: Environment,
    private val redisCommands: RedisCommands,
    private val redisKeyProvider: RedisKeyProvider,
    private val relationHandler: RelationHandler,
    private val userHandler: UserHandler,
) : CommandLineRunner {
    companion object {
        const val TEMP_ADMIN_CODE = "TEMP_ADMIN_CODE"
    }

    private fun getAdminSignupRequest(): SignUpRequest {
        return SignUpRequest(
            email = "moira_admin@gmail.com",
            password = "Admin1234!",
            password2 = "Admin1234!",
            name = "admin"
        )
    }

    private fun getDummySignupRequest(num: Int): SignUpRequest {
        return SignUpRequest(
            email = "dummy$num@gmail.com",
            password = "Dummy1234!",
            password2 = "Dummy1234!",
            name = "dummy$num"
        )
    }

    override fun run(vararg args: String?) {
        val ddlAuto = env.getProperty("spring.jpa.hibernate.ddl-auto", String::class.java)
        val profile = env.getProperty("spring.profiles.active", String::class.java)

        if (ddlAuto == "create" && profile != "test") {
            // delete all redis keys
            redisCommands.deleteAll()

            // set admin code
            val adminCodeKey = redisKeyProvider.adminCode()
            redisCommands.set(adminCodeKey, TEMP_ADMIN_CODE)

            // admin signup
            val adminId = userHandler.save(request = getAdminSignupRequest())

            repeat(100000) {
                // dummy signup
                val dummyId = userHandler.save(request = getDummySignupRequest(num = it + 1))

                // admin -> dummy (follow)
                relationHandler.follow(userId = adminId, targetId = dummyId)
            }
        }
    }
}