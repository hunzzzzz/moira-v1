package com.hunzz.relationserver.domain.relation.service

import com.hunzz.relationserver.global.exception.ErrorCode.*
import com.hunzz.relationserver.global.exception.custom.InvalidRelationException
import com.hunzz.relationserver.global.utility.RedisKeyProvider
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.script.RedisScript
import org.springframework.stereotype.Component
import java.util.*

@Component
class RelationRedisScriptHandler(
    private val redisKeyProvider: RedisKeyProvider,
    private val redisTemplate: RedisTemplate<String, String>
) {
    fun checkFollowRequest(userId: UUID, targetId: UUID) {
        // script
        val script = """
            local ids_key = KEYS[1]
            local following_key = KEYS[2]
            local target_id = ARGV[1]
            
            -- 유저 존재 여부 확인
            --if redis.call('SISMEMBER', ids_key, target_id) ~= 1 then
            --    return 'FOLLOWING_NOT_EXISTING_USER'
            --end
            
            -- 팔로우 여부 확인
            if redis.call('ZSCORE', following_key, target_id) then
                return 'ALREADY_FOLLOWED'
            end         
               
            return 'VALID'
        """.trimIndent()

        // redis keys
        val idsKey = redisKeyProvider.ids()
        val followingKey = redisKeyProvider.following(userId = userId)

        // execute script
        val result = redisTemplate.execute(
            RedisScript.of(script, String::class.java),
            listOf(idsKey, followingKey),
            targetId.toString()
        )

        // check result
        when (result) {
            FOLLOWING_NOT_EXISTING_USER.name -> throw InvalidRelationException(FOLLOWING_NOT_EXISTING_USER)
            ALREADY_FOLLOWED.name -> throw InvalidRelationException(ALREADY_FOLLOWED)
            else -> return
        }
    }

    fun checkUnfollowRequest(userId: UUID, targetId: UUID) {
        // script
        val script = """
            local ids_key = KEYS[1]
            local following_key = KEYS[2]
            local target_id = ARGV[1]
            
            -- 유저 존재 여부 확인
            --if redis.call('SISMEMBER', ids_key, target_id) ~= 1 then
            --    return 'FOLLOWING_NOT_EXISTING_USER'
            --end
            
            -- 팔로우 여부 확인
            if redis.call('ZSCORE', following_key, target_id) == false then
                return 'ALREADY_UNFOLLOWED'
            end         
               
            return 'VALID'
        """.trimIndent()

        // redis keys
        val idsKey = redisKeyProvider.ids()
        val followingKey = redisKeyProvider.following(userId = userId)

        // execute script
        val result = redisTemplate.execute(
            RedisScript.of(script, String::class.java),
            listOf(idsKey, followingKey),
            targetId.toString()
        )

        // check result
        when (result) {
            FOLLOWING_NOT_EXISTING_USER.name -> throw InvalidRelationException(FOLLOWING_NOT_EXISTING_USER)
            ALREADY_UNFOLLOWED.name -> throw InvalidRelationException(ALREADY_UNFOLLOWED)
            else -> return
        }
    }

    fun follow(userId: UUID, targetId: UUID) {
        // script
        val script = """
            local following_key = KEYS[1]
            local follower_key = KEYS[2]
            local user_id = ARGV[1]
            local target_id = ARGV[2]
            local current_time = tonumber(ARGV[3])
            
            -- 팔로우
            redis.call('ZADD', following_key, current_time, target_id)
            redis.call('ZADD', follower_key, current_time, user_id)
            
            return nil
        """.trimIndent()

        // redis keys
        val followingKey = redisKeyProvider.following(userId = userId)
        val followerKey = redisKeyProvider.follower(userId = targetId)

        // execute script
        redisTemplate.execute(
            RedisScript.of(script, String::class.java),
            listOf(followingKey, followerKey),
            userId.toString(),
            targetId.toString(),
            System.currentTimeMillis().toString()
        )
    }

    fun unfollow(userId: UUID, targetId: UUID) {
        val script = """
            local following_key = KEYS[1]
            local follower_key = KEYS[2]
            local user_id = ARGV[1]
            local target_id = ARGV[2]
            
            -- 팔로우 취소
            redis.call('ZREM', following_key, target_id)
            redis.call('ZREM', follower_key, user_id)
            
            return nil
        """.trimIndent()

        // redis keys
        val followingKey = redisKeyProvider.following(userId = userId)
        val followerKey = redisKeyProvider.follower(userId = targetId)

        // execute script
        redisTemplate.execute(
            RedisScript.of(script, String::class.java),
            listOf(followingKey, followerKey),
            userId.toString(),
            targetId.toString()
        )
    }
}