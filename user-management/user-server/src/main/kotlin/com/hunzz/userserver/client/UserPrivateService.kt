package com.hunzz.userserver.client

import com.hunzz.common.domain.user.model.CachedUser
import com.hunzz.userserver.utility.UserCacheManager
import org.springframework.stereotype.Service
import java.util.*

@Service
class UserPrivateService(
    private val userCacheManager: com.hunzz.userserver.utility.UserCacheManager
) {
    fun getAll(missingIds: List<UUID>): HashMap<UUID, CachedUser> {
        val hashMap = hashMapOf<UUID, CachedUser>()

        missingIds.forEach {
            hashMap[it] = userCacheManager.getWithLocalCache(userId = it)
        }

        return hashMap
    }
}