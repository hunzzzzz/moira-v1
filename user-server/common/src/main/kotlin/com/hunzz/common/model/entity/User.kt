package com.hunzz.common.model.entity

import com.hunzz.common.model.property.UserRole
import com.hunzz.common.model.property.UserStatus
import com.hunzz.common.model.property.UserType
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "users")
class User(
    @Id
    @Column(name = "user_id", nullable = false, unique = true)
    val id: UUID = UUID.randomUUID(),

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    val type: UserType,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: UserStatus = UserStatus.NORMAL,

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    val role: UserRole = UserRole.USER,

    @Column(name = "email", nullable = false, unique = true)
    val email: String,

    @Column(name = "password", nullable = true)
    val password: String?,

    @Column(name = "name", nullable = false)
    val name: String,

    @Column(name = "image_url", nullable = true)
    var imageUrl: String? = null,

    @Column(name = "thumbnail_url", nullable = true)
    var thumbnailUrl: String? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    fun updateImageUrls(imageUrl: String, thumbnailUrl: String) {
        this.imageUrl = imageUrl
        this.thumbnailUrl = thumbnailUrl
    }
}