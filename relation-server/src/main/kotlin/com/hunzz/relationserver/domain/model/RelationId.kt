package com.hunzz.relationserver.domain.model

import jakarta.persistence.Embeddable
import java.util.*

@Embeddable
data class RelationId(
    val userId: UUID,
    val targetId: UUID
) {
    constructor() : this(UUID.randomUUID(), UUID.randomUUID())
}