package com.hunzz.feedserver.domain.feed.service

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class FeedScheduler(
    private val feedRedisHandler: FeedRedisHandler,
    private val jdbcTemplate: JdbcTemplate
) {
    @Scheduled(fixedRate = 1000 * 60 * 60)
    fun checkReadFeedQueue() {
        // get feed ids
        val feedIds = feedRedisHandler.checkReadFeedQueue()

        // batch delete (with jdbc template)
        val sql = "DELETE FROM feeds WHERE feed_id = ?"

        jdbcTemplate.batchUpdate(sql, feedIds, 1000) { ps, feedId ->
            ps.setLong(1, feedId)
        }
    }
}