package com.hunzz.moirav1.integration

import com.fasterxml.jackson.databind.ObjectMapper
import com.hunzz.moirav1.domain.user.dto.request.LoginRequest
import com.hunzz.moirav1.domain.user.dto.request.SignUpRequest
import com.hunzz.moirav1.domain.user.dto.response.TokenResponse
import com.hunzz.moirav1.global.exception.ErrorMessages.BANNED_USER_CANNOT_LOGIN
import com.hunzz.moirav1.global.exception.ErrorMessages.INVALID_LOGIN_INFO
import com.hunzz.moirav1.global.exception.ErrorResponse
import com.hunzz.moirav1.global.utility.JwtProvider
import com.hunzz.moirav1.global.utility.RedisCommands
import com.hunzz.moirav1.global.utility.RedisKeyProvider
import com.hunzz.moirav1.utility.TestTemplate
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LoginIntegrationTest : TestTemplate() {
    @Autowired
    private lateinit var jwtProvider: JwtProvider

    private lateinit var myId: UUID

    private lateinit var myLoginRequest: LoginRequest

    @Autowired
    private lateinit var redisCommands: RedisCommands

    @Autowired
    private lateinit var redisKeyProvider: RedisKeyProvider

    private lateinit var mySignupRequest: SignUpRequest

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @BeforeEach
    fun before() {
        // signup
        mySignupRequest = SignUpRequest(
            email = "me@example.com",
            password = "Mypassword1234!",
            password2 = "Mypassword1234!",
            name = "me"
        )

        val data = objectMapper.writeValueAsString(mySignupRequest)
        myId = signup(data = data).response.contentAsString
            .let { it.substring(1, it.length - 1) }
            .let { UUID.fromString(it) }

        // set login data
        myLoginRequest = LoginRequest(
            email = "me@example.com",
            password = "Mypassword1234!"
        )
    }

    @Test
    fun 로그인() {
        // given
        val data = objectMapper.writeValueAsString(myLoginRequest)

        // when
        val result = login(data = data)
        val tokens = result.response.contentAsString
            .let { objectMapper.readValue(it, TokenResponse::class.java) }

        val atk = tokens.atk.let { jwtProvider.substringToken(token = it) }
        val rtk = tokens.rtk.let { jwtProvider.substringToken(token = it) }

        // then
        assertEquals(200, result.response.status)
        assertTrue(jwtProvider.validateToken(token = atk!!).isSuccess)
        assertTrue(jwtProvider.validateToken(token = rtk!!).isSuccess)
    }

    @Test
    fun 로그인시_입력한_이메일이_존재하지_않는_경우() {
        // given
        myLoginRequest.email = "wrong@example.com"
        val data = objectMapper.writeValueAsString(myLoginRequest)

        // when
        val result = login(data = data)
        val error = result.response.contentAsString
            .let { objectMapper.readValue(it, ErrorResponse::class.java) }

        // then
        assertEquals(400, result.response.status)
        assertEquals(INVALID_LOGIN_INFO, error.message)
    }

    @Test
    fun 로그인시_입력한_비밀번호가_올바르지_않은_경우() {
        // given
        myLoginRequest.password = "Wrongpassword1234!"
        val data = objectMapper.writeValueAsString(myLoginRequest)

        // when
        val result = login(data = data)
        val error = result.response.contentAsString
            .let { objectMapper.readValue(it, ErrorResponse::class.java) }

        // then
        assertEquals(400, result.response.status)
        assertEquals(INVALID_LOGIN_INFO, error.message)
    }

    @Test
    fun 계정_정지된_유저가_로그인을_시도하는_경우() {
        // given (내 계정이 정지됨)
        val bannedUsersKey = redisKeyProvider.bannedUsers()
        redisCommands.zAdd(key = bannedUsersKey, value = myId.toString(), score = System.currentTimeMillis().toDouble())

        // given
        val data = objectMapper.writeValueAsString(myLoginRequest)

        // when
        val result = login(data = data)
        val error = result.response.contentAsString
            .let { objectMapper.readValue(it, ErrorResponse::class.java) }

        // then
        assertEquals(400, result.response.status)
        assertEquals(BANNED_USER_CANNOT_LOGIN, error.message)
    }
}