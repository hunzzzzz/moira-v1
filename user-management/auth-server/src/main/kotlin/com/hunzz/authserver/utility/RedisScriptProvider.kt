package com.hunzz.authserver.utility

import org.springframework.stereotype.Component

@Component
class RedisScriptProvider {
    fun validateLoginRequest() = """
            local emails_key = KEYS[1]
            local banned_users_key = KEYS[2]
            local auth_key = KEYS[3]
            
            local email = ARGV[1]
            
            -- 이메일 검증
            if redis.call('SISMEMBER', emails_key, email) ~= 1 then
                return 'INVALID_LOGIN_INFO'
            end
            
            -- 차단 여부 확인
            if redis.call('ZSCORE', banned_users_key, email) then
                return 'BANNED_USER_CANNOT_LOGIN'
            end
            
            -- 유저 인증 정보 가져오기
            local user_auth = redis.call('GET', auth_key)
            if not user_auth then
                return 'REFRESH'
            end
            
            return user_auth
        """.trimIndent()

    fun logout() = """
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

    fun checkRtk() = """
            local rtk_key = KEYS[1]
            local auth_key = KEYS[2]
            local rtk_user = ARGV[1]
            
            -- RTK 검증
            local rtk_redis = redis.call('GET', rtk_key)
            if rtk_user ~= rtk_redis then
                return 'INVALID_TOKEN'
            end
            
            -- 유저 인증 정보 가져오기
            local user_auth = redis.call('GET', auth_key)
            if not user_auth then
                return 'REFRESH'
            end
            
            return user_auth
        """.trimIndent()
}