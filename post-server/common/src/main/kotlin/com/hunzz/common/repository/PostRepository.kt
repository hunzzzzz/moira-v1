package com.hunzz.common.repository

import com.hunzz.common.model.Post
import com.hunzz.common.model.cache.PostInfo
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.*

@Repository
interface PostRepository : JpaRepository<Post, UUID> {
    @Query("SELECT new com.hunzz.common.model.cache.PostInfo(p.id, p.scope, p.status, p.content, p.imageUrl, p.thumbnailUrl) FROM Post p WHERE p.id = :postId")
    fun findPostInfo(postId: UUID): PostInfo?

    @Query("SELECT p.id FROM Post p WHERE p.status = 'DELETED' AND p.deleteAt = :now")
    fun findPostIdsByStatusAndDeletedAt(now: LocalDate): List<UUID>

    @Query("SELECT p.userId FROM Post p WHERE p.id = :postId")
    fun findPostAuthorId(postId: UUID): UUID?
}