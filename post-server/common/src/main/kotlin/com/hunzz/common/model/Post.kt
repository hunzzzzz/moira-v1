package com.hunzz.common.model

import com.hunzz.common.model.property.BaseTime
import com.hunzz.common.model.property.PostScope
import com.hunzz.common.model.property.PostStatus
import jakarta.persistence.*
import java.time.LocalDate
import java.util.*

@Entity
@Table(name = "posts")
class Post(
    @Id
    @Column(name = "post_id", nullable = false, unique = true)
    val id: UUID = UUID.randomUUID(),

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: PostStatus = PostStatus.NORMAL,

    @Enumerated(EnumType.STRING)
    @Column(name = "scope", nullable = false)
    var scope: PostScope,

    @Column(name = "content", nullable = false, length = 500)
    var content: String,

    @Convert(converter = StringListConverter::class)
    @Column(name = "image_urls", nullable = true)
    val imageUrls: List<String>?,

    @Column(name = "thumbnail_url", nullable = true)
    val thumbnailUrl: String?,

    @Column(name = "user_id", nullable = false)
    val userId: UUID,

    @Column(name = "delete_at", nullable = true)
    var deleteAt: LocalDate? = null,

    @Column(name = "tx_id", nullable = false)
    val txId: UUID
) : BaseTime() {
    fun update(content: String, scope: PostScope) {
        this.content = content
        this.scope = scope
    }

    fun delete() {
        this.status = PostStatus.DELETED
        this.deleteAt = LocalDate.now().plusDays(90)
    }
}