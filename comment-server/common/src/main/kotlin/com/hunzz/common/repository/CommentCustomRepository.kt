package com.hunzz.common.repository

import com.hunzz.common.querydsl.dto.QueryDslCommentResponse
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface CommentCustomRepository {
    fun getComments(
        pageable: Pageable,
        postId: UUID,
        cursor: UUID?
    ): List<QueryDslCommentResponse>
}