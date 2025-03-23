package com.hunzz.data.task

import com.hunzz.common.redis.RedisKeyProvider
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.script.RedisScript
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class FeedReadScheduler(
    private val jdbcTemplate: JdbcTemplate,
    private val redisKeyProvider: RedisKeyProvider,
    private val redisTemplate: RedisTemplate<String, String>
) {
    @Scheduled(fixedRate = 1000 * 10 * 5) // 5분 간격
    fun readFeed() {
        // 세팅
        val script = """
            local feed_read_queue_key = KEYS[1]
            local current_time = tonumber(ARGV[1])
            local elements = {}
            
            -- 현재 시간보다 작은 score를 가진 '읽은 피드' 조회
            elements = redis.call('ZRANGEBYSCORE', feed_read_queue_key, 0, current_time)
            -- 읽은 피드 삭제
            redis.call('ZREMRANGEBYSCORE', feed_read_queue_key, 0, current_time)

            return elements
        """.trimIndent()

        val feedReadQueueKey = redisKeyProvider.feedReadQueue()

        // 스크립트 실행
        val feedIds = redisTemplate.execute(
            RedisScript.of(script, List::class.java),
            listOf(feedReadQueueKey),
            System.currentTimeMillis().toString()
        ).map { (it as String).toLong() }

        // 배치 삭제
        val sql = "DELETE FROM feeds WHERE feed_id = ?"

        jdbcTemplate.batchUpdate(sql, feedIds, 1000) { ps, feedId ->
            ps.setLong(1, feedId)
        }
    }
}