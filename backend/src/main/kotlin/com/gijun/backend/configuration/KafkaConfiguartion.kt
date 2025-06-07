package com.gijun.backend.configuration

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.*
import org.springframework.kafka.listener.ContainerProperties
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.kafka.support.serializer.JsonSerializer

@Configuration
@EnableKafka
class KafkaConfiguration {

    @Value("\${spring.kafka.bootstrap-servers:localhost:9092}")
    private lateinit var bootstrapServers: String

    @Value("\${spring.kafka.consumer.group-id:webpos-group}")
    private lateinit var groupId: String

    @Value("\${spring.kafka.consumer.key-deserializer:org.apache.kafka.common.serialization.StringDeserializer}")
    private lateinit var keyDeserializer: String

    @Value("\${spring.kafka.consumer.value-deserializer:org.springframework.kafka.support.serializer.JsonDeserializer}")
    private lateinit var valueDeserializer: String

    @Value("\${spring.kafka.producer.key-serializer:org.apache.kafka.common.serialization.StringSerializer}")
    private lateinit var keySerializer: String

    @Value("\${spring.kafka.producer.value-serializer:org.springframework.kafka.support.serializer.JsonSerializer}")
    private lateinit var valueSerializer: String

    @Value("\${spring.kafka.consumer.properties.spring.json.trusted.packages:com.gijun.backend}")
    private lateinit var trustedPackages: String

    @Bean
    fun consumerFactory(): ConsumerFactory<String, Any> {
        val configProps = mutableMapOf<String, Any>()
        configProps[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
        configProps[ConsumerConfig.GROUP_ID_CONFIG] = groupId
        configProps[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = keyDeserializer
        configProps[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = valueDeserializer
        configProps[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"
        configProps[ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG] = false
        configProps[ConsumerConfig.MAX_POLL_RECORDS_CONFIG] = 500
        configProps[ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG] = 30000
        configProps[ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG] = 10000

        // JsonDeserializer 설정
        configProps[JsonDeserializer.TRUSTED_PACKAGES] = trustedPackages
        configProps[JsonDeserializer.USE_TYPE_INFO_HEADERS] = false
        configProps[JsonDeserializer.VALUE_DEFAULT_TYPE] = "com.gijun.backend.adapter.in.messaging.events.BaseEvent"

        return DefaultKafkaConsumerFactory(configProps)
    }

    @Bean
    fun kafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, Any> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, Any>()
        factory.consumerFactory = consumerFactory()
        factory.setConcurrency(3) // 동시 처리할 컨슈머 수
        factory.containerProperties.ackMode = ContainerProperties.AckMode.MANUAL_IMMEDIATE
        factory.containerProperties.isSyncCommits = true
        factory.setCommonErrorHandler(kafkaErrorHandler())
        return factory
    }

    @Bean
    fun producerFactory(): ProducerFactory<String, Any> {
        val configProps = mutableMapOf<String, Any>()
        configProps[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
        configProps[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = keySerializer
        configProps[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = valueSerializer
        configProps[ProducerConfig.ACKS_CONFIG] = "all" // 모든 복제본 확인
        configProps[ProducerConfig.RETRIES_CONFIG] = 3
        configProps[ProducerConfig.BATCH_SIZE_CONFIG] = 16384
        configProps[ProducerConfig.LINGER_MS_CONFIG] = 5
        configProps[ProducerConfig.BUFFER_MEMORY_CONFIG] = 33554432
        configProps[ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG] = true

        // JsonSerializer 설정
        configProps[JsonSerializer.ADD_TYPE_INFO_HEADERS] = false

        return DefaultKafkaProducerFactory(configProps)
    }

    @Bean
    fun kafkaTemplate(): KafkaTemplate<String, Any> {
        val template = KafkaTemplate(producerFactory())
        template.setObservationEnabled(true) // 메트릭 수집 활성화
        return template
    }

    @Bean
    fun kafkaErrorHandler(): org.springframework.kafka.listener.CommonErrorHandler {
        return org.springframework.kafka.listener.DefaultErrorHandler(
            org.springframework.util.backoff.FixedBackOff(1000L, 3)
        )
    }
}