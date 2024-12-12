package com.example.demo.controller

import com.example.demo.models.requests.RequestWithFilter
import com.example.demo.models.response.Response
import com.example.demo.service.FilterService
import com.example.demo.service.kafka.KafkaProducerService
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.http.HttpStatus
import org.springframework.kafka.annotation.KafkaListener

class FilterController(
    private val filterService: FilterService,
    private val kafkaProducer: KafkaProducerService
) {

    @KafkaListener(topics = ["filter"], containerFactory = "kafkaListenerAuthService")
    fun setFilter(message: ConsumerRecord<String, String>) {
        val response = Response()

        try {
            val request = RequestWithFilter()
            request.readJson(message.value())
            filterService.setFilter(request.userId, request.filter)
            response.status = HttpStatus.OK
            response.message = ""
        } catch (e:Exception){
            System.err.println(e.message)
            response.status = HttpStatus.BAD_REQUEST
            response.message = e.message.toString()
        } finally {
            kafkaProducer.sendMessage("response", message.key(), response.toString())
        }
    }
}