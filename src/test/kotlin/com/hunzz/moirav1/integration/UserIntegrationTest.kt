package com.hunzz.moirav1.integration

import com.fasterxml.jackson.databind.ObjectMapper
import com.hunzz.moirav1.domain.user.dto.request.LoginRequest
import com.hunzz.moirav1.domain.user.dto.request.SignUpRequest
import com.hunzz.moirav1.domain.user.dto.response.TokenResponse
import com.hunzz.moirav1.domain.user.dto.response.UserResponse
import com.hunzz.moirav1.utility.TestTemplate
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class UserIntegrationTest : TestTemplate() {
    private lateinit var myId: UUID

    private lateinit var myTokens: TokenResponse

    private lateinit var myLoginRequest: LoginRequest

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
    fun 내_프로필_조회() {
        // given
        val atk = myTokens.atk

        // when
        val myProfile = getUser(targetId = myId, atk = atk).response.contentAsString
            .let { objectMapper.readValue(it, UserResponse::class.java) }

        // then
        assertEquals(myId, myProfile.id)
        assertEquals(mySignupRequest.name, myProfile.name)
        assertEquals(mySignupRequest.email, myProfile.email)
        assertNull(myProfile.imageUrl)
        assertTrue(myProfile.isMyProfile)
    }
}