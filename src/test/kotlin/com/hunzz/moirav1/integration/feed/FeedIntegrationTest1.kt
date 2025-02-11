package com.hunzz.moirav1.integration.feed

import com.fasterxml.jackson.databind.ObjectMapper
import com.hunzz.moirav1.domain.feed.repository.FeedRepository
import com.hunzz.moirav1.domain.post.dto.request.PostRequest
import com.hunzz.moirav1.domain.post.model.PostScope
import com.hunzz.moirav1.domain.user.dto.request.LoginRequest
import com.hunzz.moirav1.domain.user.dto.request.SignUpRequest
import com.hunzz.moirav1.domain.user.dto.response.TokenResponse
import com.hunzz.moirav1.utility.TestTemplate
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FeedIntegrationTest1 : TestTemplate() {
    @Autowired
    private lateinit var feedRepository: FeedRepository

    private lateinit var myId: UUID

    private lateinit var myTokens: TokenResponse

    private lateinit var myLoginRequest: LoginRequest

    private lateinit var mySignupRequest: SignUpRequest

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private lateinit var targetId: UUID

    private lateinit var targetLoginRequest: LoginRequest

    private lateinit var targetSignupRequest: SignUpRequest

    private lateinit var targetTokens: TokenResponse

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

        targetLoginRequest = LoginRequest(
            email = targetSignupRequest.email,
            password = targetSignupRequest.password,
        )
        val targetLoginData = objectMapper.writeValueAsString(targetLoginRequest)
        targetTokens = login(data = targetLoginData).response.contentAsString
            .let { objectMapper.readValue(it, TokenResponse::class.java) }
    }

    @Test
    fun 내가_팔로잉하는_유저A가_게시글을_등록한_경우_내_피드에_유저A의_해당_게시글이_추가() {
        // given
        follow(targetId = targetId, atk = myTokens.atk)

        // when
        val postRequest = PostRequest(
            content = "target이 작성한 게시글입니다.",
            scope = PostScope.PUBLIC.name,
        )
        var postRequestData = objectMapper.writeValueAsString(postRequest)
        val postId = addPost(data = postRequestData, atk = targetTokens.atk)
            .response.contentAsString.toLong()

        // then
        assertEquals(1, feedRepository.findAllByUserId(userId = myId).size)
        assertTrue(
            feedRepository.existsByUserIdAndPostIdAndAuthorId(
                userId = myId,
                postId = postId,
                authorId = targetId
            )
        )

        // when2
        postRequest.scope = PostScope.PRIVATE.name
        postRequestData = objectMapper.writeValueAsString(postRequest)
        val newPostId = addPost(data = postRequestData, atk = targetTokens.atk)
            .response.contentAsString.toLong()

        // then
        assertEquals(1, feedRepository.findAllByUserId(userId = myId).size)
        assertFalse(
            feedRepository.existsByUserIdAndPostIdAndAuthorId(
                userId = myId,
                postId = newPostId,
                authorId = targetId
            )
        )
    }

    @Test
    fun 내가_유저A를_팔로우하는_경우_내_피드에_유저A가_작성한_게시글들이_추가() {
        // given
        val numOfPosts = 10
        val postIds = mutableListOf<Long>()

        repeat(numOfPosts) {
            val postRequest = PostRequest(
                content = "target이 작성한 ${it + 1}번 게시글입니다.",
                scope = PostScope.PUBLIC.name
            )
            val postRequestData = objectMapper.writeValueAsString(postRequest)
            addPost(data = postRequestData, atk = targetTokens.atk)
                .let { result -> postIds.add(result.response.contentAsString.toLong()) }
        }

        // when
        follow(targetId = targetId, atk = myTokens.atk)

        // then
        assertEquals(numOfPosts, feedRepository.findAllByUserId(userId = myId).size)
        assertTrue {
            postIds.all { postId ->
                feedRepository.existsByUserIdAndPostIdAndAuthorId(
                    userId = myId,
                    postId = postId,
                    authorId = targetId
                )
            }
        }
    }

    @Test
    fun 내가_유저A를_언팔로우하는_경우_내_피드에_유저A의_작성한_게시글들이_삭제() {
        // given
        follow(targetId = targetId, atk = myTokens.atk)

        val numOfPosts = 10
        val postIds = mutableListOf<Long>()

        repeat(numOfPosts) {
            val postRequest = PostRequest(
                content = "target이 작성한 ${it + 1}번 게시글입니다.",
                scope = PostScope.PUBLIC.name
            )
            val postRequestData = objectMapper.writeValueAsString(postRequest)
            addPost(data = postRequestData, atk = targetTokens.atk)
                .let { result -> postIds.add(result.response.contentAsString.toLong()) }
        }

        // when
        unfollow(targetId = targetId, atk = myTokens.atk)

        // then
        assertEquals(0, feedRepository.findAllByUserIdAndAuthorId(userId = myId, authorId = targetId).size)
    }
}