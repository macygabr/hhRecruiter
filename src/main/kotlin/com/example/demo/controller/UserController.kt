package com.example.demo.controller

import com.example.demo.models.requests.Request
import com.example.demo.models.response.Response
import com.example.demo.service.UserService
import com.example.demo.service.kafka.KafkaProducerService
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Controller

@Controller
class UserController(
    private val userService: UserService,
    private val kafkaProducer: KafkaProducerService
) {
    private val logger: Logger = LoggerFactory.getLogger(UserController::class.java)

    @KafkaListener(topics = ["profile"], containerFactory = "kafkaListenerAuthService")
    fun profile(message: ConsumerRecord<String, String>) {
        val response = Response()
        logger.debug("Получен запрос на получение профиля: ${message.value()}")
        try {
            val request = Request()
            request.readJson(message.value())
            response.message = userService.profile(request.userId).toString()
            response.status = HttpStatus.OK
        } catch (e:Exception){
            logger.error(e.message)
            response.status = HttpStatus.BAD_REQUEST
            response.message = e.message.toString()
        } finally {
            kafkaProducer.sendMessage("response", message.key(), response.toString())
            logger.debug("Отправлен ответ после запроса профиля: {}", response)
        }
    }
}