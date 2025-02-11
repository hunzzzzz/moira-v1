package com.hunzz.common.domain.user.service

import com.hunzz.common.domain.user.dto.request.SignUpRequest
import com.hunzz.common.domain.user.model.User
import com.hunzz.common.domain.user.model.UserRole
import com.hunzz.common.domain.user.repository.UserRepository
import com.hunzz.common.global.exception.ErrorMessages.DIFFERENT_TWO_PASSWORDS
import com.hunzz.common.global.exception.ErrorMessages.DUPLICATED_EMAIL
import com.hunzz.common.global.exception.ErrorMessages.INVALID_ADMIN_CODE
import com.hunzz.common.global.exception.custom.InvalidAdminRequestException
import com.hunzz.common.global.exception.custom.InvalidUserInfoException
import com.hunzz.common.global.utility.PasswordEncoder
import com.hunzz.common.global.utility.RedisCommands
import com.hunzz.common.global.utility.RedisKeyProvider
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Component
class UserHandler(
    private val passwordEncoder: PasswordEncoder,
    private val redisCommands: RedisCommands,
    private val redisKeyProvider: RedisKeyProvider,
    private val userRepository: UserRepository
) {
    private fun isEqualPasswords(password1: String, password2: String) {
        val condition = password1 == password2

        require(condition) { throw InvalidUserInfoException(DIFFERENT_TWO_PASSWORDS) }
    }

    private fun isUniqueEmail(email: String) {
        val emailsKey = redisKeyProvider.emails()
        val condition = !redisCommands.sIsMember(key = emailsKey, value = email)

        require(condition) { throw InvalidUserInfoException(DUPLICATED_EMAIL) }
    }

    private fun isValidAdminCode(inputAdminCode: String) {
        val adminCodeKey = redisKeyProvider.adminCode()
        val adminCode = redisCommands.get(key = adminCodeKey)!!
        val condition = adminCode == inputAdminCode

        require(condition) { throw InvalidAdminRequestException(INVALID_ADMIN_CODE) }
    }

    @Transactional
    fun save(request: SignUpRequest): UUID {
        // validate
        isEqualPasswords(password1 = request.password!!, password2 = request.password2!!)
        isUniqueEmail(email = request.email!!)
        if (request.adminCode != null) isValidAdminCode(inputAdminCode = request.adminCode!!)

        // encrypt
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

        return user.id!!
    }
}