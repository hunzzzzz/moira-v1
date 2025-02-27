package com.hunzz.api.domain.repository

import com.hunzz.common.model.Notification
import org.bson.types.ObjectId
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository

@Repository
class NotificationRepositoryImpl(
    private val mongoTemplate: MongoTemplate
) : NotificationCustomRepository {
    override fun getNotifications(
        pageable: Pageable,
        userId: String,
        cursor: String?
    ): List<Notification> {
        val query = Query()
            .addCriteria(Criteria.where("userId").`is`(userId))
            .limit(pageable.pageSize)
            .with(Sort.by(Sort.Direction.DESC, "_id"))

        if (cursor != null) {
            query.addCriteria(Criteria.where("_id").lt(ObjectId(cursor)))
        }

        return mongoTemplate.find(query, Notification::class.java)
    }
}