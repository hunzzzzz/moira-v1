package com.hunzz.moirav1.integration.feed

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.hunzz.moirav1.domain.feed.dto.response.FeedResponse
import com.hunzz.moirav1.domain.feed.service.FeedHandler.Companion.FEED_PAGE_SIZE
import com.hunzz.moirav1.domain.post.dto.request.PostRequest
import com.hunzz.moirav1.domain.post.model.PostScope
import com.hunzz.moirav1.domain.user.dto.request.LoginRequest
import com.hunzz.moirav1.domain.user.dto.request.SignUpRequest
import com.hunzz.moirav1.domain.user.dto.response.TokenResponse
import com.hunzz.moirav1.utility.TestTemplate
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MvcResult
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FeedIntegrationTest2 : TestTemplate() {
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

    private fun getContentsFromHttpResponse(result: MvcResult): List<FeedResponse> {
        val list: List<FeedResponse> = result.response.contentAsString
            .let { objectMapper.readValue(it) }

        return list
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
    fun 내_피드_조회() {
        // given1 (follow target -> target add post)
        follow(targetId = targetId, atk = myTokens.atk)
        val postRequest = PostRequest(
            content = "target이 작성한 게시글입니다.",
            scope = PostScope.PUBLIC.name,
        )
        val postRequestData = objectMapper.writeValueAsString(postRequest)
        val postId = addPost(data = postRequestData, atk = targetTokens.atk)
            .response.contentAsString.toLong()

        // when1
        val result1 = getFeed(atk = myTokens.atk)
        val feed1 = getContentsFromHttpResponse(result = result1)

        // then1
        assertEquals(200, result1.response.status)
        assertEquals(1, feed1.size)
        assertEquals(postId, feed1.first().postId)
        assertEquals(targetId, feed1.first().userId)

        // given2 (like target's post)
        likePost(postId = postId, atk = myTokens.atk)

        // when2
        val result2 = getFeed(atk = myTokens.atk)
        val feed2 = getContentsFromHttpResponse(result = result2)

        // then2
        assertEquals(200, result2.response.status)
        assertEquals(1, feed2.size)
        assertEquals(postId, feed2.first().postId)
        assertEquals(targetId, feed2.first().userId)
        assertEquals(1, feed2.first().numOfLikes)
        assertTrue(feed2.first().hasLike)

        // given3 (unfollow target)
        unfollow(targetId = targetId, atk = myTokens.atk)

        // when3
        val result3 = getFeed(atk = myTokens.atk)
        val feed3 = getContentsFromHttpResponse(result = result3)

        // then3
        assertEquals(200, result3.response.status)
        assertEquals(0, feed3.size)

        // given4 (target post -> follow target)
        repeat(20) {
            val targetPostRequest = PostRequest(
                content = "target이 작성한 ${it + 1}번 게시글입니다.",
                scope = PostScope.PUBLIC.name,
            )
            val targetPostRequestData = objectMapper.writeValueAsString(targetPostRequest)

            addPost(data = targetPostRequestData, atk = targetTokens.atk)
        }
        follow(targetId = targetId, atk = myTokens.atk)

        // when4
        val result4FirstPage = getFeed(atk = myTokens.atk)
        val feed4FirstPage = getContentsFromHttpResponse(result = result4FirstPage)

        val cursor = feed4FirstPage.random().postId
        val result4NextPage = getFeed(atk = myTokens.atk, cursor = cursor)
        val feed4NextPage = getContentsFromHttpResponse(result = result4NextPage)

        // then4
        assertEquals(200, result4FirstPage.response.status)
        assertEquals(FEED_PAGE_SIZE, feed4FirstPage.size)
        assertTrue { feed4FirstPage.first().postId > feed4FirstPage.last().postId }

        assertEquals(200, result4NextPage.response.status)
        assertEquals(FEED_PAGE_SIZE, feed4NextPage.size)
        assertTrue { feed4NextPage.any { cursor > it.postId } }
    }
}