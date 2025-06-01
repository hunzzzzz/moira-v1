package com.hunzz.api.component

import com.fasterxml.jackson.databind.ObjectMapper
import com.hunzz.api.client.PostServerClient
import com.hunzz.api.client.UserServerClient
import com.hunzz.api.client.dto.UserInfo
import com.hunzz.common.redis.RedisKeyProvider
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.script.RedisScript
import org.springframework.stereotype.Component
import java.util.*

@Component
class CommentRedisHandler(
    private val commentRedisScriptProvider: CommentRedisScriptProvider,
    private val objectMapper: ObjectMapper,
    private val postServerClient: PostServerClient,
    private val redisKeyProvider: RedisKeyProvider,
    private val redisTemplate: RedisTemplate<String, String>,
    private val userServerClient: UserServerClient
) {
    fun getUserInfos(userIds: List<UUID>): List<UserInfo> {
        // 세팅
        val script = commentRedisScriptProvider.getUserInfos()

        // 스크립트 실행
        val result = redisTemplate.execute(
            RedisScript.of(script, List::class.java),
            listOf(),
            objectMapper.writeValueAsString(userIds)
        )

        // 유저 캐시 정보가 없는 userId만 추출
        val missingIds = result.filterIsInstance<String>()
            .filter { it.startsWith("NULL:") }
            .map { UUID.fromString(it.substring(5)) }
        var missingUserInfos = hashMapOf<UUID, UserInfo>()

        // 캐시 정보가 없는 유저들의 id 리스트를 user-cache 서버로 전송하여 유저 정보 조회
        // 네트워크 에러를 대비하여 최대 3번 요청을 보낸다.
        if (missingIds.isNotEmpty()) {
            var retryCount = 0
            val maxRetries = 3

            while (retryCount < maxRetries) {
                try {
                    missingUserInfos = userServerClient.getUsers(missingIds = missingIds)

                    break
                } catch (e: Exception) {
                    retryCount++
                    if (retryCount == maxRetries) throw e

                    Thread.sleep(1000)
                }
            }
        }

        // UserInfo 리스트를 리턴
        val userInfos = result.filterIsInstance<String>()
            .mapNotNull {
                if (it.startsWith("NULL:")) {
                    val userId = UUID.fromString(it.substring(5))

                    missingUserInfos[userId]
                } else objectMapper.readValue(it, UserInfo::class.java)
            }

        return userInfos
    }

    fun getPostAuthorId(postId: UUID): UUID {
        val postAuthorKey = redisKeyProvider.postAuthor(postId = postId)
        val postAuthorId = redisTemplate.opsForValue().get(postAuthorKey)

        // 게시글 작성자의 id를 Redis 캐시로 조회하되, 캐시 미스의 경우 post-cache 서버에서 조회
        return postAuthorId?.let { UUID.fromString(it) }
            ?: postServerClient.getPostAuthorId(postId = postId)
    }
}