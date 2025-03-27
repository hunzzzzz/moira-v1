package com.hunzz.common.repository

import com.hunzz.common.model.Post
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.*

@Repository
interface PostRepository : JpaRepository<Post, UUID>, PostCustomRepository {
    @Query("SELECT p.id FROM Post p WHERE p.status = 'DELETED' AND p.deleteAt = :now")
    fun findPostIdsByStatusAndDeletedAt(now: LocalDate): List<UUID>

    @Query("SELECT p.userId FROM Post p WHERE p.id = :postId")
    fun findPostAuthorId(postId: UUID): UUID?

    @Query("SELECT p.id FROM Post p WHERE p.userId = :userId ORDER BY p.createdAt DESC LIMIT 10")
    fun getLatestPosts(userId: UUID): List<UUID>
}