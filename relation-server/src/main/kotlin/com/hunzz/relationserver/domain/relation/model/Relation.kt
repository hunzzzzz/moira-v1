package com.hunzz.relationserver.domain.relation.model

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import jakarta.persistence.Table
import java.util.*

@Entity
@IdClass(RelationId::class)
@Table(name = "relations")
class Relation(
    @Id
    val userId: UUID,

    @Id
    val targetId: UUID
)