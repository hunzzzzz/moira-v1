package com.hunzz.relationserver.global.utility

import org.springframework.stereotype.Component

@Component
class RedisScriptProvider {
    fun checkFollowRequest() = """
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

    fun checkUnfollowRequest() = """
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

    fun follow() = """
            local following_key = KEYS[1]
            local follower_key = KEYS[2]
            local follow_queue_key = KEYS[3]
            
            local user_id = ARGV[1]
            local target_id = ARGV[2]
            local current_time = tonumber(ARGV[3])
            local follow_queue_data = ARGV[4]
            
            -- 팔로우
            redis.call('ZADD', following_key, current_time, target_id)
            redis.call('ZADD', follower_key, current_time, user_id)
            
            -- 팔로우 큐에 저장
            redis.call('SADD', follow_queue_key, follow_queue_data)
            
            return nil
        """.trimIndent()

    fun unfollow() = """
            local following_key = KEYS[1]
            local follower_key = KEYS[2]
            local unfollow_queue_key = KEYS[3]
            
            local user_id = ARGV[1]
            local target_id = ARGV[2]
            local unfollow_queue_data = ARGV[3]
            
            -- 팔로우 취소
            redis.call('ZREM', following_key, target_id)
            redis.call('ZREM', follower_key, user_id)
            
            -- 언팔로우 큐에 저장
            redis.call('SADD', unfollow_queue_key, unfollow_queue_data)
            
            return nil
        """.trimIndent()

    fun followQueue() = """
            local follow_queue_key = KEYS[1]
            local elements = {}
            
            while redis.call('SCARD', follow_queue_key) > 0 do
                local element = redis.call('SPOP', follow_queue_key)
                table.insert(elements, element)
            end
            
            return elements
        """.trimIndent()

    fun unfollowQueue() = """
            local unfollow_queue_key = KEYS[1]
            local elements = {}
            
            while redis.call('SCARD', unfollow_queue_key) > 0 do
                local element = redis.call('SPOP', unfollow_queue_key)
                table.insert(elements, element)
            end
            
            return elements
        """.trimIndent()
}