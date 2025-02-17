package com.hunzz.userserver.domain.user.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.hunzz.common.domain.user.model.User
import com.hunzz.common.domain.user.model.UserAuth
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class UserRedisCommandSender(
    private val objectMapper: ObjectMapper,
    private val userRedisHandler: UserRedisHandler
) {
    @KafkaListener(topics = ["signup"], groupId = "user-server-signup")
    fun signup(message: String) {
        val user = objectMapper.readValue(message, User::class.java)
        val userAuth = UserAuth(
            userId = user.id!!,
            role = user.role,
            email = user.email,
            password = user.password
        )

        userRedisHandler.signup(userAuth = userAuth)
    }
}