package com.hunzz.relationserver.global.client

import com.hunzz.relationserver.domain.relation.dto.response.FollowResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import java.util.*

@FeignClient(
    name = "user-server",
    url = "http://localhost:8080/user-server/private" // TODO : 추후 환경 변수로 분리
)
interface UserServerClient {
    @GetMapping("/users/{userId}")
    fun getUser(
        @PathVariable userId: UUID
    ): FollowResponse

    @GetMapping("/users/all")
    fun getUsers(
        @RequestParam missingIds: List<UUID>
    ): HashMap<UUID, FollowResponse>
}