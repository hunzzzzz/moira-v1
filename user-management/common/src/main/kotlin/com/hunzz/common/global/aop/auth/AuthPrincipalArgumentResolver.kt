package com.hunzz.common.global.aop.auth

import com.hunzz.common.global.utility.AuthCacheManager
import com.hunzz.common.global.utility.JwtProvider
import org.springframework.core.MethodParameter
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

@Component
class AuthPrincipalArgumentResolver(
    private val authCacheManager: AuthCacheManager,
    private val jwtProvider: JwtProvider,
) : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.hasParameterAnnotation(AuthPrincipal::class.java)
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): Any? {
        // substring atk
        val authHeader = webRequest.getHeader(HttpHeaders.AUTHORIZATION)
        val atk = jwtProvider.substringToken(token = authHeader)

        // get user info from atk
        val payload = jwtProvider.getUserInfoFromToken(token = atk!!)
        val email = payload.get("email", String::class.java)

        // get user auth from redis
        val userAuth = authCacheManager.getUserAuthWithLocalCache(email = email)

        return userAuth
    }
}