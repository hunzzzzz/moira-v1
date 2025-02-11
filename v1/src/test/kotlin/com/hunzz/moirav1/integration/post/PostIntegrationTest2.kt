package com.hunzz.moirav1.integration.post

import com.fasterxml.jackson.databind.ObjectMapper
import com.hunzz.moirav1.domain.post.dto.request.PostRequest
import com.hunzz.moirav1.domain.post.model.PostScope
import com.hunzz.moirav1.domain.user.dto.request.LoginRequest
import com.hunzz.moirav1.domain.user.dto.request.SignUpRequest
import com.hunzz.moirav1.domain.user.dto.response.TokenResponse
import com.hunzz.moirav1.global.exception.ErrorMessages.ALREADY_LIKED
import com.hunzz.moirav1.global.exception.ErrorMessages.ALREADY_UNLIKED
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

class PostIntegrationTest2 : TestTemplate() {
    private lateinit var myId: UUID

    private lateinit var myTokens: TokenResponse

    private lateinit var myLoginRequest: LoginRequest

    private lateinit var mySignupRequest: SignUpRequest

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private var postId: Long? = null

    private lateinit var postRequest: PostRequest

    @Autowired
    private lateinit var redisCommands: RedisCommands

    @Autowired
    private lateinit var redisKeyProvider: RedisKeyProvider

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

        // add post
        postRequest = PostRequest(
            content = "테스트 게시글입니다.",
            scope = PostScope.PUBLIC.name
        )
        val postData = objectMapper.writeValueAsString(postRequest)
        postId = addPost(data = postData, atk = myTokens.atk)
            .response.contentAsString.toLong()
    }

    @Test
    fun 게시글_좋아요() {
        // when
        val result = likePost(postId = postId!!, atk = myTokens.atk)

        // then
        val likeKey = redisKeyProvider.like(userId = myId)
        val likeCountKey = redisKeyProvider.likeCount()

        assertEquals(200, result.response.status)
        assertEquals(1, redisCommands.zCard(key = likeKey))
        assertEquals(1.0, redisCommands.zScore(key = likeCountKey, value = postId.toString()))
        assertNotNull(redisCommands.zScore(key = likeKey, value = postId.toString()))
    }

    @Test
    fun 이미_좋아요한_게시글을_다시_좋아요하는_경우() {
        // given
        likePost(postId = postId!!, atk = myTokens.atk)

        // when
        val result = likePost(postId = postId!!, atk = myTokens.atk)
        val error = result.response.contentAsString
            .let { objectMapper.readValue(it, ErrorResponse::class.java) }

        // then
        assertEquals(400, result.response.status)
        assertEquals(ALREADY_LIKED, error.message)
    }

    @Test
    fun 게시글_좋아요_취소() {
        // given
        likePost(postId = postId!!, atk = myTokens.atk)

        // when
        val result = unlikePost(postId = postId!!, atk = myTokens.atk)

        // then
        val likeKey = redisKeyProvider.like(userId = myId)
        val likeCountKey = redisKeyProvider.likeCount()

        assertEquals(200, result.response.status)
        assertEquals(0, redisCommands.zCard(key = likeKey))
        assertEquals(0.0, redisCommands.zScore(key = likeCountKey, value = postId.toString()))
    }

    @Test
    fun 이미_좋아요_취소한_게시글을_다시_취소하는_경우() {
        // when
        val result = unlikePost(postId = postId!!, atk = myTokens.atk)
        val error = result.response.contentAsString
            .let { objectMapper.readValue(it, ErrorResponse::class.java) }

        // then
        assertEquals(400, result.response.status)
        assertEquals(ALREADY_UNLIKED, error.message)
    }
}