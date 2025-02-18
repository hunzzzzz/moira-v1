package com.hunzz.gatewayserver.filter

import com.hunzz.gatewayserver.exception.ErrorMessages.EXPIRED_ATK
import com.hunzz.gatewayserver.exception.ErrorMessages.EXPIRED_AUTH
import com.hunzz.gatewayserver.exception.ErrorMessages.INVALID_TOKEN
import com.hunzz.gatewayserver.exception.ErrorMessages.UNPACKED_ATK
import com.hunzz.gatewayserver.exception.custom.JwtException
import com.hunzz.gatewayserver.utility.JwtProvider
import com.hunzz.gatewayserver.utility.RedisCommands
import com.hunzz.gatewayserver.utility.RedisKeyProvider
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.mock.http.server.reactive.MockServerHttpRequest
import org.springframework.mock.web.server.MockServerWebExchange
import reactor.core.publisher.Mono

@ExtendWith(MockitoExtension::class)
class JwtAuthorizationFilterTest {
    @Mock
    private lateinit var chain: GatewayFilterChain

    @Mock
    private lateinit var jwtProvider: JwtProvider

    @Mock
    private lateinit var redisKeyProvider: RedisKeyProvider

    @Mock
    private lateinit var redisCommands: RedisCommands

    @InjectMocks
    private lateinit var filter: JwtAuthorizationFilter

    @Test
    fun 필터_통과() {
        val validToken = "valid_token"
        val authHeader = "Bearer $validToken"

        val request = MockServerHttpRequest.get("").header(AUTHORIZATION, authHeader).build()
        val exchange = MockServerWebExchange.from(request)

        val blockedAtkKey = "test-blocked_atk"

        `when`(jwtProvider.substringToken(authHeader)).thenReturn(validToken)
        `when`(jwtProvider.validateToken(validToken)).thenReturn(Result.success(any()))
        `when`(redisKeyProvider.blockedAtk(validToken)).thenReturn(blockedAtkKey)
        `when`(redisCommands.get(blockedAtkKey)).thenReturn(null)
        `when`(chain.filter(any())).thenReturn(Mono.empty())

        filter.apply(null).filter(exchange, chain).block()

        // then
        verify(chain).filter(exchange)
    }

    @Test
    fun Authorization_헤더에_값이_넘어오지_않은_경우() {
        // given
        val request = MockServerHttpRequest.get("").build()
        val exchange = MockServerWebExchange.from(request)

        // expected
        assertThrows(
            JwtException::class.java,
            { filter.apply(null).filter(exchange, chain).block() },
            UNPACKED_ATK
        )
    }

    @Test
    fun Authorization_헤더에_유효하지_않은_값이_넘어온_경우() {
        // given
        val invalidHeader = "invalid_token"

        val request = MockServerHttpRequest.get("").header(AUTHORIZATION, invalidHeader).build()
        val exchange = MockServerWebExchange.from(request)

        // when
        `when`(jwtProvider.substringToken(invalidHeader)).thenReturn(null)

        // then
        assertThrows(
            JwtException::class.java,
            { filter.apply(null).filter(exchange, chain).block() },
            INVALID_TOKEN
        )
    }

    @Test
    fun Authorization_헤더에_만료된_토큰값이_넘어온_경우() {
        // given
        val expiredToken = "expired_token"
        val authHeader = "Bearer $expiredToken"

        val request = MockServerHttpRequest.get("").header(AUTHORIZATION, authHeader).build()
        val exchange = MockServerWebExchange.from(request)

        // when
        `when`(jwtProvider.substringToken(authHeader)).thenReturn(expiredToken)
        `when`(jwtProvider.validateToken(expiredToken)).thenReturn(Result.failure(JwtException(anyString())))

        // then
        assertThrows(
            JwtException::class.java,
            { filter.apply(null).filter(exchange, chain).block() },
            EXPIRED_ATK
        )
    }

    @Test
    fun Authorization_헤더에_로그아웃된_토큰값이_넘어온_경우() {
        // given
        val blockedToken = "blocked_token"
        val authHeader = "Bearer $blockedToken"

        val request = MockServerHttpRequest.get("").header(AUTHORIZATION, authHeader).build()
        val exchange = MockServerWebExchange.from(request)

        val blockedAtkKey = "test-blocked_atk"

        // when
        `when`(jwtProvider.substringToken(authHeader)).thenReturn(blockedToken)
        `when`(jwtProvider.validateToken(blockedToken)).thenReturn(Result.success(any()))
        `when`(redisKeyProvider.blockedAtk(blockedToken)).thenReturn(blockedAtkKey)
        `when`(redisCommands.get(blockedAtkKey)).thenReturn(anyString())

        // then
        assertThrows(
            JwtException::class.java,
            { filter.apply(null).filter(exchange, chain).block() },
            EXPIRED_AUTH
        )
    }
}