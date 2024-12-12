package com.example.demo.controller

import com.example.demo.models.requests.RequestWithCode
import com.example.demo.models.response.Response
import com.example.demo.service.FilterService
import com.example.demo.service.OAuthService
import com.example.demo.service.ResumeService
import com.example.demo.service.UserService
import com.example.demo.service.kafka.KafkaProducerService
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.http.HttpStatus
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.web.bind.annotation.RestController


@RestController
class OAuthController(
    private val kafkaProducer: KafkaProducerService,
    private val oauthService: OAuthService,
    private val resumeService: ResumeService,
    private val filterService: FilterService
) {

    @KafkaListener(topics = ["getLink"], containerFactory = "kafkaListenerAuthService")
    fun getOAuthURL(message: ConsumerRecord<String, String>) {
        val response = Response()
        try {
            response.message = oauthService.getURL()
            response.status = HttpStatus.OK
        } catch (e:Exception){
            System.err.println(e.message)
            response.status = HttpStatus.BAD_REQUEST
            response.message = e.message.toString()
        } finally {
            kafkaProducer.sendMessage("response", message.key(), response.toString())
        }
    }

    @KafkaListener(topics = ["registry"], containerFactory = "kafkaListenerAuthService")
    fun registry(message: ConsumerRecord<String, String>) {
        val response = Response()
        try {
            val request = RequestWithCode()
            request.readJson(message.value())

            oauthService.registry(request.userId, request.code)
            resumeService.setDefault(request.userId)
            filterService.setDefaultFilter(request.userId)
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