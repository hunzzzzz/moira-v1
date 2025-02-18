package com.hunzz.postserver.domain.comment.repository

import com.hunzz.postserver.domain.comment.model.Comment
import com.hunzz.postserver.domain.comment.repository.querydsl.CommentCustomRepository
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CommentRepository : JpaRepository<Comment, Long>, CommentCustomRepository