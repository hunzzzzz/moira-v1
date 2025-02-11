package com.hunzz.gatewayservice.filter

import com.hunzz.gatewayservice.exception.ErrorMessages.EXPIRED_ATK
import com.hunzz.gatewayservice.exception.ErrorMessages.EXPIRED_AUTH
import com.hunzz.gatewayservice.exception.ErrorMessages.INVALID_TOKEN
import com.hunzz.gatewayservice.exception.ErrorMessages.UNPACKED_ATK
import com.hunzz.gatewayservice.exception.custom.JwtException
import com.hunzz.gatewayservice.utility.JwtProvider
import com.hunzz.gatewayservice.utility.RedisCommands
import com.hunzz.gatewayservice.utility.RedisKeyProvider
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

            // check 'Authorization' header
            val authHeader = request.headers.getFirst(AUTHORIZATION)
                ?: throw JwtException(UNPACKED_ATK)

            // substring
            val atk = jwtProvider.substringToken(token = authHeader)
                ?: throw JwtException(INVALID_TOKEN)

            // validate
            jwtProvider.validateToken(token = atk).onFailure {
                throw JwtException(EXPIRED_ATK)
            }

            // check atk blacklist
            val blockedAtkKey = redisKeyProvider.blockedAtk(atk = atk)
            val isNotBlocked = redisCommands.get(key = blockedAtkKey) == null

            require(isNotBlocked) { throw JwtException(EXPIRED_AUTH) }

            chain.filter(exchange)
        }

    override fun apply(consumer: Consumer<Config>): GatewayFilter {
        val config = Config()
        consumer.accept(config)

        return this.apply(config)
    }
}