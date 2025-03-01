package com.hunzz.userserver.client

import com.hunzz.common.domain.user.model.CachedUser
import com.hunzz.common.domain.user.model.UserAuth
import com.hunzz.common.domain.user.repository.KakaoUserRepository
import com.hunzz.common.domain.user.repository.UserRepository
import com.hunzz.common.global.exception.ErrorCode.USER_NOT_FOUND
import com.hunzz.common.global.exception.custom.InvalidUserInfoException
import com.hunzz.userserver.utility.UserCacheManager
import org.springframework.stereotype.Service
import java.util.*

@Service
class UserPrivateService(
    private val kakaoUserRepository: KakaoUserRepository,
    private val userCacheManager: UserCacheManager,
    private val userRepository: UserRepository
) {
    fun get(userId: UUID): CachedUser {
        return userCacheManager.getWithLocalCache(userId = userId)
    }

    fun getAll(missingIds: List<UUID>): HashMap<UUID, CachedUser> {
        val hashMap = hashMapOf<UUID, CachedUser>()

        missingIds.forEach {
            hashMap[it] = userCacheManager.getWithLocalCache(userId = it)
        }

        return hashMap
    }

    fun getUserAuth(email: String): UserAuth {
        val userAth = kakaoUserRepository.findUserAuth(email = email)
            ?: userRepository.findUserAuth(email = email)
            ?: throw InvalidUserInfoException(USER_NOT_FOUND)

        return userAth
    }
}