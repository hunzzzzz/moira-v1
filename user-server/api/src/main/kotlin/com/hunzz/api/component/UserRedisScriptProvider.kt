package com.hunzz.api.component

import org.springframework.stereotype.Component

@Component
class UserRedisScriptProvider {
    fun validateSignupRequest() = """
            local emails_key = KEYS[1]
            local admin_code_key = KEYS[2]
            
            local email = ARGV[1]
            local input_admin_code = ARGV[2]
            
            -- 이메일 중복 검증
            if redis.call('SISMEMBER', emails_key, email) == 1 then
                return 'DUPLICATED_EMAIL'
            end
            
            -- 어드민 코드 검증
            if input_admin_code ~= '' then
                local stored_admin_code = redis.call('GET', 'admin_signup_code')
                if stored_admin_code ~= input_admin_code then
                    return 'INVALID_ADMIN_CODE'
                end
            end
            
            return 'VALID'
        """.trimIndent()

    fun signup() = """
            local emails_key = KEYS[1]
            local authKey = KEYS[2]
            
            local email = ARGV[1]
            local userAuth = ARGV[2]
    
            -- 이메일 저장
            redis.call('SADD', emails_key, email)
            -- 유저 인증 정보 저장
            redis.call('SET', authKey, userAuth, 'EX', 259200)
    
            return nil
        """.trimIndent()

    fun getRelationInfo() = """
            local following_key = KEYS[1]
            local follower_key = KEYS[2]
            
            -- 팔로잉 수 조회
            local num_of_followings = redis.call('ZCARD', following_key)
            -- 팔로워 수 조회
            local num_of_followers = redis.call('ZCARD', follower_key)
            
            return {num_of_followings, num_of_followers}
        """.trimIndent()
}