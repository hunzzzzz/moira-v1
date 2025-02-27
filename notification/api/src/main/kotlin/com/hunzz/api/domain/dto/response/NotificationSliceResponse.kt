package com.hunzz.api.domain.dto.response

import com.hunzz.common.model.Notification

data class NotificationSliceResponse(
    val currentCursor: String?,
    val nextCursor: String?,
    val contents: List<Notification>
)
