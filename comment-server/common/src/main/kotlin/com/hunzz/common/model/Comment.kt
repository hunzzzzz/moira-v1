package com.hunzz.common.model

import com.hunzz.common.model.property.BaseTime
import com.hunzz.common.model.property.CommentStatus
import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "comments")
class Comment(
    @Id
    @Column(name = "comment_id", nullable = false, unique = true)
    val id: UUID = UUID.randomUUID(),

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: CommentStatus = CommentStatus.NORMAL,

    @Column(name = "content", nullable = false)
    var content: String,

    @Column(name = "user_id", nullable = false)
    val userId: UUID,

    @Column(name = "post_id", nullable = false)
    val postId: UUID
) : BaseTime() {
    fun delete() {
        this.status = CommentStatus.DELETED
    }
}