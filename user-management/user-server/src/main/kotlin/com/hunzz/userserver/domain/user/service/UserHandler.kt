package com.hunzz.userserver.domain.user.service

import com.hunzz.common.domain.user.model.CachedUser
import com.hunzz.common.domain.user.model.User
import com.hunzz.common.domain.user.model.UserRole
import com.hunzz.common.domain.user.repository.UserRepository
import com.hunzz.common.global.exception.ErrorCode.DIFFERENT_TWO_PASSWORDS
import com.hunzz.common.global.exception.ErrorCode.USER_NOT_FOUND
import com.hunzz.common.global.exception.custom.InvalidUserInfoException
import com.hunzz.common.global.utility.KafkaProducer
import com.hunzz.common.global.utility.PasswordEncoder
import com.hunzz.userserver.domain.user.dto.request.SignUpRequest
import com.hunzz.userserver.domain.user.dto.response.UserResponse
import org.springframework.aop.framework.AopContext
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Component
class UserHandler(
    private val kafkaProducer: KafkaProducer,
    private val passwordEncoder: PasswordEncoder,
    private val userRedisScriptHandler: UserRedisScriptHandler,
    private val userRepository: UserRepository
) {
    private fun proxy() = AopContext.currentProxy() as UserHandler

    private fun isEqualPasswords(password1: String, password2: String) {
        val condition = password1 == password2

        require(condition) { throw InvalidUserInfoException(DIFFERENT_TWO_PASSWORDS) }
    }

    @Transactional
    fun save(request: SignUpRequest): UUID {
        // validate
        isEqualPasswords(password1 = request.password!!, password2 = request.password2!!)
        userRedisScriptHandler.checkSignupRequest(inputEmail = request.email!!, inputAdminCode = request.adminCode)

        // encrypt
        val encodedPassword = passwordEncoder.encodePassword(rawPassword = request.password!!)

        // save (db)
        val user =
            userRepository.save(
                User(
                    role = if (request.adminCode != null) UserRole.ADMIN else UserRole.USER,
                    email = request.email!!,
                    password = encodedPassword,
                    name = request.name!!,
                    imageUrl = null
                )
            )

        // send kafka message (redis command)
        kafkaProducer.send(topic = "signup", data = user)

        return user.id!!
    }

    fun getProfile(userId: UUID, targetId: UUID): UserResponse {
        val user = proxy().getWithLocalCache(userId = targetId)

        return UserResponse(
            id = user.userId,
            status = user.status,
            name = user.name,
            imageUrl = user.imageUrl,
            isMyProfile = userId == targetId
        )
    }

    fun get(userId: UUID): CachedUser {
        val user = userRepository.findByIdOrNull(id = userId)
            ?: throw InvalidUserInfoException(USER_NOT_FOUND)

        return CachedUser(userId = userId, status = user.status, name = user.name, imageUrl = user.imageUrl)
    }

    @Cacheable(cacheNames = ["user"], key = "#userId", cacheManager = "redisCacheManager")
    fun getWithRedisCache(userId: UUID): CachedUser {
        return get(userId = userId)
    }

    @Cacheable(cacheNames = ["user"], key = "#userId", cacheManager = "localCacheManager")
    fun getWithLocalCache(userId: UUID): CachedUser {
        return getWithRedisCache(userId = userId)
    }
}