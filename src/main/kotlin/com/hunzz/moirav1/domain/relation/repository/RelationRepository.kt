package com.hunzz.moirav1.domain.relation.repository

import com.hunzz.moirav1.domain.relation.model.Relation
import com.hunzz.moirav1.domain.relation.model.RelationId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RelationRepository : JpaRepository<Relation, RelationId>