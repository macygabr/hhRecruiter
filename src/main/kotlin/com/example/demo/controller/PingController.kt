package com.example.demo.controller

import com.example.demo.models.response.Response
import com.example.demo.service.kafka.KafkaProducerService
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.http.HttpStatus
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Controller
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Controller
class PingController (
    private val kafkaProducer: KafkaProducerService,
){
    private val logger: Logger = LoggerFactory.getLogger(PingController::class.java)

    @KafkaListener(topics = ["ping"], containerFactory = "kafkaListenerAuthService")
    fun ping(message: ConsumerRecord<String, String>) {
        val response = Response()
        try {
            logger.debug("Получен запрос на проверку соединения: ${message.value()}")
            response.message = "pong"
            response.status = HttpStatus.OK
        } catch (e:Exception){
            logger.error(e.message)
            response.status = HttpStatus.BAD_REQUEST
            response.message = e.message.toString()
        } finally {
            kafkaProducer.sendMessage("response", message.key(), response.toString())
        }
    }
}