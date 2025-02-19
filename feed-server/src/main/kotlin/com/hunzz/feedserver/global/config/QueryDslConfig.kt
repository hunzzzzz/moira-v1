package com.hunzz.feedserver.global.config

import com.querydsl.jpa.impl.JPAQueryFactory
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class QueryDslConfig {
    @PersistenceContext
    protected lateinit var em: EntityManager

    @Bean
    protected fun jpaQueryFactory() = JPAQueryFactory(em)
}