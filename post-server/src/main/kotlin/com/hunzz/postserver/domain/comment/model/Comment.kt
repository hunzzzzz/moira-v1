package com.hunzz.postserver.domain.comment.model

import com.hunzz.postserver.domain.post.model.Post
import jakarta.persistence.*
import java.util.*

@Entity
class Comment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id", nullable = false, unique = true)
    val id: Long? = null,

    @Column(name = "content", nullable = false)
    val content: String,

    @Column(name = "user_id", nullable = false)
    val userId: UUID,

    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    val post: Post
)