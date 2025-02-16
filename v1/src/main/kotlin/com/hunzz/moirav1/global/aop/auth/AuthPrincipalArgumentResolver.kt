package com.hunzz.moirav1.global.aop.auth

import com.hunzz.moirav1.global.utility.JwtProvider
import com.hunzz.moirav1.global.utility.UserAuthProvider
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
        val authHeader = webRequest.getHeader(HttpHeaders.AUTHORIZATION)
        val atk = jwtProvider.substringToken(token = authHeader)

        // get user auth
        val payload = jwtProvider.getUserInfoFromToken(token = atk!!)
        val email = payload.get("email", String::class.java)
        val userAuth = userAuthProvider.getUserAuthFromRedis(email = email)

        return userAuth
    }
}