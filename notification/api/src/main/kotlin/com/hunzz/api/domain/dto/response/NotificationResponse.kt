package com.hunzz.api.domain.dto.response

import com.hunzz.common.model.NotificationType
import java.time.LocalDateTime

abstract class NotificationResponse(
    open val id: String,
    open val type: NotificationType,
    open val createdAt: LocalDateTime
)
