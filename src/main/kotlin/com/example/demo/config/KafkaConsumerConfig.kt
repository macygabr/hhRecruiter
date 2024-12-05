package com.example.demo.config

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory

@EnableKafka
@Configuration
class KafkaConsumerConfig {

    @Value("\${kafka.bootstrap-servers}")
    private val bootstrapServers: String=""

    @Bean
    fun consumerFactoryApi(): ConsumerFactory<String, String> {
        val configProps: MutableMap<String, Any> = HashMap()
        configProps[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] =bootstrapServers
        configProps[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
        configProps[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
        configProps[ConsumerConfig.GROUP_ID_CONFIG] = "hh"
        return DefaultKafkaConsumerFactory(configProps)
    }

    @Bean
    fun kafkaListenerApiGateWayService(): ConcurrentKafkaListenerContainerFactory<String, String> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, String>()
        factory.consumerFactory = consumerFactoryApi()
        return factory
    }

    @Bean
    fun consumerFactoryAuth(): ConsumerFactory<String, String> {
        val configProps: MutableMap<String, Any> = HashMap()
        configProps[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
        configProps[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
        configProps[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
        configProps[ConsumerConfig.GROUP_ID_CONFIG] = "auth_service"
        return DefaultKafkaConsumerFactory(configProps)
    }

    @Bean
    fun kafkaListenerAuthService(): ConcurrentKafkaListenerContainerFactory<String, String> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, String>()
        factory.consumerFactory = consumerFactoryAuth()
        return factory
    }
}
