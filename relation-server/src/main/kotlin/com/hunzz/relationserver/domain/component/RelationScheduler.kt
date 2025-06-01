package com.hunzz.relationserver.domain.component

import com.hunzz.relationserver.domain.model.RelationType
import com.hunzz.relationserver.domain.repository.RelationRepository
import com.hunzz.relationserver.utility.client.UserServerClient
import com.hunzz.relationserver.utility.redis.RedisKeyProvider
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.nio.ByteBuffer
import java.time.LocalDateTime
import java.util.*

@Component
class RelationScheduler(
    private val jdbcTemplate: JdbcTemplate,
    private val relationRedisHandler: RelationRedisHandler,
    private val relationRepository: RelationRepository,
    private val redisKeyProvider: RedisKeyProvider,
    private val redisTemplate: RedisTemplate<String, String>,
    private val userServerClient: UserServerClient
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

    @Scheduled(cron = "0 0 4 * * ?") // 매일 새벽 4시 실행
    fun validateRedisRelations() {
        val allUserIds = userServerClient.getAllUserIds()

        allUserIds.forEach { userId ->
            val followingKey = redisKeyProvider.following(userId = userId)
            val followerKey = redisKeyProvider.follower(userId = userId)

            val followings = redisTemplate.opsForZSet().size(followingKey) ?: 0
            val followers = redisTemplate.opsForList().size(followerKey) ?: 0

            val followingsFromDB = relationRepository.countByUserId(userId = userId)
            val followersFromDB = relationRepository.countByTargetId(targetId = userId)

            if (followings != followingsFromDB)
                syncRelationsToRedis(userId = userId, type = RelationType.FOLLOWING)

            if (followers != followersFromDB)
                syncRelationsToRedis(userId = userId, type = RelationType.FOLLOWER)
        }
    }

    private fun syncRelationsToRedis(userId: UUID, type: RelationType) {
        when (type) {
            RelationType.FOLLOWING -> {
                val followingKey = redisKeyProvider.following(userId = userId)
                redisTemplate.delete(followingKey)

                // TODO

            }

            RelationType.FOLLOWER -> {
                val followerKey = redisKeyProvider.follower(userId = userId)
                redisTemplate.delete(followerKey)

                // TODO
            }
        }
    }
}