package com.hunzz.postserver.domain.comment.model

import com.hunzz.postserver.domain.comment.dto.request.CommentRequest
import com.hunzz.postserver.global.model.BaseTime
import jakarta.persistence.*
import java.util.*

@Entity
class Comment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id", nullable = false, unique = true)
    val id: Long? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: CommentStatus = CommentStatus.NORMAL,

    @Column(name = "content", nullable = false)
    var content: String,

    @Column(name = "user_id", nullable = false)
    val userId: UUID,

    @Column(name = "post_id", nullable = false)
    val postId: Long
) : BaseTime() {
    fun update(request: CommentRequest) {
        this.content = request.content!!
    }

    fun delete() {
        this.status = CommentStatus.DELETED
    }
}