package com.hunzz.api.domain.controller

import com.hunzz.api.domain.dto.response.NotificationSliceResponse
import com.hunzz.api.domain.service.NotificationGetService
import com.hunzz.api.utility.auth.AuthPrincipal
import com.hunzz.common.model.Notification
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/notifications")
class NotificationController(
    private val notificationGetService: NotificationGetService
) {
    @GetMapping
    fun getAll(
        @AuthPrincipal userId: String,
        @RequestParam(required = false) cursor: String?
    ): ResponseEntity<NotificationSliceResponse> {
        val body = notificationGetService.getNotifications(
            userId = userId,
            cursor = cursor
        )

        return ResponseEntity.ok(body)
    }
}