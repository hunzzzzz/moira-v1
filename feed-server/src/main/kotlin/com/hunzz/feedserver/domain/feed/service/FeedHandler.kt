package com.hunzz.feedserver.domain.feed.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.hunzz.feedserver.domain.feed.dto.response.FeedResponse
import com.hunzz.feedserver.domain.feed.dto.response.FeedSliceResponse
import com.hunzz.feedserver.domain.feed.dto.response.kafka.AddPostKafkaResponse
import com.hunzz.feedserver.domain.feed.dto.response.kafka.FollowKafkaResponse
import com.hunzz.feedserver.domain.feed.repository.FeedRepository
import com.hunzz.feedserver.global.client.PostServerClient
import org.springframework.data.domain.PageRequest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.nio.ByteBuffer
import java.util.*

@Component
class FeedHandler(
    private val feedRedisHandler: FeedRedisHandler,
    private val feedRepository: FeedRepository,
    private val jdbcTemplate: JdbcTemplate,
    private val objectMapper: ObjectMapper,
    private val postServerClient: PostServerClient
) {
    private fun UUID.toBytes(): ByteArray {
        val byteBuffer = ByteBuffer.allocate(16)

        byteBuffer.putLong(this.mostSignificantBits)
        byteBuffer.putLong(this.leastSignificantBits)

        return byteBuffer.array()
    }

    fun getFeed(userId: UUID, cursor: Long?): FeedSliceResponse {
        // get feed data from db
        val feedData = feedRepository.getFeed(
            pageable = PageRequest.ofSize(5),
            userId = userId,
            cursor = cursor
        )

        if (feedData.isEmpty())
            return FeedSliceResponse(currentCursor = cursor, nextCursor = null, contents = listOf())

        // get user/post/like infos
        val userIds = feedData.map { it.userId }
        val userInfos = feedRedisHandler.getUserInfo(userIds = userIds)

        val postIds = feedData.map { it.postId }
        val postInfos = feedRedisHandler.getPostInfo(postIds = postIds)
        val likeInfos = feedRedisHandler.getLikeInfo(userId = userId, postIds = postIds)

        // combine
        val contents = feedData.mapIndexed { i, data ->
            val postId = data.postId
            val authorId = data.authorId
            val user = userInfos[i]
            val post = postInfos[i]
            val like = likeInfos[i]

            FeedResponse(
                postId = postId,
                postStatus = post.status,
                postScope = post.scope,
                postContent = post.content,
                userId = authorId,
                userName = user.name,
                userImageUrl = user.imageUrl,
                numOfLikes = like.likes,
                hasLike = like.hasLike
            )
        }

        return FeedSliceResponse(
            currentCursor = cursor,
            nextCursor = feedData.last().postId,
            contents = contents
        )
    }

    // 유저 A(author)가 게시글을 등록할 때, 유저 A를 팔로우하는 다른 유저(user)들의 큐에 해당 게시글을 등록한다.
    @KafkaListener(topics = ["add-post"], groupId = "feed-server-when-add-post")
    fun whenAddPost(message: String) {
        val data = objectMapper.readValue(message, AddPostKafkaResponse::class.java)

        // get followers
        val followers = feedRedisHandler.getFollowers(authorId = data.authorId)

        // batch insert (with jdbc template)
        val sql = "INSERT INTO feeds (user_id, post_id, author_id) VALUES (?, ?, ?)"

        jdbcTemplate.batchUpdate(sql, followers, 1000) { ps, userId ->
            ps.setBytes(1, UUID.fromString(userId).toBytes())
            ps.setLong(2, data.postId)
            ps.setBytes(3, data.authorId.toBytes())
        }
    }

    // 유저 A가 유저 B를 팔로우할 때, 유저 A(user)의 피드에 유저 B(author)의 게시글들이 추가된다.
    @KafkaListener(topics = ["follow"], groupId = "feed-server-when-follow")
    fun whenFollow(message: String) {
        val data = objectMapper.readValue(message, FollowKafkaResponse::class.java)

        // get postIds
        val postIds = feedRedisHandler.getLatestPostIds(authorId = data.targetId)

        // batch insert (with jdbc template)
        val sql = "INSERT INTO feeds (user_id, post_id, author_id) VALUES (?, ?, ?)"

        jdbcTemplate.batchUpdate(sql, postIds, 1000) { ps, postId ->
            ps.setBytes(1, data.userId.toBytes())
            ps.setLong(2, postId.toLong())
            ps.setBytes(3, data.targetId.toBytes())
        }
    }

    // 유저 A가 유저 B를 언팔로우할 때, 유저 A(user)의 피드에 유저 B(author)의 게시글들이 삭제된다.
    @KafkaListener(topics = ["unfollow"], groupId = "feed-server-when-unfollow")
    @Transactional
    fun whenUnfollow(message: String) {
        val data = objectMapper.readValue(message, FollowKafkaResponse::class.java)

        // delete feed
        feedRepository.deleteAllByUserIdAndAuthorId(userId = data.userId, authorId = data.targetId)
    }
}