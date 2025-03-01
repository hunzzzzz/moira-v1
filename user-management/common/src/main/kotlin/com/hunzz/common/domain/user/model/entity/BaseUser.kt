package com.hunzz.common.domain.user.model.entity

import com.hunzz.common.domain.user.model.property.UserRole
import com.hunzz.common.domain.user.model.property.UserStatus
import com.hunzz.common.domain.user.model.property.UserType
import jakarta.persistence.Column
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import java.time.LocalDateTime
import java.util.*

@MappedSuperclass
abstract class BaseUser(
    @Id
    @Column(name = "user_id", nullable = false, unique = true)
    open val id: UUID,

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    val type: UserType,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: UserStatus = UserStatus.NORMAL,

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    open val role: UserRole = UserRole.USER,

    @Column(name = "email", nullable = false, unique = true)
    open val email: String,

    @Column(name = "name", nullable = false)
    open var name: String,

    @Column(name = "image_url", nullable = true)
    var imageUrl: String? = null,

    @Column(name = "thumbnail_url", nullable = true)
    var thumbnailUrl: String? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    fun updateImage(imageUrl: String, thumbnailUrl: String) {
        this.imageUrl = imageUrl
        this.thumbnailUrl = thumbnailUrl
    }
}