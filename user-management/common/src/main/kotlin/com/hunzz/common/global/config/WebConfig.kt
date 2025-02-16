package com.hunzz.common.global.config

import com.hunzz.common.global.aop.auth.AuthPrincipalArgumentResolver
import com.hunzz.common.global.utility.JwtProvider
import com.hunzz.common.global.utility.UserAuthProvider
import org.springframework.context.annotation.Configuration
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig(
    private val jwtProvider: JwtProvider,
    private val userAuthProvider: UserAuthProvider
) : WebMvcConfigurer {
    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(
            AuthPrincipalArgumentResolver(
                jwtProvider = jwtProvider,
                userAuthProvider = userAuthProvider
            )
        )
    }
}