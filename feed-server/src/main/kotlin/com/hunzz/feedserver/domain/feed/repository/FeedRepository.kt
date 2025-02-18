package com.hunzz.feedserver.domain.feed.repository

import com.hunzz.feedserver.domain.feed.model.Feed
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface FeedRepository : JpaRepository<Feed, Long>