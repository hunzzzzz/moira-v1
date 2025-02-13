package com.hunzz.userserver.domain.user.service

import com.hunzz.userserver.domain.user.dto.request.SignUpRequest
import com.hunzz.common.domain.user.model.User
import com.hunzz.common.domain.user.model.UserRole
import com.hunzz.common.domain.user.repository.UserRepository
import com.hunzz.common.global.exception.ErrorCode.DIFFERENT_TWO_PASSWORDS
import com.hunzz.common.global.exception.custom.InvalidUserInfoException
import com.hunzz.common.global.utility.PasswordEncoder
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Component
class UserHandler(
    private val passwordEncoder: PasswordEncoder,
    private val userChecker: UserChecker,
    private val userRepository: UserRepository
) {
    private fun isEqualPasswords(password1: String, password2: String) {
        val condition = password1 == password2

        require(condition) { throw InvalidUserInfoException(DIFFERENT_TWO_PASSWORDS) }
    }

    @Transactional
    fun save(request: SignUpRequest): UUID {
        // validate
        isEqualPasswords(password1 = request.password!!, password2 = request.password2!!)
        userChecker.checkSignupRequest(inputEmail = request.email!!, inputAdminCode = request.adminCode)

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

        // save (redis)
        userChecker.signup(user = user)

        return user.id!!
    }
}