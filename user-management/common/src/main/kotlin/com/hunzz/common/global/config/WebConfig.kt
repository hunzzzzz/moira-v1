package com.hunzz.common.global.config

import com.hunzz.common.global.aop.auth.AuthPrincipalArgumentResolver
import com.hunzz.common.global.utility.AuthCacheManager
import com.hunzz.common.global.utility.JwtProvider
import org.springframework.context.annotation.Configuration
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig(
    private val authCacheManager: AuthCacheManager,
    private val jwtProvider: JwtProvider,
) : WebMvcConfigurer {
    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(
            AuthPrincipalArgumentResolver(
                authCacheManager = authCacheManager,
                jwtProvider = jwtProvider
            )
        )
    }
}