package com.example.demo.controller

import com.example.demo.entity.AuthenticationServerResponse
import com.example.demo.repository.HHOAuthRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service

@Service
class Updater(private val hhoAuthRepository: HHOAuthRepository) {

    @KafkaListener(topics = ["auth_response"], containerFactory = "kafkaListenerApiGateWayService")
    fun updateToken(message: ConsumerRecord<String, String>) {
        try {
            System.err.println("updateToken key: ${message.key()} value: ${message.value()}")
            val response = AuthenticationServerResponse().readJson(message.value())
            val data = hhoAuthRepository.findByUserId(response.userId)
            data.token = response.token
            System.err.println("Updating hhoauth: ${data.id} ${data.token}")
            hhoAuthRepository.save(data)
        } catch (e:Exception){
            println(e.message)
        }
    }
}
