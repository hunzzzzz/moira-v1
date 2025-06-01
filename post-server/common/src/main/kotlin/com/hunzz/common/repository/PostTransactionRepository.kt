package com.hunzz.common.repository

import com.hunzz.common.model.PostTransaction
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface PostTransactionRepository : JpaRepository<PostTransaction, UUID>