package com.hunzz.relationserver.domain.relation.service

import com.hunzz.relationserver.domain.relation.repository.RelationRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class RelationScheduler(
    private val relationRedisScriptHandler: RelationRedisScriptHandler,
    private val relationRepository: RelationRepository
) {
    @Scheduled(fixedRate = 1000 * 10)
    fun checkFollowQueue() {
        val relations = relationRedisScriptHandler.checkFollowQueue()

        // save 'relations' in db
        relationRepository.saveAll(relations)
    }

    @Scheduled(fixedRate = 1000 * 10)
    fun checkUnfollowQueue() {
        val relationIds = relationRedisScriptHandler.checkUnfollowQueue()

        // delete 'relations' in db
        relationRepository.deleteAllById(relationIds)
    }
}