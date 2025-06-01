package com.hunzz.relationserver.domain.repository

import com.hunzz.relationserver.domain.model.Relation
import com.hunzz.relationserver.domain.model.RelationId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface RelationRepository : JpaRepository<Relation, RelationId>, RelationCustomRepository {
    fun countByUserId(userId: UUID): Long
    fun countByTargetId(targetId: UUID): Long

    @Query("SELECT r.targetId FROM Relation r WHERE r.userId = :userId")
    fun findFollowingsByUserId(userId: UUID): List<UUID>

    @Query("SELECT r.userId FROM Relation r WHERE r.targetId = :userId")
    fun findFollowersByUserId(userId: UUID): List<UUID>
}