package com.hunzz.relationserver.domain.repository

import com.hunzz.relationserver.domain.model.Relation
import com.hunzz.relationserver.domain.model.RelationId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RelationRepository : JpaRepository<Relation, RelationId>