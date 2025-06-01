package com.hunzz.api.component

import org.springframework.stereotype.Component

@Component
class FeedRedisScriptProvider {
    fun getUserInfos() = """
            -- 세팅
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

    fun getPostInfos() = """
            -- 세팅
            local post_ids = cjson.decode(ARGV[1])
            local keys = {}
            
            -- KEY 배열 생성
            for i, post_id in ipairs(post_ids) do
                keys[i] = 'post:' .. post_id
            end
            
            -- MGET으로 일괄 조회
            local post_infos = redis.call('MGET', unpack(keys))
            
            -- 누락된 데이터 처리
            local result = {}
            for i, info in ipairs(post_infos) do
                if info == false then
                    result[i] = 'NULL:' .. post_ids[i]
                else
                    result[i] = info
                end
            end
            
            return result
    """.trimIndent()

    fun getLikeInfos() = """
            -- 세팅
            local like_key = KEYS[1]
            local like_count_key = KEYS[2]
            local post_ids = cjson.decode(ARGV[1])
            
            local result = {}
            
            -- LIKE 데이터 조회
            for i, post_id in ipairs(post_ids) do
                -- 좋아요 개수
                local likes = redis.call('ZSCORE', like_count_key, post_id) or 0
                -- 해당 게시글에 좋아요를 했는지 여부 (int 형으로 변환)
                local has_like = redis.call('ZSCORE', like_key, post_id) ~= false and 1 or 0
                
                result[i] = cjson.encode({
                    likes = likes, 
                    hasLike = has_like
                })
            end
            
            return result
    """.trimIndent()

    fun readFeed() = """ 
            local read_feed_queue_key = KEYS[1]
            local feed_ids = cjson.decode(ARGV[1])
            local delete_time = tonumber(ARGV[2])
            
            -- '읽은 피드' 큐에 데이터 적재
            for _, feed_id in ipairs(feed_ids) do
                redis.call('ZADD', read_feed_queue_key, delete_time, feed_id)
            end
            
            return 'OK'
        """.trimIndent()
}