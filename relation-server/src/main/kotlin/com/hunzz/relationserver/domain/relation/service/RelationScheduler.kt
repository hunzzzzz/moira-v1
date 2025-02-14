package com.hunzz.relationserver.domain.relation.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.hunzz.relationserver.domain.relation.model.Relation
import com.hunzz.relationserver.domain.relation.model.RelationId
import com.hunzz.relationserver.domain.relation.repository.RelationRepository
import com.hunzz.relationserver.global.utility.RedisKeyProvider
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.script.RedisScript
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class RelationScheduler(
    private val objectMapper: ObjectMapper,
    private val redisKeyProvider: RedisKeyProvider,
    private val redisTemplate: RedisTemplate<String, String>,
    private val relationRepository: RelationRepository
) {
    @Scheduled(fixedRate = 1000 * 10)
    fun checkFollowQueue() {
        // script
        val script = """
            local follow_queue_key = KEYS[1]
            local elements = {}
            
            while redis.call('SCARD', follow_queue_key) > 0 do
                local element = redis.call('SPOP', follow_queue_key)
                table.insert(elements, element)
            end
            
            return elements
        """.trimIndent()

        // redis keys
        val followQueueKey = redisKeyProvider.followQueue()

        // execute script
        val result = redisTemplate.execute(
            RedisScript.of(script, List::class.java),
            listOf(followQueueKey)
        )

        // get 'relation' list
        val list = result
            .map { objectMapper.readValue(it.toString(), RelationId::class.java) }
            .map { Relation(userId = it.userId, targetId = it.targetId) }

        // save
        relationRepository.saveAll(list)
    }

    @Scheduled(fixedRate = 1000 * 10)
    fun checkUnfollowQueue() {
        // script
        val script = """
            local unfollow_queue_key = KEYS[1]
            local elements = {}
            
            while redis.call('SCARD', unfollow_queue_key) > 0 do
                local element = redis.call('SPOP', unfollow_queue_key)
                table.insert(elements, element)
            end
            
            return elements
        """.trimIndent()

        // redis keys
        val unfollowQueueKey = redisKeyProvider.unfollowQueue()

        // execute script
        val result = redisTemplate.execute(
            RedisScript.of(script, List::class.java),
            listOf(unfollowQueueKey)
        )

        // get 'relation' list
        val ids = result
            .map { objectMapper.readValue(it.toString(), RelationId::class.java) }

        // save
        relationRepository.deleteAllById(ids)
    }
}