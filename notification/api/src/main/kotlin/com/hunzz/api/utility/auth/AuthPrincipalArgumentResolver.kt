package com.hunzz.api.utility.auth

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
        val authHeader = webRequest.getHeader(HttpHeaders.AUTHORIZATION)!!

        return if (authHeader.startsWith("Locust "))
            authHeader.substring("Locust ".length)
        else {
            val atk = jwtProvider.substringToken(token = authHeader)
            val payload = jwtProvider.getUserInfoFromToken(token = atk!!)

            payload.subject.toString()
        }
    }
}