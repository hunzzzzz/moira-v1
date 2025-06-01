package com.hunzz.imageserver.component

import org.springframework.stereotype.Component
import java.util.*

@Component
class RedisKeyProvider {
    // post-transaction
    fun pending(txId: UUID) = "pending:$txId"
    fun rollback(txId: UUID) = "rollback:$txId"
}