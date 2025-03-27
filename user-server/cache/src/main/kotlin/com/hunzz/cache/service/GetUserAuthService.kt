package com.hunzz.cache.service

import com.hunzz.common.model.cache.UserAuth
import com.hunzz.common.password.PasswordEncoder
import com.hunzz.common.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class GetUserAuthService(
    private val passwordEncoder: PasswordEncoder,
    private val userRepository: UserRepository
) {
    fun getUserAuth(
        email: String
    ): UserAuth? {
        return userRepository.findUserAuth(email = email)
    }

    fun validateThenGetUserAuth(
        email: String,
        password: String
    ): UserAuth {
        val userAuth = userRepository.findUserAuth(email = email)
            ?: throw IllegalStateException("UserAuth not found")

        if (passwordEncoder.matches(password, userAuth.password))
            return userAuth
        else throw IllegalStateException("Invalid password")
    }
}