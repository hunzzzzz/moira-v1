package com.hunzz.api.component.validation

import com.hunzz.common.exception.ErrorCode.INVALID_IMAGE_FILE
import com.hunzz.common.exception.ErrorCode.MAX_IMAGE_COUNT_EXCEEDED
import com.hunzz.common.exception.custom.InvalidPostInfoException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.util.*

@Component
class PostImageHandler(
    @Value("\${cloud.aws.s3.bucketName}")
    private val bucketName: String,
) {
    fun validateImages(images: List<MultipartFile>) {
        if (images.size > 10)
            throw InvalidPostInfoException(MAX_IMAGE_COUNT_EXCEEDED)

        if (images.all { it.contentType?.startsWith("image/") == true })
            throw InvalidPostInfoException(INVALID_IMAGE_FILE)
    }

    fun generateOriginalFileNames(postId: UUID, images: List<MultipartFile>): List<String> {
        return List(images.size) { index -> "${postId}_${index + 1}.jpg" }
    }

    fun generateThumbnailFileNames(postId: UUID): String {
        return "${postId}-thumbnail.jpg"
    }

    fun generateImageUrl(fileName: String): String {
        return "https://${bucketName}.s3.amazonaws.com/$fileName"
    }
}