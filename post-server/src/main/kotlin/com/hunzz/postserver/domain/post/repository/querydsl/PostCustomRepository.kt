package com.hunzz.postserver.domain.post.repository.querydsl

import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface PostCustomRepository {
    fun getAllIds(userId: UUID): List<Long>
}