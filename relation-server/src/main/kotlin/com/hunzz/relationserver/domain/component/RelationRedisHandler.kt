package com.hunzz.relationserver.domain.component

import com.fasterxml.jackson.databind.ObjectMapper
import com.hunzz.relationserver.domain.dto.response.FollowResponse
import com.hunzz.relationserver.domain.model.RelationId
import com.hunzz.relationserver.domain.model.RelationType
import com.hunzz.relationserver.utility.client.UserServerClient
import com.hunzz.relationserver.utility.exception.ErrorCode.*
import com.hunzz.relationserver.utility.exception.custom.InvalidRelationException
import com.hunzz.relationserver.utility.redis.RedisKeyProvider
import com.hunzz.relationserver.utility.redis.RedisScriptProvider
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.script.RedisScript
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

@Component
class RelationRedisHandler(
    private val objectMapper: ObjectMapper,
    private val redisKeyProvider: RedisKeyProvider,
    private val redisScriptProvider: RedisScriptProvider,
    private val redisTemplate: RedisTemplate<String, String>,
    private val userServerClient: UserServerClient
) {
    fun checkFollowRequest(userId: UUID, targetId: UUID) {
        // 세팅
        val followingKey = redisKeyProvider.following(userId = userId)

        // 검증
        if (userId == targetId)
            throw InvalidRelationException(CANNOT_FOLLOW_ITSELF)
        if (redisTemplate.opsForZSet().score(followingKey, targetId.toString()) != null)
            throw InvalidRelationException(ALREADY_FOLLOWED)
    }

    fun checkUnfollowRequest(userId: UUID, targetId: UUID) {
        // 세팅
        val followingKey = redisKeyProvider.following(userId = userId)

        // 검증
        if (userId == targetId)
            throw InvalidRelationException(CANNOT_UNFOLLOW_ITSELF)
        if (redisTemplate.opsForZSet().score(followingKey, targetId) == null)
            throw InvalidRelationException(ALREADY_UNFOLLOWED)
    }

    fun follow(userId: UUID, targetId: UUID) {
        // 세팅
        val script = redisScriptProvider.follow()
        val followingKey = redisKeyProvider.following(userId = userId)
        val followerKey = redisKeyProvider.follower(userId = targetId)
        val followQueueKey = redisKeyProvider.followQueue()

        // 스크립트 실행
        redisTemplate.execute(
            RedisScript.of(script, String::class.java),
            listOf(followingKey, followerKey, followQueueKey),
            userId.toString(),
            targetId.toString(),
            System.currentTimeMillis().toString(),
            RelationId(userId = userId, targetId = targetId)
                .let { objectMapper.writeValueAsString(it) }
        )
    }

    fun unfollow(userId: UUID, targetId: UUID) {
        // 세팅
        val script = redisScriptProvider.unfollow()
        val followingKey = redisKeyProvider.following(userId = userId)
        val followerKey = redisKeyProvider.follower(userId = targetId)
        val unfollowQueueKey = redisKeyProvider.unfollowQueue()

        // 스크립트 실행
        redisTemplate.execute(
            RedisScript.of(script, String::class.java),
            listOf(followingKey, followerKey, unfollowQueueKey),
            userId.toString(),
            targetId.toString(),
            RelationId(userId = userId, targetId = targetId)
                .let { objectMapper.writeValueAsString(it) }
        )
    }

    fun checkFollowQueue(): List<RelationId> {
        // 세팅
        val script = redisScriptProvider.checkFollowQueue()
        val followQueueKey = redisKeyProvider.followQueue()

        // 스크립트 실행
        val result = redisTemplate.execute(
            RedisScript.of(script, List::class.java),
            listOf(followQueueKey)
        )

        // RelationId로 변환
        val relations = result
            .map { objectMapper.readValue(it.toString(), RelationId::class.java) }

        return relations
    }

    fun checkUnfollowQueue(): List<RelationId> {
        // 세팅
        val script = redisScriptProvider.checkUnfollowQueue()
        val unfollowQueueKey = redisKeyProvider.unfollowQueue()

        // 스크립트 실행
        val result = redisTemplate.execute(
            RedisScript.of(script, List::class.java),
            listOf(unfollowQueueKey)
        )

        // RelationId로 변환
        val relationIds = result
            .map { objectMapper.readValue(it.toString(), RelationId::class.java) }

        return relationIds
    }

    fun getRelations(userId: UUID, cursor: UUID?, type: RelationType, pageSize: Long): List<FollowResponse?> {
        // 세팅
        val script = redisScriptProvider.relations()
        val key = when (type) {
            RelationType.FOLLOWING -> redisKeyProvider.following(userId = userId)
            RelationType.FOLLOWER -> redisKeyProvider.follower(userId = userId)
        }

        // 스크립트 실행
        val result = redisTemplate.execute(
            RedisScript.of(script, List::class.java),
            listOf(key),
            cursor?.toString() ?: "",
            pageSize.toString(),
            LocalDateTime.now().atZone(ZoneId.systemDefault())
                .toInstant().toEpochMilli().toDouble().toString()
        )

        // 한 페이지 안에서 유저 캐시 정보가 없는 userId만 추출
        val missingIds = result.filterIsInstance<String>()
            .filter { it.startsWith("NULL:") }
            .map { UUID.fromString(it.substring(5)) }
        var missingUserInfos = hashMapOf<UUID, FollowResponse>()

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

        // FollowResponse 목록을 리턴
        val followResponses = result.filterIsInstance<String>()
            .map {
                if (it.startsWith("NULL:")) {
                    val id = UUID.fromString(it.substring(5))

                    missingUserInfos[id]
                } else objectMapper.readValue(it, FollowResponse::class.java)
            }

        return followResponses
    }
}