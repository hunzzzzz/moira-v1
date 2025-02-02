package com.hunzz.moirav1.integration

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.hunzz.moirav1.domain.relation.dto.response.FollowResponse
import com.hunzz.moirav1.domain.relation.service.RelationHandler
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

class RelationIntegrationTest2 : TestTemplate() {
    private lateinit var myId: UUID

    private lateinit var myTokens: TokenResponse

    private lateinit var myLoginRequest: LoginRequest

    private lateinit var mySignupRequest: SignUpRequest

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private fun getContentsFromHttpResponse(result: MvcResult): MutableList<FollowResponse> {
        val jsonNode = result.response.contentAsString
            .let { objectMapper.readValue(it, JsonNode::class.java) }
        val list = mutableListOf<FollowResponse>()

        jsonNode.get("content").forEach {
            list.add(objectMapper.treeToValue(it, FollowResponse::class.java))
        }

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
    }

    @Test
    fun 내_팔로잉_목록을_조회() {
        // given
        val dummyPassword = "Dummy1234!"
        repeat(30) { index ->
            // dummy signup
            val dummySignupRequest = SignUpRequest(
                email = "dummy${index + 1}@example.com",
                password = dummyPassword,
                password2 = dummyPassword,
                name = "dummy${index + 1}"
            )
            val dummySignupData = objectMapper.writeValueAsString(dummySignupRequest)
            val dummyId = signup(data = dummySignupData).response.contentAsString
                .let { it.substring(1, it.length - 1) }
                .let { UUID.fromString(it) }

            // follow: me -> dummy
            follow(targetId = dummyId, atk = myTokens.atk)
        }

        // when
        val result = getFollowings(userId = myId, atk = myTokens.atk)
        val firstPage = getContentsFromHttpResponse(result = result)

        // then
        assertEquals(200, result.response.status)
        assertEquals(RelationHandler.RELATION_PAGE_SIZE, firstPage.size)
        assertTrue(firstPage.any { firstPage.first().createdAt >= it.createdAt })
    }

    @Test
    fun 내_팔로잉_목록을_조회_중간_페이지() {
        // given
        val dummyPassword = "Dummy1234!"
        repeat(30) { index ->
            // dummy signup
            val dummySignupRequest = SignUpRequest(
                email = "dummy${index + 1}@example.com",
                password = dummyPassword,
                password2 = dummyPassword,
                name = "dummy${index + 1}"
            )
            val dummySignupData = objectMapper.writeValueAsString(dummySignupRequest)
            val dummyId = signup(data = dummySignupData).response.contentAsString
                .let { it.substring(1, it.length - 1) }
                .let { UUID.fromString(it) }

            // follow: me -> dummy
            follow(targetId = dummyId, atk = myTokens.atk)
        }

        // given (get cursor)
        val firstPage = getContentsFromHttpResponse(
            result = getFollowings(userId = myId, atk = myTokens.atk)
        )
        val cursor = firstPage.random().createdAt

        // when
        val result = getFollowings(userId = myId, atk = myTokens.atk, cursor = cursor)
        val nextPage = getContentsFromHttpResponse(result = result)

        // then
        assertEquals(200, result.response.status)
        assertEquals(RelationHandler.RELATION_PAGE_SIZE, nextPage.size)
        assertTrue(nextPage.any { cursor >= it.createdAt })
    }

    @Test
    fun 내_팔로워_목록_조회_첫_페이지() {
        // given
        val dummyPassword = "Dummy1234!"
        repeat(30) { index ->
            // dummy signup
            val dummySignupRequest = SignUpRequest(
                email = "dummy${index + 1}@example.com",
                password = dummyPassword,
                password2 = dummyPassword,
                name = "dummy${index + 1}"
            )
            val dummySignupData = objectMapper.writeValueAsString(dummySignupRequest)
            signup(data = dummySignupData).response.contentAsString

            // dummy login
            val dummyLoginRequest = LoginRequest(
                email = dummySignupRequest.email,
                password = dummyPassword
            )
            val dummyLoginData = objectMapper.writeValueAsString(dummyLoginRequest)
            val dummyAtk = login(data = dummyLoginData).response.contentAsString
                .let { objectMapper.readValue(it, TokenResponse::class.java) }.atk

            // follow: dummy -> me
            follow(targetId = myId, atk = dummyAtk)
        }

        // when
        val result = getFollowers(userId = myId, atk = myTokens.atk)
        val firstPage = getContentsFromHttpResponse(result = result)

        // then
        assertEquals(200, result.response.status)
        assertEquals(RelationHandler.RELATION_PAGE_SIZE, firstPage.size)
        assertTrue(firstPage.any { firstPage.first().createdAt >= it.createdAt })
    }
}