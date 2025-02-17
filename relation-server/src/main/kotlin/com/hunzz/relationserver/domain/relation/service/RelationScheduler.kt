package com.hunzz.relationserver.domain.relation.service

import com.hunzz.relationserver.global.utility.KafkaProducer
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.nio.ByteBuffer
import java.time.LocalDateTime
import java.util.*

@Component
class RelationScheduler(
    private val jdbcTemplate: JdbcTemplate,
    private val kafkaProducer: KafkaProducer,
    private val relationRedisScriptHandler: RelationRedisScriptHandler
) {
    private fun UUID.toBytes(): ByteArray {
        val byteBuffer = ByteBuffer.allocate(16)

        byteBuffer.putLong(this.mostSignificantBits)
        byteBuffer.putLong(this.leastSignificantBits)

        return byteBuffer.array()
    }

    @Scheduled(fixedRate = 1000 * 10)
    fun checkFollowQueue() {
        // get relations
        val relations = relationRedisScriptHandler.checkFollowQueue()
        if (relations.isEmpty()) return

        // batch insert (with jdbc template)
        val sql = "INSERT INTO relations (user_id, target_id, created_at, updated_at) VALUES (?, ?, ?, ?)"

        jdbcTemplate.batchUpdate(sql, relations, 1000) { ps, relation ->
            ps.setBytes(1, relation.userId.toBytes())
            ps.setBytes(2, relation.targetId.toBytes())
            ps.setObject(3, LocalDateTime.now())
            ps.setObject(4, LocalDateTime.now())
        }

        // send kafka message (to user-server)
        kafkaProducer.send("follow", relations.map { it.targetId })
    }

    @Scheduled(fixedRate = 1000 * 10)
    fun checkUnfollowQueue() {
        // get relation ids
        val relationIds = relationRedisScriptHandler.checkUnfollowQueue()
        if (relationIds.isEmpty()) return

        // batch delete (with jdbc template)
        val sql = "DELETE FROM relations WHERE user_id = ? AND target_id = ?"

        jdbcTemplate.batchUpdate(sql, relationIds, 1000) { ps, relationId ->
            ps.setBytes(1, relationId.userId.toBytes())
            ps.setBytes(2, relationId.targetId.toBytes())
        }
    }
}