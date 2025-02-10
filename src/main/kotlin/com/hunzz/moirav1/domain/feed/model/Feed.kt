package com.hunzz.moirav1.domain.feed.model

import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "feeds")
class Feed(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feed_id", nullable = false, unique = true)
    val id: Long? = null,

    @Column(name = "user_id", nullable = false)
    val userId: UUID,

    @Column(name = "post_id", nullable = false)
    val postId: Long,

    @Column(name = "author_id", nullable = false)
    val authorId: UUID
)