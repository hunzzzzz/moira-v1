package com.hunzz.cache.service

import com.hunzz.common.cache.UserCacheManager
import com.hunzz.common.exception.ErrorCode.USER_NOT_FOUND
import com.hunzz.common.exception.custom.InvalidUserInfoException
import com.hunzz.common.model.cache.UserAuth
import com.hunzz.common.model.cache.UserInfo
import com.hunzz.common.repository.KakaoUserRepository
import com.hunzz.common.repository.NaverUserRepository
import com.hunzz.common.repository.UserRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class CacheService(
    private val kakaoUserRepository: KakaoUserRepository,
    private val naverUserRepository: NaverUserRepository,
    private val userCacheManager: UserCacheManager,
    private val userRepository: UserRepository
) {
    fun getUserAuth(email: String): UserAuth {
        val userAth = kakaoUserRepository.findUserAuth(email = email)
            ?: naverUserRepository.findUserAuth(email = email)
            ?: userRepository.findUserAuth(email = email)
            ?: throw InvalidUserInfoException(USER_NOT_FOUND)

        return userAth
    }

    fun getUsers(missingIds: List<UUID>): HashMap<UUID, UserInfo> {
        val hashMap = hashMapOf<UUID, UserInfo>()

        missingIds.forEach {
            hashMap[it] = userCacheManager.getWithLocalCache(userId = it)
        }

        return hashMap
    }
}