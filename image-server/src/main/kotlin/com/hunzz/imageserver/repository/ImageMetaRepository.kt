package com.hunzz.imageserver.repository

import com.hunzz.imageserver.model.ImageMeta
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ImageMetaRepository : JpaRepository<ImageMeta, Long> {
    fun findAllByTxId(txId: UUID): List<ImageMeta>
    fun deleteByTxId(txId: UUID): Int
}