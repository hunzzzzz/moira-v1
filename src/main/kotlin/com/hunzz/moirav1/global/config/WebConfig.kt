package com.hunzz.moirav1.global.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.hunzz.moirav1.global.aop.auth.AuthPrincipalArgumentResolver
import com.hunzz.moirav1.global.filter.AuthCheckFilter
import com.hunzz.moirav1.global.utility.JwtProvider
import com.hunzz.moirav1.global.utility.RedisCommands
import com.hunzz.moirav1.global.utility.RedisKeyProvider
import com.hunzz.moirav1.global.utility.UserAuthProvider
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig(
    private val jwtProvider: JwtProvider,
    private val objectMapper: ObjectMapper,
    private val redisCommands: RedisCommands,
    private val redisKeyProvider: RedisKeyProvider,
    private val userAuthProvider: UserAuthProvider
) : WebMvcConfigurer {
    @Bean
    fun addAuthFilter(): FilterRegistrationBean<AuthCheckFilter> {
        val filter = FilterRegistrationBean(
            AuthCheckFilter(
                jwtProvider = jwtProvider,
                objectMapper = objectMapper,
                redisCommands = redisCommands,
                redisKeyProvider = redisKeyProvider
            )
        ).apply {
            this.urlPatterns = listOf("/users/*", "/logout")
            this.order = Ordered.HIGHEST_PRECEDENCE
        }

        return filter
    }

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(
            AuthPrincipalArgumentResolver(
                jwtProvider = jwtProvider,
                userAuthProvider = userAuthProvider
            )
        )
    }
}