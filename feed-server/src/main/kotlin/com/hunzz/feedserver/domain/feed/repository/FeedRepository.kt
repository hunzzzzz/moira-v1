package com.hunzz.feedserver.domain.feed.repository

import com.hunzz.feedserver.domain.feed.model.Feed
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface FeedRepository : JpaRepository<Feed, Long> {
    fun deleteAllByUserIdAndAuthorId(userId: UUID, authorId: UUID)
}