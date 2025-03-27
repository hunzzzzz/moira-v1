package com.hunzz.authserver.domain.component

import com.hunzz.authserver.utility.auth.UserAuth
import kotlinx.coroutines.reactor.awaitSingle
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.util.retry.Retry
import java.time.Duration

@Component
class UserAuthProvider(
    webClientBuilder: WebClient.Builder
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    private val userCacheServer = webClientBuilder
        .baseUrl("http://localhost:8080/user-cache/private")
        .build()

    suspend fun getUserAuth(email: String): UserAuth {
        return userCacheServer.get()
            .uri {
                it.path("/user/auth")
                    .queryParam("email", email)
                    .build()
            }
            .retrieve()
            .bodyToMono(UserAuth::class.java)
            .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))) // 1초 간격, 3회 재시도
            .doOnError { logger.error("[에러] user-cache 서버로부터 UserAuth를 가져오는 과정에서 에러가 발생하였습니다.", it) }
            .awaitSingle()
    }
}