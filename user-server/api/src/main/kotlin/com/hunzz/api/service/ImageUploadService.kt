package com.hunzz.api.service

import com.hunzz.api.component.UserKafkaHandler
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withTimeout
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.util.*

@Service
class ImageUploadService(
    @Value("\${cloud.aws.s3.bucketName}")
    private val bucketName: String,

    private val userKafkaHandler: UserKafkaHandler
) {
    private fun getImageFileNames(userId: UUID): Pair<String, String> {
        val originalFileName = "user-${userId}.jpg"
        val thumbnailFileName = "user-${userId}-thumbnail.jpg"

        return Pair(originalFileName, thumbnailFileName)
    }

    private fun getImageUrl(fileName: String): String {
        return "https://${bucketName}.s3.amazonaws.com/$fileName"
    }

    suspend fun uploadImage(userId: UUID, image: MultipartFile): Unit = coroutineScope {
        // 이미지 파일명 생성
        val (originalFileName, thumbnailFileName) = getImageFileNames(userId = userId)

        withTimeout(5_000) {
            // 작업1: AWS S3에 이미지 업로드
            val job1 = async {
                // Kafka 메시지 전송 (user-api -> image-server)
                userKafkaHandler.uploadImage(
                    originalFileName = originalFileName,
                    thumbnailFileName = thumbnailFileName,
                    image = image
                )
            }

            // 작업2: DB에 유저 정보 수정
            val job2 = async {
                // 위에서 생성한 파일명을 활용해 URL 제작
                val originalUrl = getImageUrl(fileName = originalFileName)
                val thumbnailUrl = getImageUrl(fileName = thumbnailFileName)

                // Kafka 메시지 전송 (user-api -> user-data)
                userKafkaHandler.updateUserImageUrls(
                    userId = userId,
                    originalUrl = originalUrl,
                    thumbnailUrl = thumbnailUrl
                )
            }

            // 작업3: 유저 캐시 재등록
            val job3 = async {
                // Kafka 메시지 전송 (user-api -> user-cache)
                userKafkaHandler.reAddUserCache(userId = userId)
            }
            awaitAll(job1, job2, job3)
        }
    }
}