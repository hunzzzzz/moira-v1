package com.hunzz.authserver.utility.auth

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtProvider(
    @Value("\${jwt.secret.key}")
    private val secretKey: String,

    @Value("\${jwt.issuer}")
    private val issuer: String,

    @Value("\${jwt.expiration-time.atk}")
    private val expirationTimeOfAtk: Int,

    @Value("\${jwt.expiration-time.rtk}")
    private val expirationTimeOfRtk: Int
) {
    private val key: SecretKey =
        Base64.getDecoder().decode(secretKey).let { Keys.hmacShaKeyFor(it) }

    private fun createToken(userAuth: UserAuth, expirationTime: Int) =
        Jwts.builder().let {
            it.subject(userAuth.userId.toString())
            it.claims(
                Jwts.claims().add(
                    mapOf("role" to userAuth.role, "email" to userAuth.email)
                ).build()
            )
            it.expiration(Date(Date().time + expirationTime))
            it.issuedAt(Date())
            it.issuer(issuer)
            it.signWith(key)
            it.compact()
        }.let { jwt -> "Bearer $jwt" }

    fun createAccessToken(userAuth: UserAuth) =
        createToken(userAuth = userAuth, expirationTime = expirationTimeOfAtk)

    fun createRefreshToken(userAuth: UserAuth) =
        createToken(userAuth = userAuth, expirationTime = expirationTimeOfRtk)

    fun substringToken(token: String?) =
        if (!token.isNullOrBlank() && token.startsWith("Bearer "))
            token.substring("Bearer ".length)
        else null

    fun validateToken(token: String) =
        kotlin.runCatching { Jwts.parser().verifyWith(key).build().parseSignedClaims(token) }

    fun getUserInfoFromToken(token: String): Claims =
        Jwts.parser().verifyWith(key).build().parseSignedClaims(token).payload
}