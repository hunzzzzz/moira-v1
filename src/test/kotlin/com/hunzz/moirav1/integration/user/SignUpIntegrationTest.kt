package com.hunzz.moirav1.integration.user

import com.fasterxml.jackson.databind.ObjectMapper
import com.hunzz.moirav1.domain.user.dto.request.SignUpRequest
import com.hunzz.moirav1.domain.user.model.UserRole
import com.hunzz.moirav1.domain.user.repository.UserRepository
import com.hunzz.moirav1.global.exception.ErrorMessages.DIFFERENT_TWO_PASSWORDS
import com.hunzz.moirav1.global.exception.ErrorMessages.DUPLICATED_EMAIL
import com.hunzz.moirav1.global.exception.ErrorMessages.INVALID_ADMIN_CODE
import com.hunzz.moirav1.global.exception.ErrorMessages.INVALID_EMAIL
import com.hunzz.moirav1.global.exception.ErrorMessages.INVALID_PASSWORD
import com.hunzz.moirav1.global.exception.ErrorMessages.UNWRITTEN_NAME
import com.hunzz.moirav1.global.exception.ErrorResponse
import com.hunzz.moirav1.global.utility.RedisCommands
import com.hunzz.moirav1.global.utility.RedisKeyProvider
import com.hunzz.moirav1.utility.DataSetter.Companion.TEST_ADMIN_SIGNUP_CODE
import com.hunzz.moirav1.utility.TestTemplate
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SignUpIntegrationTest : TestTemplate() {
    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var redisCommands: RedisCommands

    @Autowired
    private lateinit var redisKeyProvider: RedisKeyProvider

    private lateinit var mySignupRequest: SignUpRequest

    @Autowired
    private lateinit var userRepository: UserRepository

    @BeforeEach
    fun before() {
        mySignupRequest = SignUpRequest(
            email = "me@example.com",
            password = "Mypassword1234!",
            password2 = "Mypassword1234!",
            name = "me"
        )
    }

    @Test
    fun 회원가입() {
        // given
        val data = objectMapper.writeValueAsString(mySignupRequest)

        // when
        val result = signup(data = data)

        // then
        val userId = result.response.contentAsString
            .let { it.substring(1, it.length - 1) }
            .let { UUID.fromString(it) }
        val user = userRepository.findByIdOrNull(id = userId)!!

        assertEquals(201, result.response.status)
        assertEquals(mySignupRequest.email, user.email)
        assertEquals(mySignupRequest.name, user.name)

        // then (redis)
        val emailsKey = redisKeyProvider.emails()
        val idsKey = redisKeyProvider.ids()
        val userAuthKey = redisKeyProvider.userAuth(email = user.email)

        assertTrue(redisCommands.sIsMember(key = emailsKey, value = user.email))
        assertTrue(redisCommands.sIsMember(key = idsKey, value = user.id.toString()))
        assertNotNull(redisCommands.get(key = userAuthKey))
    }

    @Test
    fun 어드민_회원가입() {
        // given
        mySignupRequest.adminCode = TEST_ADMIN_SIGNUP_CODE
        val data = objectMapper.writeValueAsString(mySignupRequest)

        // when
        val result = signup(data = data)

        // then
        val userId = result.response.contentAsString
            .let { it.substring(1, it.length - 1) }
            .let { UUID.fromString(it) }
        val user = userRepository.findByIdOrNull(id = userId)!!

        assertEquals(201, result.response.status)
        assertEquals(UserRole.ADMIN, user.role)
    }

    @Test
    fun 회원가입시_유효하지_않은_이메일을_사용한_경우() {
        // given
        mySignupRequest.email = "me"
        val data = objectMapper.writeValueAsString(mySignupRequest)

        // when
        val result = signup(data = data)
        val error = result.response.contentAsString
            .let { objectMapper.readValue(it, ErrorResponse::class.java) }

        // then
        assertEquals(400, result.response.status)
        assertEquals(INVALID_EMAIL, error.message)
    }

    @Test
    fun 회원가입시_유효하지_않은_비밀번호를_사용한_경우() {
        // given
        mySignupRequest.password = "password"
        val data = objectMapper.writeValueAsString(mySignupRequest)

        // when
        val result = signup(data = data)
        val error = result.response.contentAsString
            .let { objectMapper.readValue(it, ErrorResponse::class.java) }

        // then
        assertEquals(400, result.response.status)
        assertEquals(INVALID_PASSWORD, error.message)
    }

    @Test
    fun 이름을_입력하지_않은_경우() {
        // given
        mySignupRequest.name = null
        val data = objectMapper.writeValueAsString(mySignupRequest)

        // when
        val result = signup(data = data)
        val error = result.response.contentAsString
            .let { objectMapper.readValue(it, ErrorResponse::class.java) }

        // then
        assertEquals(400, result.response.status)
        assertEquals(UNWRITTEN_NAME, error.message)
    }

    @Test
    fun 회원가입시_입력한_두_비밀번호가_다른_경우() {
        // given
        mySignupRequest.password2 = "YourPassword123!"
        val data = objectMapper.writeValueAsString(mySignupRequest)

        // when
        val result = signup(data = data)
        val error = result.response.contentAsString
            .let { objectMapper.readValue(it, ErrorResponse::class.java) }

        // then
        assertEquals(400, result.response.status)
        assertEquals(DIFFERENT_TWO_PASSWORDS, error.message)
    }

    @Test
    fun 다른_유저가_사용중인_이메일을_사용한_경우() {
        // given
        val data = objectMapper.writeValueAsString(mySignupRequest)
        signup(data = data)

        // when
        val result = signup(data = data)
        val error = result.response.contentAsString
            .let { objectMapper.readValue(it, ErrorResponse::class.java) }

        // then
        assertEquals(400, result.response.status)
        assertEquals(DUPLICATED_EMAIL, error.message)
    }

    @Test
    fun 어드민_회원가입시_잘못된_어드민_코드를_입력한_경우() {
        // given
        mySignupRequest.adminCode = TEST_ADMIN_SIGNUP_CODE + "_WRONG"
        val data = objectMapper.writeValueAsString(mySignupRequest)

        // when
        val result = signup(data = data)
        val error = result.response.contentAsString
            .let { objectMapper.readValue(it, ErrorResponse::class.java) }

        // then
        assertEquals(400, result.response.status)
        assertEquals(INVALID_ADMIN_CODE, error.message)
    }
}