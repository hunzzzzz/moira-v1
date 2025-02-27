package com.hunzz.api.domain.service

import com.hunzz.api.domain.dto.client.CommentInfo
import com.hunzz.api.domain.dto.client.PostInfo
import com.hunzz.api.domain.dto.client.UserInfo
import com.hunzz.api.domain.dto.response.CommentNotificationResponse
import com.hunzz.api.domain.dto.response.FollowNotificationResponse
import com.hunzz.api.domain.dto.response.LikeNotificationResponse
import com.hunzz.api.domain.dto.response.NotificationSliceResponse
import com.hunzz.api.domain.repository.NotificationCustomRepository
import com.hunzz.common.model.CommentNotification
import com.hunzz.common.model.FollowNotification
import com.hunzz.common.model.LikeNotification
import com.hunzz.common.model.NotificationType
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service

@Service
class NotificationGetService(
    private val notificationCustomRepository: NotificationCustomRepository,
    private val postInfoProvider: PostInfoProvider,
    private val relationInfoProvider: RelationInfoProvider,
    private val userInfoProvider: UserInfoProvider
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

        val convertedContents = contents.map { notification ->
            when (notification.type) {
                NotificationType.FOLLOW -> {
                    notification as FollowNotification

                    runBlocking {
                        val (userInfo, isFollowing) = withTimeout(5_000) {
                            // 유저 정보 조회 (Redis -> user-server 순)
                            val job1 = async {
                                userInfoProvider.getUserInfo(userId = notification.targetId)
                            }
                            // 내(actor)가 상대방(target)을 팔로우 했는지 확인
                            val job2 = async {
                                relationInfoProvider.isFollowing(
                                    actorId = notification.actorId,
                                    targetId = notification.targetId
                                )
                            }
                            awaitAll(job1, job2)
                        }

                        userInfo as UserInfo
                        isFollowing as Boolean

                        FollowNotificationResponse(
                            id = notification.id.toString(),
                            type = notification.type,
                            createdAt = notification.createdAt,
                            userId = userInfo.userId.toString(),
                            userName = userInfo.name,
                            userImageUrl = userInfo.thumbnailUrl,
                            isFollowing = isFollowing
                        )
                    }
                }

                NotificationType.LIKE -> {
                    notification as LikeNotification

                    runBlocking {
                        val (userInfo, postInfo) = withTimeout(5_000) {
                            // 유저 정보 조회 (Redis -> user-server 순)
                            val job1 = async {
                                val representativeId = notification.userIds.last()
                                userInfoProvider.getUserInfo(userId = representativeId)
                            }
                            // 게시글 정보 조회 (Redis -> post-server 순)
                            val job2 = async {
                                postInfoProvider.getPostInfo(postId = notification.postId)
                            }
                            awaitAll(job1, job2)
                        }

                        userInfo as UserInfo
                        postInfo as PostInfo

                        LikeNotificationResponse(
                            id = notification.id.toString(),
                            type = notification.type,
                            createdAt = notification.createdAt,
                            userId = userInfo.userId.toString(),
                            userName = userInfo.name,
                            userImageUrl = userInfo.thumbnailUrl,
                            postId = notification.postId,
                            postImageUrl = postInfo.thumbnailUrl,
                            numOfLikes = notification.userIds.size.toLong()
                        )
                    }
                }

                NotificationType.COMMENT -> {
                    notification as CommentNotification

                    runBlocking {
                        val (userInfo, postInfo, commentInfo) = withTimeout(5_000) {
                            // 유저 정보 조회 (Redis -> user-server 순)
                            val job1 = async {
                                userInfoProvider.getUserInfo(userId = notification.commentAuthorId)
                            }
                            // 게시글 정보 조회 (Redis -> post-server 순)
                            val job2 = async {
                                postInfoProvider.getPostInfo(postId = notification.postId)
                            }
                            // 댓글 정보 조회 (post-server)
                            val job3 = async {
                                postInfoProvider.getCommentInfo(commentId = notification.commentId)
                            }
                            awaitAll(job1, job2, job3)
                        }

                        userInfo as UserInfo
                        postInfo as PostInfo
                        commentInfo as CommentInfo

                        CommentNotificationResponse(
                            id = notification.id.toString(),
                            type = notification.type,
                            createdAt = notification.createdAt,
                            userId = userInfo.userId.toString(),
                            userName = userInfo.name,
                            userImageUrl = userInfo.thumbnailUrl,
                            postId = notification.postId,
                            postImageUrl = postInfo.thumbnailUrl,
                            commentContent = commentInfo.content
                        )
                    }
                }
            }
        }

        // 슬라이스 객체로 변환
        return NotificationSliceResponse(
            currentCursor = cursor,
            nextCursor = if (contents.size > PAGE_SIZE) contents.last().id.toString() else null,
            contents = convertedContents
        )
    }
}