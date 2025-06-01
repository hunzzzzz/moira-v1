package com.hunzz.cache.service

import com.hunzz.common.cache.UserCacheManager
import com.hunzz.common.model.cache.UserInfo
import org.springframework.stereotype.Service
import java.util.*

@Service
class GetCachedUserService(
    private val userCacheManager: UserCacheManager
) {
    fun getUsers(missingIds: List<UUID>): HashMap<UUID, UserInfo> {
        val hashMap = hashMapOf<UUID, UserInfo>()

        missingIds.forEach {
            hashMap[it] = userCacheManager.getWithLocalCache(userId = it)
        }

        return hashMap
    }
}