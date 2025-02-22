package com.hunzz.imageserver.service

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.PutObjectRequest
import com.hunzz.imageserver.dto.ImageResponse
import net.coobird.thumbnailator.Thumbnails
import net.coobird.thumbnailator.geometry.Positions
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.*
import javax.imageio.ImageIO

@Component
class ImageHandler(
    @Value("\${cloud.aws.s3.bucketName}")
    private val bucketName: String,

    private val amazonS3: AmazonS3,
) {
    private fun getImageUrl(imageId: UUID, isThumbnail: Boolean = false): String {
        val fileName = if (isThumbnail) "${imageId}-thumbnail.jpg" else "${imageId}.jpg"

        return "https://${bucketName}.s3.amazonaws.com/$fileName"
    }

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

    fun save(image: MultipartFile): ImageResponse {
        // settings
        val imageId = UUID.randomUUID()
        val originalImage = ImageIO.read(image.inputStream)

        // upload original image
        val originalImageFileName = "${imageId}.jpg"
        uploadToS3(fileName = originalImageFileName, image = originalImage, scale = 1.0)

        // upload thumbnail
        val thumbnailImageFileName = "${imageId}-thumbnail.jpg"
        uploadToS3(fileName = thumbnailImageFileName, image = originalImage, scale = 0.25)

        return ImageResponse(
            imageId = imageId,
            imageUrl = getImageUrl(imageId = imageId),
            thumbnailUrl = getImageUrl(imageId = imageId, isThumbnail = true)
        )
    }
}