package com.hunzz.imageserver.task

import com.fasterxml.jackson.databind.ObjectMapper
import com.hunzz.imageserver.component.ImageUploader
import com.hunzz.imageserver.component.RedisKeyProvider
import com.hunzz.imageserver.dto.DeletePostImagesRequest
import com.hunzz.imageserver.repository.ImageMetaRepository
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.TimeUnit

@Component
class DeletePostImagesTask(
    private val imageMetaRepository: ImageMetaRepository,
    private val imageUploader: ImageUploader,
    private val objectMapper: ObjectMapper,
    private val redisKeyProvider: RedisKeyProvider,
    private val redisTemplate: RedisTemplate<String, String>
) {
    @KafkaListener(topics = ["delete-images"], groupId = "delete-images")
    @Transactional
    fun deleteImages(message: String) {
        val data = objectMapper.readValue(message, DeletePostImagesRequest::class.java)

        // AWS S3 버킷에서 삭제
        val imageMetas = imageMetaRepository.findAllByTxId(txId = data.txId)
        imageMetas.forEach { imageUploader.deleteFromS3(fileName = it.originalFileName) }

        // DB에서 삭제
        imageMetaRepository.deleteByTxId(txId = data.txId)

        // 롤백 완료 사실을 알림
        val rollbackKey = redisKeyProvider.rollback(txId = data.txId)
        redisTemplate.opsForSet().add(rollbackKey, "image")
        redisTemplate.expire(rollbackKey, 1, TimeUnit.HOURS)
    }
}