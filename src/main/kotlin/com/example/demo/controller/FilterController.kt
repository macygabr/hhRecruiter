package com.example.demo.controller

import com.example.demo.models.requests.RequestWithFilter
import com.example.demo.models.response.Response
import com.example.demo.service.FilterService
import com.example.demo.service.kafka.KafkaProducerService
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Controller

@Controller
class FilterController(
    private val filterService: FilterService,
    private val kafkaProducer: KafkaProducerService
) {

    private val objectMapper = ObjectMapper()
    private val logger: Logger = LoggerFactory.getLogger(FilterController::class.java)

    @KafkaListener(topics = ["filter"], containerFactory = "kafkaListenerAuthService")
    fun setFilter(message: ConsumerRecord<String, String>) {
        val response = Response()
        try {
            logger.debug("Получен запрос на установку фильтра: ${message.value()}")
            val jsonNode: JsonNode = objectMapper.readTree(message.value())
            val userId = jsonNode["userId"]?.asLong() ?: throw IllegalArgumentException("Поле 'userId' отсутствует в сообщении")

            val request = RequestWithFilter()
            request.readJson(message.value())
            filterService.setFilter(userId, request.filter)
            response.status = HttpStatus.OK
            response.message = "filter " + request.filter
        } catch (e:Exception){
            logger.error(e.message)
            response.status = HttpStatus.BAD_REQUEST
            response.message = e.message.toString()
        } finally {
            kafkaProducer.sendMessage("response", message.key(), response.toString())
            logger.debug("Отправлен ответ: $response")
        }
    }
}