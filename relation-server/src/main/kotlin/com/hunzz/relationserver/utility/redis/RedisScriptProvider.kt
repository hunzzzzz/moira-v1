package com.hunzz.relationserver.utility.redis

import org.springframework.stereotype.Component

@Component
class RedisScriptProvider {
    fun follow() = """
            -- 세팅
            local following_key = KEYS[1]
            local follower_key = KEYS[2]
            local follow_queue_key = KEYS[3]
            
            local user_id = ARGV[1]
            local target_id = ARGV[2]
            local current_time = tonumber(ARGV[3])
            local follow_queue_data = ARGV[4]
            
            -- 팔로잉 목록 업데이트 (userId)
            redis.call('ZADD', following_key, current_time, target_id)
            -- 팔로워 목록 업데이트 (targetId)
            redis.call('ZADD', follower_key, current_time, user_id)
            
            -- 팔로우 큐에 저장
            redis.call('SADD', follow_queue_key, follow_queue_data)
            
            return nil
        """.trimIndent()

    fun unfollow() = """
            -- 세팅
            local following_key = KEYS[1]
            local follower_key = KEYS[2]
            local unfollow_queue_key = KEYS[3]
            
            local user_id = ARGV[1]
            local target_id = ARGV[2]
            local unfollow_queue_data = ARGV[3]
            
            -- 팔로잉 목록 업데이트 (userId)
            redis.call('ZREM', following_key, target_id)
            -- 팔로워 목록 업데이트 (targetId)
            redis.call('ZREM', follower_key, user_id)
            
            -- 언팔로우 큐에 저장
            redis.call('SADD', unfollow_queue_key, unfollow_queue_data)
            
            return nil
        """.trimIndent()

    fun followQueue() = """
            local follow_queue_key = KEYS[1]
            local elements = {}
            
            -- 팔로우 큐에서 요소를 하나씩 pop
            while redis.call('SCARD', follow_queue_key) > 0 do
                local element = redis.call('SPOP', follow_queue_key)
                table.insert(elements, element)
            end
            
            return elements
        """.trimIndent()

    fun unfollowQueue() = """
            local unfollow_queue_key = KEYS[1]
            local elements = {}
            
            -- 언팔로우 큐에서 요소를 하나씩 pop
            while redis.call('SCARD', unfollow_queue_key) > 0 do
                local element = redis.call('SPOP', unfollow_queue_key)
                table.insert(elements, element)
            end
            
            return elements
        """.trimIndent()

    fun relations() = """
            -- 세팅
            local key = KEYS[1]
            local cursor = ARGV[1]
            local page_size = tonumber(ARGV[2])
            local current_time = tonumber(ARGV[3])
            
            local user_ids
            
            -- 커서 기반 페이징 처리
            if cursor == "" or cursor == nil then
                user_ids = redis.call('ZREVRANGEBYSCORE', key, current_time, '-inf', 'LIMIT', 0, page_size)
            else
                local cursor_score = redis.call('ZSCORE', key, cursor)
                user_ids = redis.call('ZREVRANGEBYSCORE', key, cursor_score, '-inf', 'LIMIT', 1, page_size)
            end
            
            -- 유저 캐시 조회
            local result = {}
            
            for i, user_id in ipairs(user_ids) do
                local user_cache_key = 'user:' .. user_id
                local user_cache = redis.call('GET', user_cache_key)
                if user_cache == false then
                    result[i] = 'NULL:' .. user_id
                else
                    result[i] = user_cache
                end
            end
            
            return result
        """.trimIndent()
}