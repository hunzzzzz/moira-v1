package com.hunzz.gatewayservice.utility

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtProvider(
    @Value("\${jwt.secret.key}")
    private val secretKey: String
) {
    private val key: SecretKey =
        Base64.getDecoder().decode(secretKey).let { Keys.hmacShaKeyFor(it) }

    fun substringToken(token: String?) =
        if (!token.isNullOrBlank() && token.startsWith("Bearer "))
            token.substring("Bearer ".length)
        else null

    fun validateToken(token: String) =
        kotlin.runCatching { Jwts.parser().verifyWith(key).build().parseSignedClaims(token) }
}