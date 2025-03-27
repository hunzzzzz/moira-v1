package com.hunzz.authserver.utility.redis

import org.springframework.stereotype.Component

@Component
class RedisScriptProvider {
    fun blockAtkThenDeleteRtk() = """
            -- 세팅
            local blocked_atk_key = KEYS[1]
            local rtk_key = KEYS[2]
            
            local atk = ARGV[1]
            local atk_exp_time = ARGV[2]
            
            -- 로그아웃 유저의 ATK 차단
            redis.call('SET', blocked_atk_key, atk, 'PX', atk_exp_time)
            
            -- RTK 삭제
            redis.call('DEL', rtk_key)
            
            return 'OK'
        """.trimIndent()

    fun validateRtk() = """
            -- 세팅
            local rtk_key = KEYS[1]
            local rtk_from_header = ARGV[1]
            
            -- RTK 추출
            local rtk_redis = redis.call('GET', rtk_key)
            
            -- RTK 만료 여부 검증
            if rtk_redis == false then
                return 'EXPIRED_AUTH'
            end
            
            -- RTK 일치 여부 검증
            if rtk_from_header ~= rtk_redis then
                return 'INVALID_TOKEN'
            end
            
            return 'OK'
        """.trimIndent()
}