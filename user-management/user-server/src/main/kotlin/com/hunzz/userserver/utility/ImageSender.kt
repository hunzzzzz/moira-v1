package com.hunzz.userserver.utility

import com.hunzz.common.global.exception.ErrorCode.*
import com.hunzz.common.global.exception.custom.InternalSystemException
import com.hunzz.common.global.exception.custom.InvalidUserInfoException
import com.hunzz.userserver.dto.response.ImageInfo
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate
import org.springframework.web.multipart.MultipartFile

@Component
class ImageSender(
    @Value("\${gateway.server.url}")
    val gatewayServerUrl: String
) {
    companion object {
        val validExtensions = listOf("png", "jpg", "jpeg", "svg")
    }

    private fun checkValidImage(image: MultipartFile) {
        // check file
        if (image.isEmpty || image.originalFilename.isNullOrBlank()) {
            throw InvalidUserInfoException(INVALID_IMAGE_FILE)
        }

        // check extension
        val indexOfDot = image.originalFilename!!.lastIndexOf('.')
        val extension = image.originalFilename!!.substring(indexOfDot + 1)

        if (!validExtensions.contains(extension)) {
            throw InvalidUserInfoException(INVALID_IMAGE_EXTENSION)
        }
    }

    private fun getRequestEntity(image: MultipartFile): HttpEntity<LinkedMultiValueMap<String, Any>> {
        // headers
        val headers = HttpHeaders().apply {
            contentType = MediaType.MULTIPART_FORM_DATA
        }
        // body
        val body = LinkedMultiValueMap<String, Any>().apply {
            add("image", MultipartFileResource(file = image))
        }
        return HttpEntity(body, headers)
    }

    fun sendRequest(image: MultipartFile): ImageInfo {
        // validate
        checkValidImage(image = image)

        // send request
        return RestTemplate().postForObject(
            "${gatewayServerUrl}/image-server/images", // url
            getRequestEntity(image = image), // meta data
            ImageInfo::class.java // response type
        ) ?: throw InternalSystemException(IMAGE_SYSTEM_ERROR)
    }
}