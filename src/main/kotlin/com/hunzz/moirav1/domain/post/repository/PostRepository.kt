package com.hunzz.moirav1.domain.post.repository

import com.hunzz.moirav1.domain.post.model.Post
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface PostRepository : JpaRepository<Post, Long> {
    fun findAllByUserId(userId: UUID): List<Post>
}