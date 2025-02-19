package com.hunzz.postserver.domain.post.repository

import com.hunzz.postserver.domain.post.model.Post
import com.hunzz.postserver.domain.post.repository.querydsl.PostCustomRepository
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PostRepository : JpaRepository<Post, Long>, PostCustomRepository