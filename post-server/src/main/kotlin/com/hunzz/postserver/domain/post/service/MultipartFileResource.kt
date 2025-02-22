package com.hunzz.postserver.domain.post.service

import org.springframework.core.io.InputStreamResource
import org.springframework.web.multipart.MultipartFile
import java.io.InputStream

class MultipartFileResource(private val file: MultipartFile) : InputStreamResource(file.inputStream) {
    override fun getInputStream(): InputStream = file.inputStream
    override fun getFilename(): String = file.originalFilename ?: "unknown"
    override fun contentLength(): Long = file.size
    override fun getDescription(): String = "MultipartFile resource"
}