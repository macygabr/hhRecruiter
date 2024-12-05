package com.example.demo.controller

import com.example.demo.models.AuthenticationServerResponse
import com.example.demo.models.HttpException
import com.example.demo.repository.HHOAuthRepository
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.http.HttpStatus
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service

@Service
class Updater(private val hhoAuthRepository: HHOAuthRepository) {

    @KafkaListener(topics = ["auth_response"], containerFactory = "kafkaListenerApiGateWayService")
    fun updateToken(message: ConsumerRecord<String, String>) {
        try {
            val response = AuthenticationServerResponse().readJson(message.value())
            val data = hhoAuthRepository.findByUserId(response.userId)?:throw HttpException(HttpStatus.UNAUTHORIZED, "token invalid")
            data.token = response.token
            println("Updating hhoauth: ${data.id} ${data.token}")
            hhoAuthRepository.save(data)
        } catch (e:Exception){
            println(e.message)
        }
    }
}
