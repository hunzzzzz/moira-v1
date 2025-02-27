package com.hunzz.api.domain.repository

import com.hunzz.common.model.Notification
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository

@Repository
interface NotificationCustomRepository {
    fun getNotifications(
        pageable: Pageable,
        userId: String,
        cursor: String?
    ): List<Notification>
}