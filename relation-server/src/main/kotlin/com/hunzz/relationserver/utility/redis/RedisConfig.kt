package com.hunzz.relationserver.utility.redis

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
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

        lettuceConnectionFactory.start()

        return lettuceConnectionFactory
    }

    @Bean
    fun redisTemplate() = RedisTemplate<String, Any>().apply {
        connectionFactory = redisConnectionFactory()

        // key serializer
        keySerializer = StringRedisSerializer()
        hashKeySerializer = StringRedisSerializer()

        // exclude 'class info' from data
        val objectMapper = ObjectMapper().apply {
            setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
        }

        // value serializer
        valueSerializer = GenericJackson2JsonRedisSerializer(objectMapper)
        hashValueSerializer = GenericJackson2JsonRedisSerializer(objectMapper)
    }
}