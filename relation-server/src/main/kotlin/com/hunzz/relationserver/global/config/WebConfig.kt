package com.hunzz.relationserver.global.config

import com.hunzz.relationserver.global.aop.auth.AuthPrincipalArgumentResolver
import com.hunzz.relationserver.global.utility.JwtProvider
import org.springframework.context.annotation.Configuration
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig(
    private val jwtProvider: JwtProvider,
) : WebMvcConfigurer {
    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(
            AuthPrincipalArgumentResolver(
                jwtProvider = jwtProvider
            )
        )
    }
}