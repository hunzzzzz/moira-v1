package com.hunzz.relationserver.domain.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.hunzz.relationserver.domain.component.RelationRedisHandler
import com.hunzz.relationserver.domain.dto.response.FollowResponse
import com.hunzz.relationserver.domain.dto.response.FollowSliceResponse
import com.hunzz.relationserver.domain.model.RelationType
import com.hunzz.relationserver.domain.repository.RelationRepository
import com.hunzz.relationserver.utility.client.UserServerClient
import com.hunzz.relationserver.utility.exception.ErrorCode.FEIGN_CLIENT_ERROR
import com.hunzz.relationserver.utility.exception.custom.InternalSystemError
import org.springframework.stereotype.Component
import java.util.*

@Component
class GetRelationsService(
    private val objectMapper: ObjectMapper,
    private val relationRedisHandler: RelationRedisHandler,
    private val relationRepository: RelationRepository,
    private val userServerClient: UserServerClient
) {
    companion object {
        const val RELATION_PAGE_SIZE = 10L
    }

    fun <T> retry(
        logic: () -> T
    ): T {
        var retryCount = 0
        val maxRetries = 3

        while (retryCount < maxRetries) {
            try {
                return logic()
            } catch (e: Exception) {
                retryCount++
                if (retryCount == maxRetries) throw e

                Thread.sleep(1000)
            }
        }

        throw InternalSystemError(FEIGN_CLIENT_ERROR)
    }

    fun getRelations(userId: UUID, cursor: UUID?, type: RelationType): FollowSliceResponse {
        // Redis 내 커서 기반 페이징 + 캐시 조회
        val result = try {
            relationRedisHandler.getRelations(
                userId = userId,
                cursor = cursor,
                type = type,
                pageSize = RELATION_PAGE_SIZE
            )
        } catch (e: Exception) {
            null
        }

        // Redis 결과에 문제가 발생한 경우에는 DB를 조회한다.
        val followResponses = if (
            result == null
            || result.count { it.startsWith("NULL:") } > result.size * 0.9 // NULL 비율 90% 초과 시
        ) {
            relationRepository.findRelations(
                userId = userId,
                cursor = cursor,
                type = type
            )
        } else {
            // Redis에 캐시 정보가 없는 유저들의 userId 추출
            val missingIds = result
                .filter { it.startsWith("NULL:") }
                .map { UUID.fromString(it.substring(5)) }

            // Redis에 캐시 정보가 없는 유저들의 userId 리스트를 user-cache 서버로 전송하여 유저 정보 조회
            var missingUserInfos = hashMapOf<UUID, FollowResponse>()

            if (missingIds.isNotEmpty())
                missingUserInfos = retry { userServerClient.getUsers(missingIds = missingIds) }

            // 조합
            result.map {
                if (it.startsWith("NULL:")) {
                    val id = UUID.fromString(it.substring(5))

                    missingUserInfos[id]
                } else objectMapper.readValue(it, FollowResponse::class.java)
            }
        }

        // 커서 정보 추가
        return FollowSliceResponse(
            nextCursor = if (followResponses.size <= RELATION_PAGE_SIZE) null else followResponses.lastOrNull()?.userId,
            contents = followResponses
        )
    }
}