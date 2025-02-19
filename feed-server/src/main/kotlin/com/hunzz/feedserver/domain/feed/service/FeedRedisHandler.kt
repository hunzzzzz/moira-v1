package com.hunzz.feedserver.domain.feed.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.hunzz.feedserver.domain.feed.dto.response.FeedLikeResponse
import com.hunzz.feedserver.global.client.UserServerClient
import com.hunzz.feedserver.global.model.CachedUser
import com.hunzz.feedserver.global.utility.RedisKeyProvider
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.script.RedisScript
import org.springframework.stereotype.Component
import java.util.*

@Component
class FeedRedisHandler(
    private val objectMapper: ObjectMapper,
    private val redisKeyProvider: RedisKeyProvider,
    private val redisTemplate: RedisTemplate<String, String>,
    private val userServerClient: UserServerClient
) {
    fun getFollowers(authorId: UUID): MutableSet<String> {
        val followerKey = redisKeyProvider.follower(userId = authorId)

        return redisTemplate.opsForZSet().range(followerKey, 0, -1) ?: mutableSetOf()
    }

    fun getLatestPostIds(authorId: UUID): MutableSet<String> {
        val latestPostKey = redisKeyProvider.latestPosts(userId = authorId)

        return redisTemplate.opsForZSet().range(latestPostKey, 0, -1) ?: mutableSetOf()
    }

    fun getUserInfo(userIds: List<UUID>): List<CachedUser> {
        val script = """
            local user_ids = cjson.decode(ARGV[1])
            local keys = {}
            
            -- KEY 배열 생성
            for i, user_id in ipairs(user_ids) do
                keys[i] = 'user:' .. user_id
            end
            
            -- MGET으로 일괄 조회
            local user_infos = redis.call('MGET', unpack(keys))
            
            -- 누락된 데이터 처리
            local result = {}
            for i, info in ipairs(user_infos) do
                if info == false then
                    result[i] = 'NULL:' .. user_ids[i]
                else
                    result[i] = info
                end
            end
            
            return result
        """.trimIndent()

        var retryCount = 0
        val maxRetries = 3

        var result: List<*> = listOf<Any>()
        var missingInfos = hashMapOf<UUID, CachedUser>()

        while (retryCount < maxRetries) {
            try {
                // execute script
                result = redisTemplate.execute(
                    RedisScript.of(script, List::class.java), // script
                    listOf(), // keys
                    objectMapper.writeValueAsString(userIds) // argv[1]
                )

                // if there's no cache in redis, get follow info from user-server
                val missingIds = result.filterIsInstance<String>()
                    .filter { it.startsWith("NULL:") }
                    .map { UUID.fromString(it.substring(5)) }

                if (missingIds.isNotEmpty())
                    missingInfos = userServerClient.getUsers(missingIds = missingIds)

                break
            } catch (e: Exception) {
                retryCount++
                if (retryCount == maxRetries) throw e

                Thread.sleep(1000)
            }
        }

        // return result
        val users = result.filterIsInstance<String>()
            .mapNotNull {
                if (it.startsWith("NULL:")) {
                    val id = UUID.fromString(it.substring(5))

                    missingInfos[id]
                } else objectMapper.readValue(it, CachedUser::class.java)
            }

        return users
    }

    fun getLikeInfo(userId: UUID, postIds: List<Long>): List<FeedLikeResponse> {
        val script = """
            local like_key = KEYS[1]
            local like_count_key = KEYS[2]
            local post_ids = cjson.decode(ARGV[1])
            
            local result = {}
            
            -- LIKE 데이터 조회
            for i, post_id in ipairs(post_ids) do
                local likes = redis.call('ZSCORE', like_count_key, post_id)
                local has_like = redis.call('ZSCORE', like_key, post_id) ~= false
                
                result[i] = {likes, has_like}
            end
            
            return result
        """.trimIndent()

        val likeKey = redisKeyProvider.like(userId = userId)
        val likeCountKey = redisKeyProvider.likeCount()

        // execute script
        val result = redisTemplate.execute(
            RedisScript.of(script, List::class.java), // script
            listOf(likeKey, likeCountKey), // keys
            objectMapper.writeValueAsString(postIds) // argv[1]
        )

        return result.map {
            it as List<*>
            val likes = (it[0] as Long?) ?: 0L
            val hasLike = (it[1] as Boolean?) ?: false

            FeedLikeResponse(likes = likes, hasLike = hasLike)
        }
    }
}