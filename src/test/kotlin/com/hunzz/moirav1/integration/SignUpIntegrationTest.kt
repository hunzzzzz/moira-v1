package com.hunzz.moirav1.integration

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
import com.hunzz.moirav1.utility.DataCleaner
import com.hunzz.moirav1.utility.TestTemplate
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class SignUpIntegrationTest : TestTemplate() {
    @Autowired
    lateinit var dataCleaner: DataCleaner

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var redisCommands: RedisCommands

    @Autowired
    private lateinit var redisKeyProvider: RedisKeyProvider

    private lateinit var mySignupRequest: SignUpRequest

    @Autowired
    private lateinit var userRepository: UserRepository

    private fun signup(data: String): MvcResult {
        return mockMvc.perform(
            post("/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(data)
        ).andDo(print()).andReturn()
    }

    @BeforeEach
    fun before() {
        dataCleaner.execute()

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

        // then (db)
        val userId = result.response.contentAsString
            .let { it.substring(1, it.length - 1) }
            .let { UUID.fromString(it) }
        val user = userRepository.findByIdOrNull(id = userId)!!

        assertEquals(201, result.response.status)
        assertEquals(mySignupRequest.email, user.email)
        assertEquals(mySignupRequest.name, user.name)

        // then (redis userAuth 저장 여부 확인)
        val userAuthKey = redisKeyProvider.userAuth(email = user.email)

        assertNotNull(redisCommands.get(key = userAuthKey))
    }

    @Test
    fun 어드민_회원가입() {
        // given (어드민 가입 코드 추가)
        val adminCode = "TEST_ADMIN_CODE"
        val adminCodeKey = redisKeyProvider.adminCode()
        redisCommands.set(key = adminCodeKey, value = adminCode)

        // given
        mySignupRequest.adminCode = adminCode
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
        // given (어드민 가입 코드 추가)
        val adminCode = "TEST_ADMIN_CODE"
        val adminCodeKey = redisKeyProvider.adminCode()
        redisCommands.set(key = adminCodeKey, value = adminCode)

        // given
        mySignupRequest.adminCode = adminCode + "_wrong"
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