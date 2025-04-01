package com.hunzz.api.component

import com.hunzz.common.model.PostTransactionStatus
import com.hunzz.common.redis.RedisKeyProvider
import com.hunzz.common.repository.PostTransactionRepository
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.repository.findByIdOrNull
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.*

@Component
class PostTransactionEventScheduler(
    private val postTransactionRepository: PostTransactionRepository,
    private val redisKeyProvider: RedisKeyProvider,
    private val redisTemplate: RedisTemplate<String, String>
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @Scheduled(fixedDelay = 1000 * 60 * 5)
    @Transactional
    fun checkRollbackAck() {
        val keys = redisTemplate.keys("rollback:*")

        keys.forEach { rollbackKey ->
            val txId = UUID.fromString(rollbackKey.split(":")[1])
            val services = redisTemplate.opsForSet().members(rollbackKey) ?: emptySet()

            // 모든 서버의 롤백 작업 완료를 확인한 경우, status를 COMPENSATED로 변경 + 키 삭제
            if (services.size == 3) {
                val pendingKey = redisKeyProvider.pending(txId = txId)
                postTransactionRepository.findByIdOrNull(id = txId)?.run {
                    updateStatus(status = PostTransactionStatus.COMPENSATED)

                    redisTemplate.delete(pendingKey)
                    redisTemplate.delete(rollbackKey)
                }
            }
            // 로깅 -> 수동 개입 필요
            else {
                val missingServices = setOf("data", "image", "feed") - services
                logger.error("[롤백 실패] txId=${txId}, 부재 서비스: $missingServices")
            }
        }
    }
}