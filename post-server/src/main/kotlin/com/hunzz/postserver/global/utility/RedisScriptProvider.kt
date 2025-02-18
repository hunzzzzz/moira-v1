package com.hunzz.postserver.global.utility

import org.springframework.stereotype.Component

@Component
class RedisScriptProvider {
    fun like() = """
            local like_key = KEYS[1]
            local like_count_key = KEYS[2]
            
            local post_id = ARGV[1]
            local current_time = ARGV[2]
            
            if redis.call('ZSCORE', like_key, post_id) then
                return 'ALREADY_LIKED'
            end
            
            redis.call('ZADD', like_key, current_time, post_id)
            redis.call('ZINCRBY', like_count_key, 1.0, post_id)
            
            return 'VALID'
        """.trimIndent()

    fun unlike() = """
            local like_key = KEYS[1]
            local like_count_key = KEYS[2]
            
            local post_id = ARGV[1]
            local current_time = ARGV[2]
            
            if redis.call('ZSCORE', like_key, post_id) == false then
                return 'ALREADY_UNLIKED'
            end
            
            redis.call('ZREM', like_key, post_id)
            redis.call('ZINCRBY', like_count_key, -1.0, post_id)
            
            return 'VALID'       
    """.trimIndent()
}