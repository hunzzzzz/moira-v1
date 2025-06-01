package com.hunzz.imageserver.task

import com.fasterxml.jackson.databind.ObjectMapper
import com.hunzz.imageserver.component.ImageUploader
import com.hunzz.imageserver.component.RedisKeyProvider
import com.hunzz.imageserver.dto.UploadPostImagesRequest
import com.hunzz.imageserver.model.ImageMeta
import com.hunzz.imageserver.model.ImageType
import com.hunzz.imageserver.repository.ImageMetaRepository
import jakarta.transaction.Transactional
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import java.io.ByteArrayInputStream
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO

@Component
class UploadPostImagesTask(
    private val imageMetaRepository: ImageMetaRepository,
    private val imageUploader: ImageUploader,
    private val objectMapper: ObjectMapper,
    private val redisKeyProvider: RedisKeyProvider,
    private val redisTemplate: RedisTemplate<String, String>
) {
    @KafkaListener(topics = ["upload-images"], groupId = "upload-images")
    @Transactional
    fun uploadAll(message: String) {
        val data = objectMapper.readValue(message, UploadPostImagesRequest::class.java)

        repeat(data.images.size) { index ->
            // 이미지 파일
            val image = ImageIO.read(ByteArrayInputStream(data.images[index]))

            // 원본 이미지 AWS S3에 업로드
            imageUploader.uploadToS3(fileName = data.fileNames[index], image = image, scale = 1.0)

            // 첫번째 이미지의 썸네일만 AWS S3에 업로드
            if (index == 0)
                imageUploader.uploadToS3(
                    fileName = data.thumbnailFileName,
                    image = image,
                    scale = 0.25,
                    isThumbnail = true
                )

            // DB에 저장
            ImageMeta(
                type = ImageType.POST,
                txId = data.txId,
                userId = data.userId,
                isThumbnail = index == 0,
                originalFileName = data.fileNames[index],
            ).let { imageMetaRepository.save(it) }
        }

        // 작업 완료 사실을 알림
        val pendingKey = redisKeyProvider.pending(txId = data.txId)
        redisTemplate.opsForSet().add(pendingKey, "image")
        redisTemplate.expire(pendingKey, 1, TimeUnit.HOURS)
    }
}