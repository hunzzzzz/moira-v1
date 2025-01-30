package com.hunzz.moirav1.integration

import com.fasterxml.jackson.databind.ObjectMapper
import com.hunzz.moirav1.domain.user.dto.request.LoginRequest
import com.hunzz.moirav1.domain.user.dto.request.SignUpRequest
import com.hunzz.moirav1.domain.user.dto.response.TokenResponse
import com.hunzz.moirav1.global.exception.ErrorMessages.EXPIRED_AUTH
import com.hunzz.moirav1.global.exception.ErrorResponse
import com.hunzz.moirav1.global.utility.RedisCommands
import com.hunzz.moirav1.global.utility.RedisKeyProvider
import com.hunzz.moirav1.utility.TestTemplate
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LogoutIntegrationTest : TestTemplate() {
    private lateinit var myId: UUID

    private lateinit var myTokens: TokenResponse

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

        // login
        val loginData = objectMapper.writeValueAsString(myLoginRequest)
        myTokens = login(data = loginData).response.contentAsString
            .let { objectMapper.readValue(it, TokenResponse::class.java) }
    }

    @Test
    fun 로그아웃() {
        // given
        val myAtk = myTokens.atk

        // when
        val result = logout(atk = myAtk)

        // then
        val blockedAtkKey = redisKeyProvider.blockedAtk(atk = myAtk)
        val rtkKey = redisKeyProvider.rtk(email = myLoginRequest.email!!)

        assertEquals(200, result.response.status)
        assertEquals(myAtk, redisCommands.get(key = blockedAtkKey))
        assertNull(redisCommands.get(key = rtkKey))
    }

    @Test
    fun 로그아웃_후_인증이_필요한_API에_접근하는_경우() {
        // given
        val data = objectMapper.writeValueAsString(myLoginRequest)
        val myAtk = login(data = data).response.contentAsString
            .let { objectMapper.readValue(it, TokenResponse::class.java) }.atk

        // when
        logout(atk = myAtk)

        val result = getUser(targetId = myId, atk = myAtk)
        val error = result.response.contentAsString
            .let { objectMapper.readValue(it, ErrorResponse::class.java) }

        // then
        assertEquals(401, result.response.status)
        assertEquals(EXPIRED_AUTH, error.message)
    }
}