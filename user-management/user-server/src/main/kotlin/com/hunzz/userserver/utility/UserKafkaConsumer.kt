package com.hunzz.userserver.utility

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.hunzz.common.domain.user.model.CachedUser
import com.hunzz.common.domain.user.model.User
import com.hunzz.common.domain.user.model.UserAuth
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import java.util.*

@Component
class UserKafkaConsumer(
    private val objectMapper: ObjectMapper,
    private val userCacheManager: UserCacheManager,
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

    @KafkaListener(topics = ["add-user-cache"], groupId = "user-server-add-user-cache")
    fun addUserCache(message: String) {
        val userId = objectMapper.readValue(message, UUID::class.java)

        userCacheManager.getWithRedisCache(userId = userId)
    }

    @KafkaListener(topics = ["add-users-cache"], groupId = "user-server-add-users-cache")
    fun addUsersCache(message: String) {
        val userIds = objectMapper.readValue(message, object : TypeReference<List<UUID>>() {})

        userIds.forEach {
            userCacheManager.getWithRedisCache(userId = it)
        }
    }

    @KafkaListener(topics = ["re-add-user-cache"], groupId = "user-server-re-add-user-cache")
    fun reAddUserCache(message: String) {
        val newCachedUser = objectMapper.readValue(message, CachedUser::class.java)

        // evict local cache
        userCacheManager.evictLocalCache(userId = newCachedUser.userId)

        // re-add redis cache
        userRedisHandler.setUserCache(userId = newCachedUser.userId, cachedUser = newCachedUser)
    }
}