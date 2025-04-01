package com.hunzz.common.model

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
class PostTransaction(
    @Id
    @Column(name = "tx_id")
    val txId: UUID,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: PostTransactionStatus,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    fun updateStatus(status: PostTransactionStatus) {
        this.status = status
        this.updatedAt = LocalDateTime.now()
    }
}