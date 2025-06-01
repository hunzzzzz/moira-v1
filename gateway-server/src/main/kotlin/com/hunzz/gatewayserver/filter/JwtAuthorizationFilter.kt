package com.hunzz.gatewayserver.filter

import com.hunzz.gatewayserver.exception.ErrorMessages.EXPIRED_ATK
import com.hunzz.gatewayserver.exception.ErrorMessages.EXPIRED_AUTH
import com.hunzz.gatewayserver.exception.ErrorMessages.INVALID_TOKEN
import com.hunzz.gatewayserver.exception.ErrorMessages.UNPACKED_ATK
import com.hunzz.gatewayserver.exception.custom.JwtException
import com.hunzz.gatewayserver.utility.JwtProvider
import com.hunzz.gatewayserver.utility.RedisCommands
import com.hunzz.gatewayserver.utility.RedisKeyProvider
import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.stereotype.Component
import java.util.function.Consumer

@Component
class JwtAuthorizationFilter(
    private val jwtProvider: JwtProvider,
    private val redisKeyProvider: RedisKeyProvider,
    private val redisCommands: RedisCommands
) : AbstractGatewayFilterFactory<JwtAuthorizationFilter.Config>(Config::class.java) {
    class Config

    override fun apply(config: Config?): GatewayFilter =
        GatewayFilter { exchange, chain ->
            val request = exchange.request
            val response = exchange.response

            // Authorization 헤더에서 ATK 추출
            val authHeader = request.headers.getFirst(AUTHORIZATION)
                ?: throw JwtException(UNPACKED_ATK)
            val atk = jwtProvider.substringToken(token = authHeader)
                ?: throw JwtException(INVALID_TOKEN)

            // ATK 검증
            jwtProvider.validateToken(token = atk).onFailure {
                throw JwtException(EXPIRED_ATK)
            }

            // (로그아웃으로 인한) 차단된 ATK인지 검증
            val blockedAtkKey = redisKeyProvider.blockedAtk(atk = atk)
            val isBlocked = redisCommands.get(key = blockedAtkKey) != null

            if (isBlocked) throw JwtException(EXPIRED_AUTH)

            chain.filter(exchange)
        }

    override fun apply(consumer: Consumer<Config>): GatewayFilter {
        val config = Config()
        consumer.accept(config)

        return this.apply(config)
    }
}