package com.hunzz.postserver.domain.comment.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.hunzz.postserver.global.client.UserServerClient
import com.hunzz.postserver.global.model.CachedUser
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.script.RedisScript
import org.springframework.stereotype.Component
import java.util.*

@Component
class CommentRedisHandler(
    private val objectMapper: ObjectMapper,
    private val redisTemplate: RedisTemplate<String, String>,
    private val userServerClient: UserServerClient
) {
    fun getUserInfo(userIds: List<String>): List<CachedUser> {
        val script = """
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

        var retryCount = 0
        val maxRetries = 3

        var result: List<*> = listOf<Any>()
        var missingInfos = hashMapOf<UUID, CachedUser>()

        while (retryCount < maxRetries) {
            try {
                // execute script
                result = redisTemplate.execute(
                    RedisScript.of(script, List::class.java), // script
                    listOf(), // keys
                    objectMapper.writeValueAsString(userIds) // argv[1]
                )

                // if there's no cache in redis, get follow info from user-server
                val missingIds = result.filterIsInstance<String>()
                    .filter { it.startsWith("NULL:") }
                    .map { UUID.fromString(it.substring(5)) }

                if (missingIds.isNotEmpty())
                    missingInfos = userServerClient.getUsers(missingIds = missingIds)

                break
            } catch (e: Exception) {
                retryCount++
                if (retryCount == maxRetries) throw e

                Thread.sleep(1000)
            }
        }

        // return result
        val users = result.filterIsInstance<String>()
            .mapNotNull {
                if (it.startsWith("NULL:")) {
                    val id = UUID.fromString(it.substring(5))

                    missingInfos[id]
                } else objectMapper.readValue(it, CachedUser::class.java)
            }

        return users
    }
}