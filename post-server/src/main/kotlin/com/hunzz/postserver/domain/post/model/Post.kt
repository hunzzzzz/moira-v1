package com.hunzz.postserver.domain.post.model

import com.hunzz.postserver.domain.post.dto.request.PostRequest
import com.hunzz.postserver.global.model.BaseTime
import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "posts")
class Post(
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: PostStatus = PostStatus.NORMAL,

    @Enumerated(EnumType.STRING)
    @Column(name = "scope", nullable = false)
    var scope: PostScope,

    @Column(name = "content", nullable = false, length = 500)
    var content: String,

    @Column(name = "image_url", nullable = true)
    val imageUrl: String?,

    @Column(name = "thumbnail_url", nullable = true)
    val thumbnailUrl: String?,

    @Column(name = "user_id", nullable = false)
    val userId: UUID
) : BaseTime() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id", nullable = false, unique = true)
    val id: Long? = null

    fun update(request: PostRequest) {
        this.scope = request.scope.let { PostScope.valueOf(it!!) }
        this.content = request.content!!
    }

    fun delete() {
        this.status = PostStatus.DELETED
    }
}