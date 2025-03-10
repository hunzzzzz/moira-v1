package com.hunzz.imageserver.service

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.PutObjectRequest
import com.fasterxml.jackson.databind.ObjectMapper
import com.hunzz.imageserver.dto.KafkaImageRequest
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
    private fun uploadToS3(fileName: String, image: BufferedImage, scale: Double, isThumbnail: Boolean = false) {
        // settings
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

        // upload
        amazonS3.putObject(
            PutObjectRequest(bucketName, fileName, inputStream, metadata)
        )
    }

    @KafkaListener(topics = ["add-image"], groupId = "image-server-add-image")
    fun save(message: String) {
        val data = objectMapper.readValue(message, KafkaImageRequest::class.java)

        val originalImage = ImageIO.read(ByteArrayInputStream(data.image))

        uploadToS3(fileName = data.originalFileName, image = originalImage, scale = 1.0)
        uploadToS3(fileName = data.thumbnailFileName, image = originalImage, scale = 0.25, isThumbnail = true)
    }
}