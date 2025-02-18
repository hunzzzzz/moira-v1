package com.hunzz.postserver.domain.comment.repository.querydsl

import com.hunzz.postserver.domain.comment.dto.response.CommentQueryDslResponse
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository

@Repository
interface CommentCustomRepository {
    fun getComments(
        pageable: Pageable,
        postId: Long,
        cursor: Long?
    ): List<CommentQueryDslResponse>
}