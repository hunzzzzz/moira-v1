package com.hunzz.moirav1.domain.user.service

import com.hunzz.moirav1.domain.user.dto.request.LoginRequest
import com.hunzz.moirav1.domain.user.dto.response.TokenResponse
import com.hunzz.moirav1.global.exception.ErrorMessages.BANNED_USER_CANNOT_LOGIN
import com.hunzz.moirav1.global.exception.ErrorMessages.INVALID_LOGIN_INFO
import com.hunzz.moirav1.global.exception.custom.InvalidUserInfoException
import com.hunzz.moirav1.global.utility.*
import org.springframework.stereotype.Component
import java.util.*

@Component
class AuthHandler(
    private val jwtProvider: JwtProvider,
    private val passwordEncoder: PasswordEncoder,
    private val redisCommands: RedisCommands,
    private val redisKeyProvider: RedisKeyProvider,
    private val userAuthProvider: UserAuthProvider
) {
    private fun isExistingEmail(email: String) {
        val userAuthKey = redisKeyProvider.userAuth(email = email)
        val condition = redisCommands.get(key = userAuthKey) != null

        require(condition) { throw InvalidUserInfoException(INVALID_LOGIN_INFO) }
    }

    private fun isUserPassword(password: String, encodedUserPassword: String) {
        val condition = passwordEncoder.matches(
            rawPassword = password,
            encodedPassword = encodedUserPassword
        )

        require(condition) { throw InvalidUserInfoException(INVALID_LOGIN_INFO) }
    }

    private fun isNotBannedUser(userId: UUID) {
        val bannedUsersKey = redisKeyProvider.bannedUsers()
        val condition = redisCommands.zScore(key = bannedUsersKey, value = userId.toString()) == null

        require(condition) { throw InvalidUserInfoException(BANNED_USER_CANNOT_LOGIN) }
    }

    fun login(request: LoginRequest): TokenResponse {
        // validate
        isExistingEmail(email = request.email!!)

        val userAuth = userAuthProvider.getUserAuthFromRedis(email = request.email!!)

        isUserPassword(password = request.password!!, encodedUserPassword = userAuth.password)
        isNotBannedUser(userId = userAuth.userId)

        // create token
        val atk = jwtProvider.createAccessToken(userAuth = userAuth)
        val rtk = jwtProvider.createRefreshToken(userAuth = userAuth)

        // save rtk in redis
        val rtkKey = redisKeyProvider.rtk(email = request.email!!)
        redisCommands.set(key = rtkKey, value = rtk)

        return TokenResponse(atk = atk, rtk = rtk)
    }
}