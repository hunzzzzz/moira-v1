package com.hunzz.userserver.utility

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.hunzz.common.domain.user.model.CachedUser
import com.hunzz.common.domain.user.model.UserAuth
import com.hunzz.common.domain.user.model.entity.KakaoUser
import com.hunzz.common.domain.user.model.entity.User
import com.hunzz.common.domain.user.model.property.UserRole
import com.hunzz.common.domain.user.model.property.UserType
import com.hunzz.common.domain.user.repository.KakaoUserRepository
import com.hunzz.userserver.kafka.dto.KakaoSignupKafkaRequest
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Component
class UserKafkaConsumer(
    private val kakaoUserRepository: KakaoUserRepository,
    private val objectMapper: ObjectMapper,
    private val userCacheManager: UserCacheManager,
    private val userRedisHandler: UserRedisHandler
) {
    @KafkaListener(topics = ["signup"], groupId = "user-server-signup")
    fun signup(message: String) {
        val user = objectMapper.readValue(message, User::class.java)
        val userAuth = UserAuth(
            userId = user.id!!,
            type = user.type,
            role = user.role,
            email = user.email,
            password = user.password
        )

        userRedisHandler.signup(userAuth = userAuth)
    }

    @KafkaListener(topics = ["kakao-signup"], groupId = "user-server-kakao-signup")
    @Transactional
    fun kakaoSignup(message: String) {
        val data = objectMapper.readValue(message, KakaoSignupKafkaRequest::class.java)

        // 추후 코루틴으로 리팩토링
        // DB에 객체 저장
        kakaoUserRepository.save(
            KakaoUser(
                id = data.userId,
                email = data.email,
                name = data.name,
            )
        )

        // Redis에 유저 정보 저장
        val userAuth = UserAuth(
            userId = data.userId,
            type = UserType.KAKAO,
            role = UserRole.USER,
            email = data.email,
            password = null
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