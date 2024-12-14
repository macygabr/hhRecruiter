package com.example.demo.controller

import com.example.demo.models.requests.RequestWithCode
import com.example.demo.models.response.Response
import com.example.demo.service.FilterService
import com.example.demo.service.OAuthService
import com.example.demo.service.ResumeService
import com.example.demo.service.kafka.KafkaProducerService
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Controller

@Controller
class OAuthController(
    private val kafkaProducer: KafkaProducerService,
    private val oauthService: OAuthService,
    private val resumeService: ResumeService,
    private val filterService: FilterService
) {
    private val logger: Logger = LoggerFactory.getLogger(OAuthController::class.java)

    @KafkaListener(topics = ["getLink"], containerFactory = "kafkaListenerAuthService")
    fun getOAuthURL(message: ConsumerRecord<String, String>) {
        val response = Response()
        logger.debug("Получен запрос на получение ссылки для авторизации: ${message.value()}")
        try {
            response.message = oauthService.getURL()
            response.status = HttpStatus.OK
        } catch (e:Exception){
            logger.error(e.message)
            response.status = HttpStatus.BAD_REQUEST
            response.message = e.message.toString()
        } finally {
            kafkaProducer.sendMessage("response", message.key(), response.toString())
            logger.debug("Отправлен ответ после запроса ссылки: {}", response)
        }
    }

    @KafkaListener(topics = ["registry"], containerFactory = "kafkaListenerAuthService")
    fun registry(message: ConsumerRecord<String, String>) {
        val response = Response()
        logger.debug("Получен запрос на регистрацию: ${message.value()}")
        try {
            val request = RequestWithCode()
            request.readJson(message.value())

            oauthService.registry(request.userId, request.code)
            val resume = resumeService.setDefault(request.userId)
            filterService.setDefaultFilter(request.userId)
            response.status = HttpStatus.OK
            response.message = resume.toString()
        } catch (e:Exception){
            logger.error(e.message)
            response.status = HttpStatus.BAD_REQUEST
            response.message = e.message.toString()
        } finally {
            kafkaProducer.sendMessage("response", message.key(), response.toString())
            logger.debug("Отправлен ответ после регистрации: {}", response)
        }
    }
}