package com.hunzz.imageserver.service

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.PutObjectRequest
import com.fasterxml.jackson.databind.ObjectMapper
import com.hunzz.imageserver.dto.KafkaImageRequest
import com.hunzz.imageserver.dto.KafkaImagesRequest
import net.coobird.thumbnailator.Thumbnails
import net.coobird.thumbnailator.geometry.Positions
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

@Component
class ImageHandler(
    @Value("\${cloud.aws.s3.bucketName}")
    private val bucketName: String,

    private val amazonS3: AmazonS3,
    private val objectMapper: ObjectMapper
) {
    private fun uploadToS3(
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

    @KafkaListener(topics = ["upload-image"], groupId = "upload-image")
    fun upload(message: String) {
        val data = objectMapper.readValue(message, KafkaImageRequest::class.java)
        val originalImage = ImageIO.read(ByteArrayInputStream(data.image))

        uploadToS3(fileName = data.originalFileName, image = originalImage, scale = 1.0)
        uploadToS3(fileName = data.thumbnailFileName, image = originalImage, scale = 0.25, isThumbnail = true)
    }

    @KafkaListener(topics = ["upload-images"], groupId = "upload-images")
    fun uploadAll(message: String) {
        val data = objectMapper.readValue(message, KafkaImagesRequest::class.java)

        repeat(data.images.size) { index ->
            // 이미지 파일
            val originalImage = ImageIO.read(ByteArrayInputStream(data.images[index]))

            // 원본 이미지 업로드
            uploadToS3(fileName = data.fileNames[index], image = originalImage, scale = 1.0)

            // 첫번째 이미지의 썸네일만 업로드
            if (index == 0)
                uploadToS3(fileName = data.thumbnailFileName, image = originalImage, scale = 0.25, isThumbnail = true)
        }
    }
}