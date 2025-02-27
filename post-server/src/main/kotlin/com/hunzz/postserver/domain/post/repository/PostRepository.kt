package com.hunzz.postserver.domain.post.repository

import com.hunzz.postserver.domain.post.model.Post
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface PostRepository : JpaRepository<Post, Long> {
    @Query("SELECT p.userId FROM Post p WHERE p.id = :postId")
    fun getUserIdFromPostId(postId: Long): UUID
}