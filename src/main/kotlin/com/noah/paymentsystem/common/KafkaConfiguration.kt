package com.noah.paymentsystem.common

import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.io.IOException


@Configuration
class KafkaConfiguration {
    @Value("\${spring.kafka.bootstrap-servers}")
    private val bootstrapServers: String? = null

    @Bean
    fun producerFactory(): ProducerFactory<String, String> {
        val configs: MutableMap<String, Any?> = HashMap()
        configs[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
        configs[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        configs[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        return DefaultKafkaProducerFactory(configs)
    }

    @Bean
    fun kafkaTemplate(): KafkaTemplate<String?, String?> {
        return KafkaTemplate(producerFactory())
    }
}

@Service
class KafkaProducer(
    private val kafkaTemplate: KafkaTemplate<String, String>
) {
    fun sendMessage(message: String) {
        println("Produced message: $message")
        kafkaTemplate.send(TOPIC, message)
    }

    companion object {
        private const val TOPIC = "exam-topic"
    }
}

@Service
class KafkaConsumer {
    @KafkaListener(topics = ["exam-topic"], groupId = "foo")
    @Throws(IOException::class)
    fun consume(message: String) {
        println("Consumed message : $message")
    }
}

@RestController
@RequestMapping(value = ["/kafka"])
class KafkaController(private val producer: KafkaProducer) {
    @PostMapping
    fun sendMessage(@RequestParam message: String): String {
        println("message : $message")
        producer.sendMessage(message)

        return "success"
    }
}
