package com.hunzz.moirav1.domain.feed.service

import com.hunzz.moirav1.domain.feed.model.Feed
import com.hunzz.moirav1.domain.feed.repository.FeedRepository
import com.hunzz.moirav1.domain.post.repository.PostRepository
import com.hunzz.moirav1.global.utility.RedisCommands
import com.hunzz.moirav1.global.utility.RedisKeyProvider
import org.springframework.stereotype.Component
import java.util.*

@Component
class FeedEventHandler(
    private val feedRepository: FeedRepository,
    private val postRepository: PostRepository,
    private val redisCommands: RedisCommands,
    private val redisKeyProvider: RedisKeyProvider
) {
    // 유저 A(author)가 게시글을 등록할 때, 유저 A를 팔로우하는 다른 유저(user)들의 큐에 해당 게시글을 등록한다.
    fun whenAddPost(authorId: UUID, postId: Long) {
        // settings
        val followerKey = redisKeyProvider.follower(userId = authorId)
        val followers = redisCommands.zRange(key = followerKey, start = 0, end = -1)

        // add 'feed' in db
        followers.forEach {
            val postQueue = Feed(
                userId = UUID.fromString(it),
                postId = postId,
                authorId = authorId
            )

            feedRepository.save(postQueue)
        }
    }

    // 유저 A가 유저 B를 팔로우할 때, 유저 A(user)의 피드에 유저 B(author)의 게시글들이 추가된다.
    fun whenFollow(userId: UUID, authorId: UUID) {
        // get postIds of user B
        val postIds = postRepository.findAllByUserId(userId = authorId)

        // add 'feed' in db
        postIds.forEach {
            val postQueue = Feed(
                userId = userId,
                postId = it.id!!,
                authorId = authorId
            )

            feedRepository.save(postQueue)
        }
    }

    // 유저 A가 유저 B를 언팔로우할 때, 유저 A(user)의 피드에 유저 B(author)의 게시글들이 삭제된다.
    fun whenUnfollow(userId: UUID, authorId: UUID) {
        feedRepository.deleteAllByUserIdAndAuthorId(userId = userId, authorId = authorId)
    }
}