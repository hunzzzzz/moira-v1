package com.hunzz.api.service

import com.hunzz.api.client.dto.UserInfo
import com.hunzz.api.component.CommentRedisHandler
import com.hunzz.api.dto.response.CommentResponse
import com.hunzz.api.dto.response.CommentSliceResponse
import com.hunzz.common.repository.CommentRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withTimeout
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import java.util.*

@Service
class GetCommentsService(
    private val commentRedisHandler: CommentRedisHandler,
    private val commentRepository: CommentRepository
) {
    companion object {
        const val COMMENT_PAGE_SIZE = 10
    }

    suspend fun getComments(userId: UUID, postId: UUID, cursor: UUID?): CommentSliceResponse =
        coroutineScope {
            val pageable = PageRequest.ofSize(COMMENT_PAGE_SIZE)

            // QueryDsl 활용 DB 조회
            val contentsFromDB = commentRepository.getComments(
                pageable = pageable,
                postId = postId,
                cursor = cursor
            )

            if (contentsFromDB.isEmpty())
                return@coroutineScope CommentSliceResponse(
                    currentCursor = cursor, nextCursor = null, contents = listOf<CommentResponse>()
                )

            // 추가 데이터 조회
            var userInfos = listOf<UserInfo>()
            var postAuthorId = UUID.randomUUID()

            withTimeout(5_000) {
                // 작업1: 유저 정보 조회
                val job1 = async {
                    val userIds = contentsFromDB.map { it.userId }

                    userInfos = commentRedisHandler.getUserInfos(userIds = userIds)
                }
                // 작업2: 작성자 정보 조회
                val job2 = async {
                    postAuthorId = commentRedisHandler.getPostAuthorId(postId = postId)
                }

                awaitAll(job1, job2)
            }

            val contents = contentsFromDB.mapIndexed { index, data ->
                val userInfo = userInfos[index]

                CommentResponse(
                    commentId = data.commentId,
                    commentContent = data.content,
                    commentCreatedAt = data.createdAt,
                    userId = userInfo.userId,
                    userName = userInfo.name,
                    userThumbnailUrl = userInfo.thumbnailUrl,
                    isMyComment = userId == userInfo.userId,
                    isPostAuthor = postAuthorId == userInfo.userId
                )
            }

            // 커서 정보 포함
            return@coroutineScope CommentSliceResponse(
                currentCursor = cursor,
                nextCursor = contentsFromDB.lastOrNull()?.commentId,
                contents = contents
            )
        }
}