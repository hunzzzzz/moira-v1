package com.hunzz.moirav1.global.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.StringRedisSerializer

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
}