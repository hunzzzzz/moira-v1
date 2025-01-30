package com.hunzz.moirav1.integration

import com.fasterxml.jackson.databind.ObjectMapper
import com.hunzz.moirav1.domain.user.dto.request.LoginRequest
import com.hunzz.moirav1.domain.user.dto.request.SignUpRequest
import com.hunzz.moirav1.domain.user.dto.response.TokenResponse
import com.hunzz.moirav1.global.exception.ErrorMessages.ALREADY_FOLLOWED
import com.hunzz.moirav1.global.exception.ErrorMessages.CANNOT_FOLLOW_ITSELF
import com.hunzz.moirav1.global.exception.ErrorMessages.USER_NOT_FOUND
import com.hunzz.moirav1.global.exception.ErrorResponse
import com.hunzz.moirav1.global.utility.RedisCommands
import com.hunzz.moirav1.global.utility.RedisKeyProvider
import com.hunzz.moirav1.utility.TestTemplate
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class RelationIntegrationTest : TestTemplate() {
    private lateinit var myId: UUID

    private lateinit var myTokens: TokenResponse

    private lateinit var myLoginRequest: LoginRequest

    private lateinit var mySignupRequest: SignUpRequest

    @Autowired
    private lateinit var redisCommands: RedisCommands

    @Autowired
    private lateinit var redisKeyProvider: RedisKeyProvider

    private lateinit var targetId: UUID

    private lateinit var targetSignupRequest: SignUpRequest

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @BeforeEach
    fun before() {
        // signup (me)
        mySignupRequest = SignUpRequest(
            email = "me@example.com",
            password = "Mypassword1234!",
            password2 = "Mypassword1234!",
            name = "me"
        )

        val mySignupData = objectMapper.writeValueAsString(mySignupRequest)
        myId = signup(data = mySignupData).response.contentAsString
            .let { it.substring(1, it.length - 1) }
            .let { UUID.fromString(it) }

        // login (me)
        myLoginRequest = LoginRequest(
            email = "me@example.com",
            password = "Mypassword1234!"
        )

        val loginData = objectMapper.writeValueAsString(myLoginRequest)
        myTokens = login(data = loginData).response.contentAsString
            .let { objectMapper.readValue(it, TokenResponse::class.java) }

        // signup (target)
        targetSignupRequest = SignUpRequest(
            email = "target@example.com",
            password = "Target1234!",
            password2 = "Target1234!",
            name = "target"
        )

        val targetSignupData = objectMapper.writeValueAsString(targetSignupRequest)
        targetId = signup(data = targetSignupData).response.contentAsString
            .let { it.substring(1, it.length - 1) }
            .let { UUID.fromString(it) }
    }

    @Test
    fun 팔로우() {
        // when
        val result = follow(targetId = targetId, atk = myTokens.atk)

        // then
        assertEquals(200, result.response.status)

        // then (redis)
        val followingKey = redisKeyProvider.following(userId = myId)
        val followerKey = redisKeyProvider.follower(userId = targetId)

        assertEquals(1, redisCommands.zCard(key = followingKey))
        assertEquals(1, redisCommands.zCard(key = followerKey))

        assertNotNull(redisCommands.zScore(key = followingKey, value = targetId.toString()))
        assertNotNull(redisCommands.zScore(key = followerKey, value = myId.toString()))
    }

    @Test
    fun 언팔로우() {
        // given
        follow(targetId = targetId, atk = myTokens.atk)

        // when
        val result = unfollow(targetId = targetId, atk = myTokens.atk)

        // then
        assertEquals(200, result.response.status)

        // then (redis)
        val followingKey = redisKeyProvider.following(userId = myId)
        val followerKey = redisKeyProvider.follower(userId = targetId)

        assertEquals(0, redisCommands.zCard(key = followingKey))
        assertEquals(0, redisCommands.zCard(key = followerKey))

        assertNull(redisCommands.zScore(key = followingKey, value = targetId.toString()))
        assertNull(redisCommands.zScore(key = followerKey, value = myId.toString()))
    }

    @Test
    fun 존재하지_않는_대상을_팔로우하려는_경우() {
        // when
        val result = follow(targetId = UUID.randomUUID(), atk = myTokens.atk)
        val error = result.response.contentAsString
            .let { objectMapper.readValue(it, ErrorResponse::class.java) }

        // then
        assertEquals(400, result.response.status)
        assertEquals(USER_NOT_FOUND, error.message)
    }

    @Test
    fun 본인을_팔로우하려는_경우() {
        // when
        val result = follow(targetId = myId, atk = myTokens.atk)
        val error = result.response.contentAsString
            .let { objectMapper.readValue(it, ErrorResponse::class.java) }

        // then
        assertEquals(400, result.response.status)
        assertEquals(CANNOT_FOLLOW_ITSELF, error.message)
    }

    @Test
    fun 이미_팔로우한_대상을_다시_팔로우하려는_경우() {
        // given
        follow(targetId = targetId, atk = myTokens.atk)

        // when
        val result = follow(targetId = targetId, atk = myTokens.atk)
        val error = result.response.contentAsString
            .let { objectMapper.readValue(it, ErrorResponse::class.java) }

        // then
        assertEquals(400, result.response.status)
        assertEquals(ALREADY_FOLLOWED, error.message)
    }
}