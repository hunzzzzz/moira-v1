package com.hunzz.api.auth

import org.springframework.core.MethodParameter
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import java.util.*

@Component
class AuthPrincipalArgumentResolver(
    private val jwtProvider: JwtProvider
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
        // Authorization 헤더에서 ATK 추출
        val authHeader = webRequest.getHeader(HttpHeaders.AUTHORIZATION)!!
        val atk = jwtProvider.substringToken(token = authHeader)

        // ATK에서 userId 추출
        val payload = jwtProvider.getUserInfoFromToken(token = atk!!)
        val userId = UUID.fromString(payload.subject)

        return userId
    }
}