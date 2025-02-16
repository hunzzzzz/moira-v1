package com.hunzz.moirav1.domain.user.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.hunzz.moirav1.domain.user.dto.request.LoginRequest
import com.hunzz.moirav1.domain.user.dto.response.TokenResponse
import com.hunzz.moirav1.domain.user.model.UserAuth
import com.hunzz.moirav1.global.exception.ErrorMessages.BANNED_USER_CANNOT_LOGIN
import com.hunzz.moirav1.global.exception.ErrorMessages.INVALID_LOGIN_INFO
import com.hunzz.moirav1.global.exception.ErrorMessages.INVALID_TOKEN
import com.hunzz.moirav1.global.exception.custom.InvalidUserInfoException
import com.hunzz.moirav1.global.utility.*
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.TimeUnit

@Component
class AuthHandler(
    @Value("\${jwt.expiration-time.atk}")
    private val expirationTimeOfAtk: Long,

    private val jwtProvider: JwtProvider,
    private val passwordEncoder: PasswordEncoder,
    private val redisCommands: RedisCommands,
    private val redisKeyProvider: RedisKeyProvider,
    private val userAuthProvider: UserAuthProvider,
    private val userHandler: UserHandler
) {
    private fun isExistingEmail(email: String) {
        val condition = userHandler.isUser(email = email)

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

    private fun isRtkNotNull(rtk: String?) {
        val condition = !rtk.isNullOrBlank()

        require(condition) { throw InvalidUserInfoException(INVALID_TOKEN) }
    }

    private fun isValidRtk(rtk: String?) {
        val condition = jwtProvider.validateToken(token = rtk!!).isSuccess

        require(condition) { throw InvalidUserInfoException(INVALID_TOKEN) }
    }

    private fun isSameRtk(rtkFromAuthHeader: String?, rtkFromRedis: String?) {
        val condition = rtkFromAuthHeader == rtkFromRedis

        require(condition) { throw InvalidUserInfoException(INVALID_TOKEN) }
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

    fun logout(email: String, httpServletRequest: HttpServletRequest) {
        // get atk
        val atk = httpServletRequest.getHeader(AUTHORIZATION)

        // setting
        val blockedAtkKey = redisKeyProvider.blockedAtk(atk = atk)
        val rtkKey = redisKeyProvider.rtk(email = email)

        // add atk to blacklist
        redisCommands.set(
            key = blockedAtkKey,
            value = atk,
            expirationTime = expirationTimeOfAtk,
            timeUnit = TimeUnit.MILLISECONDS
        )

        // delete rtk from redis
        redisCommands.delete(key = rtkKey)
    }

    fun refresh(httpServletRequest: HttpServletRequest): TokenResponse {
        // get rtk from authorization header
        val authHeader = httpServletRequest.getHeader(AUTHORIZATION)
        val rtk = jwtProvider.substringToken(token = authHeader)

        isRtkNotNull(rtk = rtk)
        isValidRtk(rtk = rtk!!)

        // get rtk from redis
        val payload = jwtProvider.getUserInfoFromToken(token = rtk!!)
        val email = payload.get("email", String::class.java)

        val rtkKey = redisKeyProvider.rtk(email = email)
        val rtkFromRedis = redisCommands.get(key = rtkKey)

        // validate
        isSameRtk(rtkFromAuthHeader = authHeader, rtkFromRedis = rtkFromRedis)

        // get user auth from redis
        val objectMapper = ObjectMapper()
        val userAuthKey = redisKeyProvider.userAuth(email = email)
        val userAuth = redisCommands.get(key = userAuthKey)
            .let { objectMapper.readValue(it, UserAuth::class.java) }

        // create new atk and rtk
        val newAtk = jwtProvider.createAccessToken(userAuth = userAuth)
        val newRtk = jwtProvider.createRefreshToken(userAuth = userAuth)

        // save new rtk in redis
        redisCommands.set(key = rtkKey, value = newRtk)

        // return tokens
        return TokenResponse(atk = newAtk, rtk = newRtk)
    }
}