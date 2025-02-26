package com.hunzz.postserver.global.utility

import org.springframework.stereotype.Component

@Component
class RedisScriptProvider {
    fun like() = """
            local like_key = KEYS[1]
            local like_count_key = KEYS[2]
            local like_notification_key = KEYS[3]
            
            local post_id = ARGV[1]
            local user_id = ARGV[2]
            local current_time = ARGV[3]
            
            -- 이미 해당 게시글에 좋아요를 누른 경우
            if redis.call('ZSCORE', like_key, post_id) then
                return 'ALREADY_LIKED'
            end
            
            -- 특정 유저가 좋아요를 누른 게시글들의 postId 저장
            redis.call('ZADD', like_key, current_time, post_id)
            -- 게시글 별 좋아요 수 저장
            redis.call('ZINCRBY', like_count_key, 1.0, post_id)
            -- 좋아요 큐에 postId 저장
            redis.call('ZADD', like_notification_key, current_time, user_id)
            
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

    fun likeQueue() = """
            local key = KEYS[1]
            
            -- 좋아요를 누른 유저들의 id 추출
            local user_ids = redis.call('ZRANGE', key, 0, -1)

            -- 큐 전체 삭제
            redis.call('DEL', key)
            
            return user_ids
    """.trimIndent()
}