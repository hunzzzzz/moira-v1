package com.hunzz.api.component

import org.springframework.stereotype.Component

@Component
class UserRedisScriptProvider {
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