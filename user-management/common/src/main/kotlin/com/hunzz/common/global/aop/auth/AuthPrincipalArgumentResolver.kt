package com.hunzz.common.global.aop.auth

import com.hunzz.common.global.utility.JwtProvider
import com.hunzz.common.global.utility.UserAuthProvider
import org.springframework.core.MethodParameter
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

@Component
class AuthPrincipalArgumentResolver(
    private val jwtProvider: JwtProvider,
    private val userAuthProvider: UserAuthProvider
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

        println(atk)

        // get user info from atk
        val payload = jwtProvider.getUserInfoFromToken(token = atk!!)
        val email = payload.get("email", String::class.java)

        println(email)
        // get user auth from redis
        val userAuth = userAuthProvider.getUserAuthWithLocalCache(email = email)

        println(userAuth)
        return userAuth
    }
}