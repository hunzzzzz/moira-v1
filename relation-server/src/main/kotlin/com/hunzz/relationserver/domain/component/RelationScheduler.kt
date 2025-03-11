package com.hunzz.relationserver.domain.component

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.nio.ByteBuffer
import java.time.LocalDateTime
import java.util.*

@Component
class RelationScheduler(
    private val jdbcTemplate: JdbcTemplate,
    private val relationRedisHandler: RelationRedisHandler
) {
    private fun UUID.toBytes(): ByteArray {
        val byteBuffer = ByteBuffer.allocate(16)

        byteBuffer.putLong(this.mostSignificantBits)
        byteBuffer.putLong(this.leastSignificantBits)

        return byteBuffer.array()
    }

    @Scheduled(fixedRate = 1000 * 10)
    fun checkFollowQueue() {
        // Redis에서 팔로잉 큐 조회
        val relations = relationRedisHandler.checkFollowQueue()
        if (relations.isEmpty()) return

        // 배치 삽입 (Jdbc Template)
        val sql = "INSERT INTO relations (user_id, target_id, created_at) VALUES (?, ?, ?)"

        jdbcTemplate.batchUpdate(sql, relations, 1000) { ps, relation ->
            ps.setBytes(1, relation.userId.toBytes())
            ps.setBytes(2, relation.targetId.toBytes())
            ps.setObject(3, LocalDateTime.now())
        }
    }

    @Scheduled(fixedRate = 1000 * 10)
    fun checkUnfollowQueue() {
        // Redis에서 언팔로잉 큐 조회
        val relationIds = relationRedisHandler.checkUnfollowQueue()
        if (relationIds.isEmpty()) return

        // 배치 삭제 (Jdbc Template)
        val sql = "DELETE FROM relations WHERE user_id = ? AND target_id = ?"

        jdbcTemplate.batchUpdate(sql, relationIds, 1000) { ps, relationId ->
            ps.setBytes(1, relationId.userId.toBytes())
            ps.setBytes(2, relationId.targetId.toBytes())
        }
    }
}