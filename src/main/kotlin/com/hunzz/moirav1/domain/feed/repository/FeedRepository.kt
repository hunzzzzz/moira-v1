package com.hunzz.moirav1.domain.feed.repository

import com.hunzz.moirav1.domain.feed.model.Feed
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface FeedRepository : JpaRepository<Feed, Long>