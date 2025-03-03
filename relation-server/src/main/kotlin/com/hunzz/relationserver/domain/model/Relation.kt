package com.hunzz.relationserver.domain.model

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@IdClass(RelationId::class)
@Table(name = "relations")
class Relation(
    @Id
    val userId: UUID,

    @Id
    val targetId: UUID,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
)