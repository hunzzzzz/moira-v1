package com.hunzz.relationserver.domain.relation.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.hunzz.relationserver.domain.relation.dto.response.FollowResponse
import com.hunzz.relationserver.domain.relation.model.Relation
import com.hunzz.relationserver.domain.relation.model.RelationId
import com.hunzz.relationserver.domain.relation.model.RelationType
import com.hunzz.relationserver.global.client.UserServiceClient
import com.hunzz.relationserver.global.exception.ErrorCode.*
import com.hunzz.relationserver.global.exception.custom.InvalidRelationException
import com.hunzz.relationserver.global.utility.RedisKeyProvider
import com.hunzz.relationserver.global.utility.RedisScriptProvider
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.script.RedisScript
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

@Component
class RelationRedisScriptHandler(
    private val objectMapper: ObjectMapper,
    private val redisKeyProvider: RedisKeyProvider,
    private val redisScriptProvider: RedisScriptProvider,
    private val redisTemplate: RedisTemplate<String, String>,
    private val userServiceClient: UserServiceClient
) {
    fun checkFollowRequest(userId: UUID, targetId: UUID) {
        // settings
        val script = redisScriptProvider.checkFollowRequest()
        val idsKey = redisKeyProvider.ids()
        val followingKey = redisKeyProvider.following(userId = userId)

        // execute 'validation' script
        val result = redisTemplate.execute(
            RedisScript.of(script, String::class.java), // script
            listOf(idsKey, followingKey), // keys
            targetId.toString() // argv[1]
        )

        // check result
        when (result) {
            FOLLOWING_NOT_EXISTING_USER.name -> throw InvalidRelationException(FOLLOWING_NOT_EXISTING_USER)
            ALREADY_FOLLOWED.name -> throw InvalidRelationException(ALREADY_FOLLOWED)
            else -> return
        }
    }

    fun checkUnfollowRequest(userId: UUID, targetId: UUID) {
        // settings
        val script = redisScriptProvider.checkUnfollowRequest()
        val idsKey = redisKeyProvider.ids()
        val followingKey = redisKeyProvider.following(userId = userId)

        // execute 'validation' script
        val result = redisTemplate.execute(
            RedisScript.of(script, String::class.java), // script
            listOf(idsKey, followingKey), // keys
            targetId.toString() // argv[1]
        )

        // check result
        when (result) {
            FOLLOWING_NOT_EXISTING_USER.name -> throw InvalidRelationException(FOLLOWING_NOT_EXISTING_USER)
            ALREADY_UNFOLLOWED.name -> throw InvalidRelationException(ALREADY_UNFOLLOWED)
            else -> return
        }
    }

    fun follow(userId: UUID, targetId: UUID) {
        // settings
        val script = redisScriptProvider.follow()
        val followingKey = redisKeyProvider.following(userId = userId)
        val followerKey = redisKeyProvider.follower(userId = targetId)
        val followQueueKey = redisKeyProvider.followQueue()

        // execute script
        redisTemplate.execute(
            RedisScript.of(script, String::class.java), // script
            listOf(followingKey, followerKey, followQueueKey), // keys
            userId.toString(), // argv[1]
            targetId.toString(), // argv[2]
            System.currentTimeMillis().toString(), // argv[3]
            RelationId(userId = userId, targetId = targetId) // argv[4]
                .let { objectMapper.writeValueAsString(it) }
        )
    }

    fun unfollow(userId: UUID, targetId: UUID) {
        // settings
        val script = redisScriptProvider.unfollow()
        val followingKey = redisKeyProvider.following(userId = userId)
        val followerKey = redisKeyProvider.follower(userId = targetId)
        val unfollowQueueKey = redisKeyProvider.unfollowQueue()

        // execute script
        redisTemplate.execute(
            RedisScript.of(script, String::class.java), // script
            listOf(followingKey, followerKey, unfollowQueueKey), // keys
            userId.toString(), // argv[1]
            targetId.toString(), // argv[2]
            RelationId(userId = userId, targetId = targetId) // argv[3]
                .let { objectMapper.writeValueAsString(it) }
        )
    }

    fun checkFollowQueue(): List<Relation> {
        // settings
        val script = redisScriptProvider.followQueue()
        val followQueueKey = redisKeyProvider.followQueue()

        // execute script
        val result = redisTemplate.execute(
            RedisScript.of(script, List::class.java), // script
            listOf(followQueueKey) // keys
        )

        // get relations
        val relations = result
            .map { objectMapper.readValue(it.toString(), RelationId::class.java) }
            .map { Relation(userId = it.userId, targetId = it.targetId) }

        return relations
    }

    fun checkUnfollowQueue(): List<RelationId> {
        // settings
        val script = redisScriptProvider.unfollowQueue()
        val unfollowQueueKey = redisKeyProvider.unfollowQueue()

        // execute script
        val result = redisTemplate.execute(
            RedisScript.of(script, List::class.java), // script
            listOf(unfollowQueueKey) // keys
        )

        // get relation ids
        val relationIds = result
            .map { objectMapper.readValue(it.toString(), RelationId::class.java) }

        return relationIds
    }

    fun getRelations(userId: UUID, cursor: UUID?, type: RelationType, pageSize: Long): List<FollowResponse> {
        val script = """
            local key = KEYS[1]
            
            local cursor = ARGV[1]
            local page_size = tonumber(ARGV[2])
            local current_time = tonumber(ARGV[3])
            
            local user_ids
            
            -- 커서 기반 페이징 처리
            if cursor == "" then
                user_ids = redis.call('ZREVRANGEBYSCORE', key, current_time, '-inf', 'LIMIT', 0, page_size)
            else 
                local cursor_score = redis.call('ZSCORE', key, cursor)
                user_ids = redis.call('ZREVRANGEBYSCORE', key, cursor_score, '-inf', 'LIMIT', 1, page_size)
            end
            
            -- 팔로우 유저 정보 조회
            local result = {}
            for i, user_id in ipairs(user_ids) do
                local follow_info_key = 'user:' .. user_id
                local follow_info = redis.call('GET', follow_info_key)
                if follow_info == nil then
                    result[i] = 'NULL::' .. user_id
                else
                    result[i] = follow_info
                end
            end
            
            return result
        """.trimIndent()

        val key = when (type) {
            RelationType.FOLLOWING -> redisKeyProvider.following(userId = userId)
            RelationType.FOLLOWER -> redisKeyProvider.follower(userId = userId)
        }

        val result = redisTemplate.execute(
            RedisScript.of(script, List::class.java), // script
            listOf(key), // keys
            cursor?.toString() ?: "", // argv[1]
            pageSize.toString(), // argv[2]
            LocalDateTime.now().atZone(ZoneId.systemDefault())
                .toInstant().toEpochMilli().toDouble().toString() // argv[3]
        )

        println(result)

        val followResponses = result.map {
            val json = it as String

            // if there's no cache in redis, get follow info from user-server
            if (json.startsWith("NULL")) {
                val id = UUID.fromString(json.substring(6))

                userServiceClient.getFollowInfo(userId = id)
            } else objectMapper.readValue(json, FollowResponse::class.java)
        }

        return followResponses
    }
}