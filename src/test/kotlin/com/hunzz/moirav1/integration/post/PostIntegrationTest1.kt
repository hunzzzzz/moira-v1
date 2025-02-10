package com.hunzz.moirav1.integration.post

import com.fasterxml.jackson.databind.ObjectMapper
import com.hunzz.moirav1.domain.feed.repository.FeedRepository
import com.hunzz.moirav1.domain.post.dto.request.PostRequest
import com.hunzz.moirav1.domain.post.model.PostScope
import com.hunzz.moirav1.domain.post.model.PostStatus
import com.hunzz.moirav1.domain.post.repository.PostRepository
import com.hunzz.moirav1.domain.user.dto.request.LoginRequest
import com.hunzz.moirav1.domain.user.dto.request.SignUpRequest
import com.hunzz.moirav1.domain.user.dto.response.TokenResponse
import com.hunzz.moirav1.global.exception.ErrorMessages.CANNOT_UPDATE_OTHERS_POST
import com.hunzz.moirav1.global.exception.ErrorMessages.UNWRITTEN_POST_CONTENT
import com.hunzz.moirav1.global.exception.ErrorMessages.UNWRITTEN_SCOPE
import com.hunzz.moirav1.global.exception.ErrorResponse
import com.hunzz.moirav1.global.utility.RedisCommands
import com.hunzz.moirav1.global.utility.RedisKeyProvider
import com.hunzz.moirav1.utility.TestTemplate
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class PostIntegrationTest1 : TestTemplate() {
    @Autowired
    private lateinit var feedRepository: FeedRepository

    private lateinit var myId: UUID

    private lateinit var myTokens: TokenResponse

    private lateinit var myLoginRequest: LoginRequest

    private lateinit var mySignupRequest: SignUpRequest

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var postRepository: PostRepository

    private lateinit var postRequest: PostRequest

    @Autowired
    private lateinit var redisCommands: RedisCommands

    @Autowired
    private lateinit var redisKeyProvider: RedisKeyProvider

    private fun otherSignupAndLogin(): String {
        val otherSignupRequest = SignUpRequest(
            email = "other@example.com",
            password = "Other1234!",
            password2 = "Other1234!",
            name = "other"
        )
        val otherSignupData = objectMapper.writeValueAsString(otherSignupRequest)
        signup(data = otherSignupData)

        val otherLoginRequest = LoginRequest(
            email = otherSignupRequest.email,
            password = otherSignupRequest.password
        )
        val loginData = objectMapper.writeValueAsString(otherLoginRequest)
        val otherTokens = login(data = loginData).response.contentAsString
            .let { objectMapper.readValue(it, TokenResponse::class.java) }

        return otherTokens.atk
    }

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

        // set post data
        postRequest = PostRequest(
            content = "테스트 게시글입니다.",
            scope = PostScope.PUBLIC.name
        )
    }

    @Test
    fun 게시글_등록() {
        // given
        val data = objectMapper.writeValueAsString(postRequest)

        // given (feed)
        val numOfDummyUsers = 10
        val followerKey = redisKeyProvider.follower(userId = myId)

        repeat(numOfDummyUsers) {
            redisCommands.zAdd(
                key = followerKey,
                value = UUID.randomUUID().toString(),
                score = System.currentTimeMillis().toDouble()
            )
        }

        // when
        val result = addPost(data = data, atk = myTokens.atk)
        val postId = result.response.contentAsString.toLong()

        // then
        val post = postRepository.findByIdOrNull(id = postId)!!

        assertEquals(201, result.response.status)
        assertEquals(myId, post.userId)
        assertEquals(postRequest.content, post.content)
        assertEquals(PostScope.valueOf(postRequest.scope!!), post.scope)

        assertEquals(numOfDummyUsers, feedRepository.count().toInt())

        // then (redis)
        val likeCountKey = redisKeyProvider.likeCount()

        assertEquals(0.0, redisCommands.zScore(key = likeCountKey, value = postId.toString()))
    }

    @Test
    fun 게시글_내용을_입력하지_않고_게시글을_등록하는_경우() {
        // given
        postRequest.content = null
        val data = objectMapper.writeValueAsString(postRequest)

        // when
        val result = addPost(data = data, atk = myTokens.atk)
        val error = result.response.contentAsString
            .let { objectMapper.readValue(it, ErrorResponse::class.java) }

        // then
        assertEquals(400, result.response.status)
        assertEquals(UNWRITTEN_POST_CONTENT, error.message)
    }

    @Test
    fun 게시글_공개범위를_입력하지_않고_게시글을_등록하는_경우() {
        // given
        postRequest.scope = null
        val data = objectMapper.writeValueAsString(postRequest)

        // when
        val result = addPost(data = data, atk = myTokens.atk)
        val error = result.response.contentAsString
            .let { objectMapper.readValue(it, ErrorResponse::class.java) }

        // then
        assertEquals(400, result.response.status)
        assertEquals(UNWRITTEN_SCOPE, error.message)
    }

    @Test
    fun 게시글_수정() {
        // given
        val addData = objectMapper.writeValueAsString(postRequest)
        val postId = addPost(data = addData, atk = myTokens.atk)
            .response.contentAsString.toLong()

        // when
        val updatedContent = "수정된 테스트 내용입니다."
        postRequest.content = updatedContent
        val updateData = objectMapper.writeValueAsString(postRequest)

        val result = updatePost(postId = postId, data = updateData, atk = myTokens.atk)

        // then
        val post = postRepository.findByIdOrNull(id = postId)!!

        assertEquals(200, result.response.status)
        assertEquals(myId, post.userId)
        assertEquals(updatedContent, post.content)
        assertEquals(PostScope.valueOf(postRequest.scope!!), post.scope)
    }

    @Test
    fun 다른_작성자의_게시글을_수정하는_경우() {
        // given
        val otherAtk = otherSignupAndLogin()
        val otherPostRequest = PostRequest(
            content = "다른 사람이 작성한 게시글입니다.",
            scope = PostScope.PUBLIC.name
        )
        val otherPostData = objectMapper.writeValueAsString(otherPostRequest)
        val postId = addPost(data = otherPostData, atk = otherAtk)
            .response.contentAsString.toLong()

        // when
        val updatedContent = "수정된 테스트 내용입니다."
        postRequest.content = updatedContent
        val updateData = objectMapper.writeValueAsString(postRequest)

        val result = updatePost(postId = postId, data = updateData, atk = myTokens.atk)
        val error = result.response.contentAsString
            .let { objectMapper.readValue(it, ErrorResponse::class.java) }

        // then
        assertEquals(400, result.response.status)
        assertEquals(CANNOT_UPDATE_OTHERS_POST, error.message)
    }

    @Test
    fun 게시글_삭제() {
        // given
        val addData = objectMapper.writeValueAsString(postRequest)
        val postId = addPost(data = addData, atk = myTokens.atk)
            .response.contentAsString.toLong()

        // when
        val result = deletePost(postId = postId, atk = myTokens.atk)

        // then
        val deletedPost = postRepository.findByIdOrNull(id = postId)

        assertEquals(204, result.response.status)
        assertNotNull(deletedPost)
        assertEquals(PostStatus.DELETED, deletedPost.status)
    }

    @Test
    fun 다른_작성자의_게시글을_삭제하는_경우() {
        // given
        val otherAtk = otherSignupAndLogin()
        val otherPostRequest = PostRequest(
            content = "다른 사람이 작성한 게시글입니다.",
            scope = PostScope.PUBLIC.name
        )
        val otherPostData = objectMapper.writeValueAsString(otherPostRequest)
        val postId = addPost(data = otherPostData, atk = otherAtk)
            .response.contentAsString.toLong()

        // when
        val result = deletePost(postId = postId, atk = myTokens.atk)
        val error = result.response.contentAsString
            .let { objectMapper.readValue(it, ErrorResponse::class.java) }

        // then
        assertEquals(400, result.response.status)
        assertEquals(CANNOT_UPDATE_OTHERS_POST, error.message)
    }
}