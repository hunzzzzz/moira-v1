package com.hunzz.common.kafka

import org.apache.kafka.clients.producer.ProducerConfig.*
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory

@Configuration
@EnableKafka
class KafkaProducerConfig(
    @Value("\${spring.kafka.bootstrap-servers}")
    val serverUrl: String
) {
    @Bean
    fun producerFactory(): ProducerFactory<String, String> {
        val configs = mapOf(
            BOOTSTRAP_SERVERS_CONFIG to serverUrl,
            KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            VALUE_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java
        )
        return DefaultKafkaProducerFactory(configs)
    }

    @Bean
    fun template() = KafkaTemplate(producerFactory())
}