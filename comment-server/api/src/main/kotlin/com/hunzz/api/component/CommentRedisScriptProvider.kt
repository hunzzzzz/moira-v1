package com.hunzz.api.component

import org.springframework.stereotype.Component

@Component
class CommentRedisScriptProvider {
    fun getUserInfos() = """
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
}