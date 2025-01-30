package com.hunzz.moirav1.integration

import com.fasterxml.jackson.databind.ObjectMapper
import com.hunzz.moirav1.domain.user.dto.request.LoginRequest
import com.hunzz.moirav1.domain.user.dto.request.SignUpRequest
import com.hunzz.moirav1.domain.user.dto.response.TokenResponse
import com.hunzz.moirav1.global.utility.RedisCommands
import com.hunzz.moirav1.global.utility.RedisKeyProvider
import com.hunzz.moirav1.utility.TestTemplate
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull

class AuthIntegrationTest : TestTemplate() {
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
    fun 리프레쉬_토큰_재발급() {
        // when
        Thread.sleep(1000)

        val result = refresh(rtk = myTokens.rtk)
        val newTokens = result.response.contentAsString
            .let { objectMapper.readValue(it, TokenResponse::class.java) }

        // then
        assertEquals(200, result.response.status)
        assertNotEquals(myTokens.atk, newTokens.atk)
        assertNotEquals(myTokens.rtk, newTokens.rtk)

        // then (redis)
        val rtkKey = redisKeyProvider.rtk(email = mySignupRequest.email!!)
        val rtkInRedis = redisCommands.get(key = rtkKey)

        assertNotNull(rtkInRedis)
        assertNotEquals(myTokens.rtk, rtkInRedis)
    }
}