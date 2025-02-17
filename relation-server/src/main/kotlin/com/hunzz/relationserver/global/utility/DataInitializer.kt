package com.hunzz.relationserver.global.utility

import com.hunzz.relationserver.domain.relation.service.RelationHandler
import org.springframework.boot.CommandLineRunner
import org.springframework.core.env.Environment
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.util.*

@Component
class DataInitializer(
    private val env: Environment,
    private val redisKeyProvider: RedisKeyProvider,
    private val redisTemplate: RedisTemplate<String, String>,
    private val relationHandler: RelationHandler
) : CommandLineRunner {
    private fun follow1000Users(myId: UUID) {
        val idsKey = redisKeyProvider.ids()
        val userIds = redisTemplate.opsForSet().members(idsKey)!!.map { UUID.fromString(it) }

        userIds.forEach {
            if (myId != it)
                relationHandler.follow(userId = myId, targetId = it)
        }
    }

    override fun run(vararg args: String?) {
        val ddlAuto = env.getProperty("spring.jpa.hibernate.ddl-auto", String::class.java)
        val profile = env.getProperty("spring.profiles.active", String::class.java)

        if (ddlAuto == "create" && profile != "test") {
            // follow 1000 users
            val myId = UUID.fromString("ef9ab91a-4b19-4fd1-9e77-4ac90727ae1b")
            follow1000Users(myId = myId)
        }
    }
}