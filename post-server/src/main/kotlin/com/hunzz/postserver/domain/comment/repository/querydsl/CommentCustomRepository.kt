package com.hunzz.postserver.domain.comment.repository.querydsl

import com.hunzz.postserver.domain.comment.dto.response.CommentQueryDslResponse
import org.springframework.stereotype.Repository

@Repository
interface CommentCustomRepository {
    fun getComments(
        postId: Long
    ): List<CommentQueryDslResponse>
}