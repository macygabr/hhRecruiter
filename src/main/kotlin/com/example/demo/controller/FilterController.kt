package com.example.demo.controller

import com.example.demo.models.requests.RequestWithFilter
import com.example.demo.models.response.Response
import com.example.demo.service.FilterService
import com.example.demo.service.kafka.KafkaProducerService
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.http.HttpStatus
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.web.bind.annotation.RestController

@RestController
class FilterController(
    private val filterService: FilterService,
    private val kafkaProducer: KafkaProducerService
) {

    private val objectMapper = ObjectMapper()

    @KafkaListener(topics = ["filter"], containerFactory = "kafkaListenerAuthService")
    fun setFilter(message: ConsumerRecord<String, String>) {
        val response = Response()
        System.err.println(message.value())
        try {
            val jsonNode: JsonNode = objectMapper.readTree(message.value())
            val userId = jsonNode["userId"]?.asLong() ?: throw IllegalArgumentException("Поле 'userId' отсутствует в сообщении")

            val request = RequestWithFilter()
            request.readJson(message.value())
            filterService.setFilter(userId, request.filter)
            response.status = HttpStatus.OK
            response.message = "filter " + request.filter
        } catch (e:Exception){
            System.err.println(e.message)
            response.status = HttpStatus.BAD_REQUEST
            response.message = e.message.toString()
        } finally {
            kafkaProducer.sendMessage("response", message.key(), response.toString())
        }
    }
}