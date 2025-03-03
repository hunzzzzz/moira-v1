package com.hunzz.api.service

import com.hunzz.api.component.UserRedisHandler
import com.hunzz.api.dto.response.RelationInfo
import com.hunzz.api.dto.response.UserResponse
import com.hunzz.common.cache.UserCacheManager
import com.hunzz.common.model.cache.UserInfo
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.springframework.stereotype.Service
import java.util.*

@Service
class ProfileService(
    private val userCacheManager: UserCacheManager,
    private val userRedisHandler: UserRedisHandler
) {
    fun getProfile(userId: UUID, targetId: UUID) = runBlocking {
        val (userInfo, relationInfo) = withTimeout(5_000) {
            // 유저 정보 조회
            val job1 = async {
                userCacheManager.getWithLocalCache(userId = targetId)
            }
            // 팔로잉/팔로워 수 조회
            val job2 = async {
                userRedisHandler.getRelationInfo(userId = targetId)
            }
            awaitAll(job1, job2)
        }

        userInfo as UserInfo
        relationInfo as RelationInfo

        // 데이터 병합 후 리턴
        UserResponse(
            id = userInfo.userId,
            status = userInfo.status,
            name = userInfo.name,
            imageUrl = userInfo.imageUrl,
            thumbnailUrl = userInfo.thumbnailUrl,
            numOfFollowings = relationInfo.numOfFollowings,
            numOfFollowers = relationInfo.numOfFollowers,
            isMyProfile = userId == targetId
        )
    }
}