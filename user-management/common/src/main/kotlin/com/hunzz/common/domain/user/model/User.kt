package com.hunzz.common.domain.user.model

import com.hunzz.common.global.model.BaseTime
import jakarta.persistence.*
import org.hibernate.annotations.UuidGenerator
import java.util.*

@Entity
@Table(name = "users")
class User(
    @Id
    @UuidGenerator
    @Column(name = "user_id", nullable = false, unique = true)
    val id: UUID? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: UserStatus = UserStatus.NORMAL,

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    val role: UserRole = UserRole.USER,

    @Column(name = "email", nullable = false, unique = true)
    val email: String,

    @Column(name = "password", nullable = false)
    val password: String,

    @Column(name = "name", nullable = false)
    val name: String,

    @Column(name = "image_url", nullable = true)
    var imageUrl: String? = null,

    @Column(name = "thumbnail_url", nullable = true)
    var thumbnailUrl: String? = null
) : BaseTime() {
    fun updateImage(imageUrl: String, thumbnailUrl: String) {
        this.imageUrl = imageUrl
        this.thumbnailUrl = thumbnailUrl
    }
}