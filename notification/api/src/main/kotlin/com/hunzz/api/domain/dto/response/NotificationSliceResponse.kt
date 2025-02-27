package com.hunzz.api.domain.dto.response

data class NotificationSliceResponse(
    val currentCursor: String?,
    val nextCursor: String?,
    val contents: List<NotificationResponse>
)
