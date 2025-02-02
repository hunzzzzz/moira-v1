package com.hunzz.moirav1.domain.user.service

import com.hunzz.moirav1.domain.user.dto.request.SignUpRequest
import com.hunzz.moirav1.domain.user.dto.response.UserResponse
import com.hunzz.moirav1.domain.user.model.User
import com.hunzz.moirav1.domain.user.model.UserRole
import com.hunzz.moirav1.domain.user.repository.UserRepository
import com.hunzz.moirav1.global.exception.ErrorMessages.DIFFERENT_TWO_PASSWORDS
import com.hunzz.moirav1.global.exception.ErrorMessages.DUPLICATED_EMAIL
import com.hunzz.moirav1.global.exception.ErrorMessages.INVALID_ADMIN_CODE
import com.hunzz.moirav1.global.exception.ErrorMessages.USER_NOT_FOUND
import com.hunzz.moirav1.global.exception.custom.InvalidAdminRequestException
import com.hunzz.moirav1.global.exception.custom.InvalidUserInfoException
import com.hunzz.moirav1.global.utility.PasswordEncoder
import com.hunzz.moirav1.global.utility.RedisCommands
import com.hunzz.moirav1.global.utility.RedisKeyProvider
import com.hunzz.moirav1.global.utility.UserAuthProvider
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import java.util.*

@Component
class UserHandler(
    private val passwordEncoder: PasswordEncoder,
    private val redisCommands: RedisCommands,
    private val redisKeyProvider: RedisKeyProvider,
    private val userAuthProvider: UserAuthProvider,
    private val userRepository: UserRepository
) {
    private fun isEqualPasswords(password1: String, password2: String) {
        val condition = password1 == password2

        require(condition) { throw InvalidUserInfoException(DIFFERENT_TWO_PASSWORDS) }
    }

    private fun isUniqueEmail(email: String) {
        val condition = !isUser(email = email)

        require(condition) { throw InvalidUserInfoException(DUPLICATED_EMAIL) }
    }

    private fun isValidAdminCode(inputAdminCode: String) {
        val adminCodeKey = redisKeyProvider.adminCode()
        val adminCode = redisCommands.get(key = adminCodeKey)!!
        val condition = adminCode == inputAdminCode

        require(condition) { throw InvalidAdminRequestException(INVALID_ADMIN_CODE) }
    }

    fun isUser(email: String): Boolean {
        val emailsKey = redisKeyProvider.emails()

        return redisCommands.sIsMember(key = emailsKey, value = email)
    }

    fun isUser(userId: UUID): Boolean {
        val idsKey = redisKeyProvider.ids()

        return redisCommands.sIsMember(key = idsKey, value = userId.toString())
    }

    fun get(userId: UUID): User {
        return userRepository.findByIdOrNull(id = userId) ?: throw InvalidUserInfoException(USER_NOT_FOUND)
    }

    fun get(userId: UUID, targetId: UUID): UserResponse {
        val user = get(userId = targetId)

        return UserResponse.from(user = user, isMyProfile = userId == targetId)
    }

    fun save(request: SignUpRequest): UUID {
        // validate
        isEqualPasswords(password1 = request.password!!, password2 = request.password2!!)
        isUniqueEmail(email = request.email!!)
        if (request.adminCode != null) isValidAdminCode(inputAdminCode = request.adminCode!!)

        // create
        val encodedPassword = passwordEncoder.encodePassword(rawPassword = request.password!!)

        // save (db)
        val user = userRepository.save(
            User(
                role = if (request.adminCode != null) UserRole.ADMIN else UserRole.USER,
                email = request.email!!,
                password = encodedPassword,
                name = request.name!!,
                imageUrl = null
            )
        )

        // save (redis)
        val userAuth = userAuthProvider.saveUserAuthInRedis(user = user)

        val emailsKey = redisKeyProvider.emails()
        val idsKey = redisKeyProvider.ids()

        redisCommands.sAdd(key = emailsKey, value = userAuth.email)
        redisCommands.sAdd(key = idsKey, value = userAuth.userId.toString())

        return userAuth.userId
    }
}