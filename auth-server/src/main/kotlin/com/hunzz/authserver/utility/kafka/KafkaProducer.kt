package com.hunzz.authserver.utility.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class KafkaProducer(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val objectMapper: ObjectMapper
) {
    private val logger = LoggerFactory.getLogger(KafkaProducer::class.java)

    fun send(topic: String, data: Any) {
        val str = objectMapper.writeValueAsString(data)

        kafkaTemplate.send(topic, str)
        logger.info("kafka 메시지를 전송하였습니다. ($topic)")
    }
}