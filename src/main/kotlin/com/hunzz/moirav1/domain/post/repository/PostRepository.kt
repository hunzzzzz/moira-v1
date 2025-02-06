package com.hunzz.moirav1.domain.post.repository

import com.hunzz.moirav1.domain.post.model.Post
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PostRepository : JpaRepository<Post, Long>