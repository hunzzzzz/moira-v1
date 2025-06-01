package com.hunzz.data.task

import com.fasterxml.jackson.databind.ObjectMapper
import com.hunzz.common.redis.RedisKeyProvider
import com.hunzz.data.dto.FeedDeleteQueueDto
import com.hunzz.data.dto.FeedQueueDto
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.script.RedisScript
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.nio.ByteBuffer
import java.util.*

@Component
class FeedScheduler(
    private val jdbcTemplate: JdbcTemplate,
    private val objectMapper: ObjectMapper,
    private val redisKeyProvider: RedisKeyProvider,
    private val redisTemplate: RedisTemplate<String, String>
) {
    private fun UUID.toBytes(): ByteArray {
        val byteBuffer = ByteBuffer.allocate(16)

        byteBuffer.putLong(this.mostSignificantBits)
        byteBuffer.putLong(this.leastSignificantBits)

        return byteBuffer.array()
    }

    fun checkFeedQueue() {
        val script = """
            local feed_queue_key = KEYS[1]
            local elements = {}
            
            -- 피드 큐에서 요소를 하나씩 pop
            while redis.call('SCARD', feed_queue_key) > 0 do
                local element = redis.call('SPOP', feed_queue_key)
                table.insert(elements, element)
            end
            
            return elements
        """.trimIndent()

        // 스크립트 실행
        val result = redisTemplate.execute(
            RedisScript.of(script, List::class.java),
            listOf(redisKeyProvider.feedQueue())
        ).map { objectMapper.readValue(it as String, FeedQueueDto::class.java) }

        if (result.isEmpty()) return

        // 배치 삽입 (with jdbc template)
        val sql = "INSERT INTO feeds (user_id, post_id, author_id) VALUES (?, ?, ?)"

        jdbcTemplate.batchUpdate(sql, result, 1000) { ps, dto ->
            ps.setBytes(1, dto.userId.toBytes())
            ps.setBytes(2, dto.postId.toBytes())
            ps.setBytes(3, dto.authorId.toBytes())
        }
    }

    fun checkFeedDeleteQueue() {
        val script = """
            local feed_delete_queue_key = KEYS[1]
            local elements = {}
            
            -- 피드 삭제 큐에서 요소를 하나씩 pop
            while redis.call('SCARD', feed_delete_queue_key) > 0 do
                local element = redis.call('SPOP', feed_delete_queue_key)
                table.insert(elements, element)
            end
            
            return elements
        """.trimIndent()

        val result = redisTemplate.execute(
            RedisScript.of(script, List::class.java),
            listOf(redisKeyProvider.feedDeleteQueue())
        ).map { objectMapper.readValue(it as String, FeedDeleteQueueDto::class.java) }

        if (result.isEmpty()) return

        // 배치 삭제 (jdbc template)
        val sql = "DELETE FROM feeds WHERE user_id = ? AND author_id = ?"

        jdbcTemplate.batchUpdate(sql, result, 1000) { ps, dto ->
            ps.setBytes(1, dto.userId.toBytes())
            ps.setBytes(2, dto.authorId.toBytes())
        }
    }

    @Scheduled(fixedDelay = 1000 * 10)
    fun checkQueue() {
        checkFeedQueue()
        checkFeedDeleteQueue()
    }
}