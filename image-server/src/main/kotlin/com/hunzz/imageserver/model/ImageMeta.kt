package com.hunzz.imageserver.model

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
class ImageMeta(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_meta_id", nullable = false, unique = true)
    val id: Long? = null,

    @Column(name = "tx_id", nullable = false)
    val txId: UUID,

    @Column(name = "user_id", nullable = false)
    val userId: UUID,

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    val type: ImageType,

    @Column(name = "is_thumbnail", nullable = false)
    val isThumbnail: Boolean,

    @Column(name = "original_file_name", nullable = false)
    val originalFileName: String,

    @Column(name = "uploaded_at", nullable = false)
    val uploadedAt: LocalDateTime = LocalDateTime.now()
)