package com.hunzz.moirav1.domain.feed.repository

import com.hunzz.moirav1.domain.feed.model.Feed
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface FeedRepository : JpaRepository<Feed, Long> {
    fun existsByUserIdAndPostIdAndAuthorId(userId: UUID, postId: Long, authorId: UUID): Boolean
    fun findAllByUserId(userId: UUID): List<Feed>
    fun findAllByUserIdAndAuthorId(userId: UUID, authorId: UUID): List<Feed>
    fun deleteAllByUserIdAndAuthorId(userId: UUID, authorId: UUID)
}