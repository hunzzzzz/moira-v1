package com.hunzz.api.component

import org.springframework.stereotype.Component

@Component
class PostRedisScriptProvider {
    fun like() = """
            local like_key = KEYS[1]
            local like_count_key = KEYS[2]
            local like_notification_key = KEYS[3]
            
            local post_id = ARGV[1]
            local user_id = ARGV[2]
            local current_time = ARGV[3]
            
            -- 이미 해당 게시글에 좋아요를 누른 경우 예외 처리
            if redis.call('ZSCORE', like_key, post_id) then
                return 'ALREADY_LIKED'
            end
            
            -- 유저의 좋아요 게시글 목록에 postId 추가
            redis.call('ZADD', like_key, current_time, post_id)
            -- 좋아요 수 +1
            redis.call('ZINCRBY', like_count_key, 1.0, post_id)
            -- 좋아요 큐에 postId 저장
            redis.call('ZADD', like_notification_key, current_time, user_id)
            
            return 'VALID'
        """.trimIndent()

    fun unlike() = """
            local like_key = KEYS[1]
            local like_count_key = KEYS[2]
            local like_notification_key = KEYS[3]
            
            local post_id = ARGV[1]
            local user_id = ARGV[2]
            local current_time = ARGV[3]
            
            -- 게시글에 좋아요가 되어있지 않은 경우 예외 처리
            if redis.call('ZSCORE', like_key, post_id) == false then
                return 'ALREADY_UNLIKED'
            end
            
            -- 유저의 좋아요 게시글 목록에 postId 제거
            redis.call('ZREM', like_key, post_id)
            -- 좋아요 수 -1
            redis.call('ZINCRBY', like_count_key, -1.0, post_id)
            -- 좋아요 큐에 postId 제거
            if redis.call('ZSCORE', like_notification_key, user_id) then
                redis.call('ZREM', like_notification_key, user_id)
            end
            
            return 'VALID'       
    """.trimIndent()
}