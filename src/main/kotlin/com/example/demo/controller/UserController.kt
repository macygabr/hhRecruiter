package com.example.demo.controller

import com.example.demo.models.requests.Request
import com.example.demo.models.response.Response
import com.example.demo.service.UserService
import com.example.demo.service.kafka.KafkaProducerService
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.http.HttpStatus
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController(
    private val userService: UserService,
    private val kafkaProducer: KafkaProducerService
) {


    @KafkaListener(topics = ["profile"], containerFactory = "kafkaListenerAuthService")
    fun profile(message: ConsumerRecord<String, String>) {
        val response = Response()

        try {
            val request = Request()
            request.readJson(message.value())
            System.err.println(request.toString())
            response.message = userService.profile(request.userId).toString()
            response.status = HttpStatus.OK
        } catch (e:Exception){
            System.err.println(e.message)
            response.status = HttpStatus.BAD_REQUEST
            response.message = e.message.toString()
        } finally {
            kafkaProducer.sendMessage("response", message.key(), response.toString())
        }
    }
}