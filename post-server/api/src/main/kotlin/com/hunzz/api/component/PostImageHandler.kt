package com.hunzz.api.component

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.util.*

@Component
class PostImageHandler(
    @Value("\${cloud.aws.s3.bucketName}")
    private val bucketName: String,

    private val postKafkaHandler: PostKafkaHandler
) {
    private fun getOriginalFileName(imageId: UUID): String {
        return "${imageId}.jpg"
    }

    private fun getThumbnailFileName(imageId: UUID): String {
        return "${imageId}-thumbnail.jpg"
    }

    private fun getImageUrl(fileName: String): String {
        return "https://${bucketName}.s3.amazonaws.com/$fileName"
    }

    fun sendImageUploadRequest(image: MultipartFile?): Pair<String?, String?> {
        if (image != null) {
            // 세팅
            val imageId = UUID.randomUUID()
            val originalFileName = getOriginalFileName(imageId = imageId)
            val thumbnailFileName = getThumbnailFileName(imageId = imageId)

            // Kafka 메시지 전송 (post-api -> image-server)
            postKafkaHandler.uploadPostImage(
                originalFileName = originalFileName,
                thumbnailFileName = thumbnailFileName,
                image = image
            )

            // imageUrl 리턴
            val originalImageUrl = getImageUrl(fileName = originalFileName)
            val thumbnailImageUrl = getImageUrl(fileName = thumbnailFileName)

            return Pair(originalImageUrl, thumbnailImageUrl)
        }
        return Pair(null, null)
    }
}