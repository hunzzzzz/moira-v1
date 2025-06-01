package com.hunzz.api.component

import com.fasterxml.jackson.databind.ObjectMapper
import com.hunzz.api.client.PostServerClient
import com.hunzz.api.client.UserServerClient
import com.hunzz.api.client.dto.PostInfo
import com.hunzz.api.client.dto.UserInfo
import com.hunzz.api.dto.response.FeedLikeResponse
import com.hunzz.common.redis.RedisKeyProvider
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.script.RedisScript
import org.springframework.stereotype.Component
import java.util.*

@Component
class FeedRedisHandler(
    private val feedRedisScriptProvider: FeedRedisScriptProvider,
    private val objectMapper: ObjectMapper,
    private val postServerClient: PostServerClient,
    private val redisKeyProvider: RedisKeyProvider,
    private val redisTemplate: RedisTemplate<String, String>,
    private val userServerClient: UserServerClient
) {
    fun getUserInfos(userIds: List<UUID>): List<UserInfo> {
        // 세팅
        val script = feedRedisScriptProvider.getUserInfos()

        // 스크립트 실행
        val result = redisTemplate.execute(
            RedisScript.of(script, List::class.java),
            listOf(),
            objectMapper.writeValueAsString(userIds)
        )

        // 유저 캐시 정보가 없는 userId만 추출
        val missingIds = result.filterIsInstance<String>()
            .filter { it.startsWith("NULL:") }
            .map { UUID.fromString(it.substring(5)) }
            .distinct()
        var missingUserInfos = hashMapOf<UUID, UserInfo>()

        // 캐시 정보가 없는 유저들의 id 리스트를 user-cache 서버로 전송하여 유저 정보 조회
        // 네트워크 에러를 대비하여 최대 3번 요청을 보낸다.
        if (missingIds.isNotEmpty()) {
            var retryCount = 0
            val maxRetries = 3

            while (retryCount < maxRetries) {
                try {
                    missingUserInfos = userServerClient.getUsers(missingIds = missingIds)

                    break
                } catch (e: Exception) {
                    retryCount++
                    if (retryCount == maxRetries) throw e

                    Thread.sleep(1000)
                }
            }
        }

        // UserInfo 리스트를 리턴
        val userInfos = result.filterIsInstance<String>()
            .mapNotNull {
                if (it.startsWith("NULL:")) {
                    val userId = UUID.fromString(it.substring(5))

                    missingUserInfos[userId]
                } else objectMapper.readValue(it, UserInfo::class.java)
            }

        return userInfos
    }

    fun getPostInfos(postIds: List<UUID>): List<PostInfo> {
        // 세팅
        val script = feedRedisScriptProvider.getPostInfos()

        // 스크립트 실행
        val result = redisTemplate.execute(
            RedisScript.of(script, List::class.java),
            listOf(),
            objectMapper.writeValueAsString(postIds)
        )

        // 게시글 캐시 정보가 없는 postId만 추출
        val missingIds = result.filterIsInstance<String>()
            .filter { it.startsWith("NULL:") }
            .map { UUID.fromString(it.substring(5)) }
        var missingPostInfos = hashMapOf<UUID, PostInfo>()

        // 캐시 정보가 없는 게시글의 id 리스트를 post-cache 서버로 전송하여 게시글 정보 조회
        // 네트워크 에러를 대비하여 최대 3번 요청을 보낸다.
        if (missingIds.isNotEmpty()) {
            var retryCount = 0
            val maxRetries = 3

            while (retryCount < maxRetries) {
                try {
                    missingPostInfos = postServerClient.getPosts(missingIds = missingIds)

                    break
                } catch (e: Exception) {
                    retryCount++
                    if (retryCount == maxRetries) throw e

                    Thread.sleep(1000)
                }
            }
        }

        // PostInfo 리스트를 리턴
        val postInfos = result.filterIsInstance<String>()
            .mapNotNull {
                if (it.startsWith("NULL:")) {
                    val postId = UUID.fromString(it.substring(5))

                    missingPostInfos[postId]
                } else objectMapper.readValue(it, PostInfo::class.java)
            }

        return postInfos
    }

    fun getLikeInfos(userId: UUID, postIds: List<UUID>): List<FeedLikeResponse> {
        // 세팅
        val script = feedRedisScriptProvider.getLikeInfos()
        val likeKey = redisKeyProvider.like(userId = userId)
        val likeCountKey = redisKeyProvider.likeCount()

        // 스크립트 실행
        val result = redisTemplate.execute(
            RedisScript.of(script, List::class.java),
            listOf(likeKey, likeCountKey),
            objectMapper.writeValueAsString(postIds)
        )

        return result.map { objectMapper.readValue(it as String, FeedLikeResponse::class.java) }
    }

    fun readFeed(feedIds: List<Long>) {
        // 세팅
        val script = feedRedisScriptProvider.readFeed()
        val feedReadQueueKey = redisKeyProvider.feedReadQueue()

        // 스크립트 실행
        redisTemplate.execute(
            RedisScript.of(script, String::class.java),
            listOf(feedReadQueueKey),
            objectMapper.writeValueAsString(feedIds),
            (System.currentTimeMillis() + 1000 * 60 * 30).toString() // 30분 뒤
        )
    }
}