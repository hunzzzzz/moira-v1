package com.hunzz.userserver.utility

import jakarta.persistence.Entity
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.persistence.Table
import org.springframework.beans.factory.InitializingBean
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class DataCleaner(
    private val redisTemplate: RedisTemplate<String, String>
) : InitializingBean {
    @PersistenceContext
    private lateinit var entityManager: EntityManager
    private lateinit var tables: List<String>

    override fun afterPropertiesSet() {
        tables = entityManager.metamodel.entities
            .filter { entity -> entity.javaType.getAnnotation(Entity::class.java) != null }
            .map { entity -> entity.javaType.getAnnotation(Table::class.java).name }
            .toList()
    }

    @Transactional
    fun execute() {
        entityManager.flush()

        tables.forEach { tableName ->
            entityManager.createNativeQuery("ALTER TABLE $tableName DISABLE CONSTRAINT ALL")
            entityManager.createNativeQuery("TRUNCATE TABLE $tableName").executeUpdate()
            entityManager.createNativeQuery("ALTER TABLE $tableName ENABLE CONSTRAINT ALL")
        }

        redisTemplate.keys("*").forEach { redisTemplate.delete(it) }
    }
}