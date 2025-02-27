package com.hunzz.api.domain.service

import com.hunzz.api.domain.dto.response.NotificationSliceResponse
import com.hunzz.api.domain.repository.NotificationCustomRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service

@Service
class NotificationGetService(
    private val notificationCustomRepository: NotificationCustomRepository,
) {
    companion object {
        const val PAGE_SIZE = 10
    }

    fun getNotifications(userId: String, cursor: String?): NotificationSliceResponse {
        // 알림 목록 조회
        val contents = notificationCustomRepository.getNotifications(
            pageable = PageRequest.ofSize(PAGE_SIZE),
            userId = userId,
            cursor = cursor
        )

        // 슬라이스 객체로 변환
        return NotificationSliceResponse(
            currentCursor = cursor,
            nextCursor = if (contents.size > PAGE_SIZE) contents.last().id.toString() else null,
            contents = contents
        )
    }
}