package com.hunzz.moirav1.domain.relation.service

import com.hunzz.moirav1.domain.relation.dto.response.FollowResponse
import com.hunzz.moirav1.domain.relation.model.Relation
import com.hunzz.moirav1.domain.relation.model.RelationId
import com.hunzz.moirav1.domain.relation.model.RelationType
import com.hunzz.moirav1.domain.relation.repository.RelationRepository
import com.hunzz.moirav1.domain.user.service.UserHandler
import com.hunzz.moirav1.global.exception.ErrorMessages.ALREADY_FOLLOWED
import com.hunzz.moirav1.global.exception.ErrorMessages.ALREADY_UNFOLLOWED
import com.hunzz.moirav1.global.exception.ErrorMessages.CANNOT_FOLLOW_ITSELF
import com.hunzz.moirav1.global.exception.ErrorMessages.CANNOT_UNFOLLOW_ITSELF
import com.hunzz.moirav1.global.exception.ErrorMessages.USER_NOT_FOUND
import com.hunzz.moirav1.global.exception.custom.InvalidUserInfoException
import com.hunzz.moirav1.global.utility.RedisCommands
import com.hunzz.moirav1.global.utility.RedisKeyProvider
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Slice
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Component
class RelationHandler(
    private val redisCommands: RedisCommands,
    private val redisKeyProvider: RedisKeyProvider,
    private val relationRepository: RelationRepository,
    private val userHandler: UserHandler
) {
    companion object {
        const val RELATION_PAGE_SIZE = 10
    }

    private fun isExistingUser(userId: UUID) {
        val condition = userHandler.isUser(userId = userId)

        require(condition) { throw InvalidUserInfoException(USER_NOT_FOUND) }
    }

    private fun isNotFollowingItself(userId: UUID, targetId: UUID, isUnfollow: Boolean) {
        val condition = userId != targetId

        require(condition) {
            throw InvalidUserInfoException(
                message = if (isUnfollow) CANNOT_UNFOLLOW_ITSELF else CANNOT_FOLLOW_ITSELF
            )
        }
    }

    private fun isNotAlreadyFollowed(followingKey: String, targetId: UUID, isUnfollow: Boolean) {
        val condition = (redisCommands.zScore(key = followingKey, value = targetId.toString()) == null)
            .let { if (isUnfollow) !it else it }

        require(condition) {
            throw InvalidUserInfoException(
                message = if (isUnfollow) ALREADY_UNFOLLOWED else ALREADY_FOLLOWED
            )
        }
    }

    @Transactional
    fun follow(userId: UUID, targetId: UUID, isUnfollow: Boolean = false) {
        // settings
        val followingKey = redisKeyProvider.following(userId = userId)
        val followerKey = redisKeyProvider.follower(userId = targetId)
        val now = System.currentTimeMillis().toDouble()

        // validate
        isExistingUser(userId = targetId)
        isNotFollowingItself(userId = userId, targetId = targetId, isUnfollow = isUnfollow)
        isNotAlreadyFollowed(followingKey = followingKey, targetId = targetId, isUnfollow = isUnfollow)

        // save or delete
        if (isUnfollow) {
            // delete (redis)
            redisCommands.zRem(key = followingKey, value = targetId.toString())
            redisCommands.zRem(key = followerKey, value = userId.toString())

            // delete (db)
            val userRelationId = RelationId(userId = userId, targetId = targetId)
            relationRepository.deleteById(userRelationId)
        } else {
            // save (redis)
            redisCommands.zAdd(key = followingKey, value = targetId.toString(), score = now)
            redisCommands.zAdd(key = followerKey, value = userId.toString(), score = now)

            // save (db)
            val userRelation = Relation(userId = userId, targetId = targetId)
            relationRepository.save(userRelation)
        }
    }

    fun getRelations(userId: UUID, cursor: LocalDateTime?, type: RelationType): Slice<FollowResponse> {
        // settings
        val pageable = PageRequest.ofSize(RELATION_PAGE_SIZE)

        // querydsl
        return relationRepository.getRelations(
            pageable = pageable,
            userId = userId,
            cursor = cursor,
            type = type
        )
    }
}