package com.hunzz.authserver.utility.redis

import org.springframework.stereotype.Component

@Component
class RedisScriptProvider {
    fun validateThenGetUserAuth() = """
            -- 세팅
            local emails_key = KEYS[1]
            local auth_key = KEYS[2]
            
            local email = ARGV[1]
            
            -- 이메일 검증
            if redis.call('SISMEMBER', emails_key, email) ~= 1 then
                return 'INVALID_LOGIN_INFO'
            end
            
            -- 유저 인증 정보 가져오기
            local user_auth = redis.call('GET', auth_key)
            if not user_auth then
                return 'NO_CACHE'
            end
            
            return user_auth
        """.trimIndent()

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
            
            return nil
        """.trimIndent()

    fun checkRtkThenGetUserAuth() = """
            -- 세팅
            local rtk_key = KEYS[1]
            local auth_key = KEYS[2]
            
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
            
            -- 유저 인증 정보 가져오기
            local user_auth = redis.call('GET', auth_key)
            if not user_auth then
                return 'NO_CACHE'
            end
            
            return user_auth
        """.trimIndent()
}