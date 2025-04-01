package com.hunzz.imageserver.component

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.PutObjectRequest
import net.coobird.thumbnailator.Thumbnails
import net.coobird.thumbnailator.geometry.Positions
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

@Component
class ImageUploader(
    @Value("\${cloud.aws.s3.bucketName}")
    private val bucketName: String,

    private val amazonS3: AmazonS3,
) {
    fun uploadToS3(
        fileName: String,
        image: BufferedImage,
        scale: Double,
        isThumbnail: Boolean = false
    ) {
        // 세팅
        val outputStream = ByteArrayOutputStream()
        if (isThumbnail)
            Thumbnails.of(image)
                .crop(Positions.CENTER)
                .size(250, 250)
                .outputFormat("jpg")
                .toOutputStream(outputStream)
        else
            Thumbnails.of(image)
                .scale(scale)
                .outputFormat("jpg")
                .toOutputStream(outputStream)

        val inputStream = ByteArrayInputStream(outputStream.toByteArray())
        val metadata = ObjectMetadata().apply {
            contentLength = outputStream.size().toLong()
            contentType = "image/jpeg"
        }

        // AWS S3에 업로드
        amazonS3.putObject(
            PutObjectRequest(bucketName, fileName, inputStream, metadata)
        )
    }

    fun deleteFromS3(fileName: String) {
        amazonS3.deleteObject(bucketName, fileName)
    }
}