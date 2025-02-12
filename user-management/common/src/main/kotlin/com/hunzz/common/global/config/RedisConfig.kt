package com.hunzz.common.global.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.cache.RedisCacheConfiguration.defaultCacheConfig
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.cache.RedisCacheManager.RedisCacheManagerBuilder
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair.fromSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.time.Duration

@Configuration
class RedisConfig(
    @Value("\${spring.data.redis.host}")
    private val host: String,

    @Value("\${spring.data.redis.port}")
    private val port: Int
) {
    fun redisConnectionFactory(): LettuceConnectionFactory {
        val lettuceConnectionFactory = LettuceConnectionFactory(host, port)
        return lettuceConnectionFactory
    }

    @Bean
    fun redisTemplate() = RedisTemplate<String, Any>().apply {
        connectionFactory = redisConnectionFactory()

        keySerializer = StringRedisSerializer()
        valueSerializer = StringRedisSerializer()

        hashKeySerializer = StringRedisSerializer()
        hashValueSerializer = StringRedisSerializer()

        setDefaultSerializer(StringRedisSerializer())
    }

    private fun getDefaultConfiguration() = defaultCacheConfig()
        .serializeKeysWith(fromSerializer(StringRedisSerializer()))
        .serializeValuesWith(fromSerializer(GenericJackson2JsonRedisSerializer()))
        .entryTtl(Duration.ofDays(1))

    @Bean
    @Primary
    fun redisCacheManager(): RedisCacheManager = RedisCacheManagerBuilder
        .fromConnectionFactory(redisConnectionFactory())
        .cacheDefaults(getDefaultConfiguration())
        .build()
}