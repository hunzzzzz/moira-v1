package com.hunzz.moirav1.domain.relation.model

import jakarta.persistence.Embeddable
import java.util.*

@Embeddable
data class RelationId(
    val userId: UUID,
    val targetId: UUID
)