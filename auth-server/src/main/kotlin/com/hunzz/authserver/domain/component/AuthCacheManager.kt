package com.hunzz.authserver.domain.component

import com.hunzz.authserver.utility.auth.UserAuth
import com.hunzz.authserver.utility.client.UserServerClient
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Component

@Component
class AuthCacheManager(
    private val userServerClient: UserServerClient
) {
    fun getUserAuth(email: String): UserAuth {
        return userServerClient.getUserAuth(email = email)
    }

    @Cacheable(cacheNames = ["auth"], key = "#email", cacheManager = "redisCacheManager")
    fun getUserAuthWithRedisCache(email: String): UserAuth {
        return getUserAuth(email = email)
    }

    @Cacheable(cacheNames = ["auth"], key = "#email", cacheManager = "localCacheManager")
    fun getUserAuthWithLocalCache(email: String): UserAuth {
        return getUserAuthWithRedisCache(email = email)
    }
}