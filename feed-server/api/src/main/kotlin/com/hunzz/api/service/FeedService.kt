package com.hunzz.api.service

import com.hunzz.api.client.dto.PostInfo
import com.hunzz.api.client.dto.UserInfo
import com.hunzz.api.component.FeedRedisHandler
import com.hunzz.api.dto.response.FeedLikeResponse
import com.hunzz.api.dto.response.FeedResponse
import com.hunzz.api.dto.response.FeedSliceResponse
import com.hunzz.common.repository.FeedRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withTimeout
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import java.util.*

@Service
class FeedService(
    private val feedRedisHandler: FeedRedisHandler,
    private val feedRepository: FeedRepository
) {
    companion object {
        const val FEED_PAGE_SIZE = 5
    }

    suspend fun getFeed(userId: UUID, cursor: Long?) = coroutineScope {
        // QueryDsl을 활용하여 DB에서 피드 기본 정보 조회
        val feedData = feedRepository.getFeed(
            pageable = PageRequest.ofSize(FEED_PAGE_SIZE),
            userId = userId,
            cursor = cursor
        )

        // 데이터가 없다면 다음 커서값을 null로 리턴
        if (feedData.isEmpty())
            return@coroutineScope FeedSliceResponse(
                currentCursor = cursor,
                nextCursor = null,
                contents = listOf()
            )

        // 피드 조회에 필요한 데이터들을 각각 가져오기
        val (userInfos, postInfos, likeInfos, _) = withTimeout(5_000) {
            // 작업1: 유저 정보 획득
            val job1 = async {
                feedRedisHandler.getUserInfos(userIds = feedData.map { it.authorId })
            }
            // 작업2: 게시글 정보 획득
            val job2 = async {
                feedRedisHandler.getPostInfos(postIds = feedData.map { it.postId })
            }
            // 작업3: 좋아요 정보 획득
            val job3 = async {
                feedRedisHandler.getLikeInfos(userId = userId, postIds = feedData.map { it.postId })
            }
            // 작업4: 피드 읽음 처리
            val job4 = async {
                feedRedisHandler.readFeed(feedIds = feedData.map { it.feedId })
            }
            awaitAll(job1, job2, job3, job4)
        }

        userInfos as List<*>
        postInfos as List<*>
        likeInfos as List<*>

        // 데이터 병합
        val contents = feedData.mapIndexed { index, data ->
            val user = userInfos[index] as UserInfo
            val post = postInfos[index] as PostInfo
            val like = likeInfos[index] as FeedLikeResponse

            FeedResponse(
                feedId = data.feedId,
                postId = post.postId,
                postScope = post.scope,
                postStatus = post.status,
                postContent = post.content,
                postImageUrl = post.imageUrl,
                postThumbnailUrl = post.thumbnailUrl,
                userId = user.userId,
                userName = user.name,
                userImageUrl = user.imageUrl,
                userThumbnailUrl = user.thumbnailUrl,
                numOfLikes = like.likes,
                hasLike = like.hasLike == 1
            )
        }

        return@coroutineScope FeedSliceResponse(
            currentCursor = cursor,
            nextCursor = feedData.last().feedId,
            contents = contents
        )
    }
}