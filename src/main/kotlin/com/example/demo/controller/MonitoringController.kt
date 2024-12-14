package com.example.demo.controller

import com.example.demo.models.requests.Request
import com.example.demo.models.response.Response
import com.example.demo.service.MonitoringService
import com.example.demo.service.OAuthService
import com.example.demo.service.kafka.KafkaProducerService
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Controller

@Controller
class MonitoringController(
    private val oauthService: OAuthService,
    private val kafkaProducer: KafkaProducerService,
    private val monitoringService: MonitoringService
) {

    private val logger: Logger = LoggerFactory.getLogger(MonitoringController::class.java)

    @KafkaListener(topics = ["start"], containerFactory = "kafkaListenerAuthService")
    fun startMonitoring(message: ConsumerRecord<String, String>) {
        val response = Response()
        logger.debug("Получен запрос на запуск мониторинга: ${message.value()}")
        try {
            val request = Request().readJson(message.value())
            monitoringService.startMonitoringVacancies(request.userId)
            oauthService.refreshAccessToken(request.userId)

            response.status = HttpStatus.OK
            response.message = "OK"
        } catch (e:Exception){
            logger.error(e.message)
            response.status = HttpStatus.BAD_REQUEST
            response.message = e.message.toString()
        } finally {
            kafkaProducer.sendMessage("response", message.key(), response.toString())
            logger.debug("Отправлен ответ после запуска: {}", response)
        }
    }

    @KafkaListener(topics = ["stop"], containerFactory = "kafkaListenerAuthService")
    fun stopMonitoring(message: ConsumerRecord<String, String>) {
        val response = Response()
        logger.info("Получен запрос на остановку мониторинга: ${message.value()}")
        try {
            val request = Request().readJson(message.value())
            monitoringService.stopMonitoringVacancies(request.userId)
            oauthService.stopRefreshAccessToken(request.userId)
        } catch (e:Exception){
            logger.error(e.message)
            response.status = HttpStatus.BAD_REQUEST
            response.message = e.message.toString()
        } finally {
            kafkaProducer.sendMessage("response", message.key(), response.toString())
            logger.debug("Отправлен ответ после остановки мониторинга: {}", response)
        }
    }

    @KafkaListener(topics = ["status"], containerFactory = "kafkaListenerAuthService")
    fun statusMonitoring(message: ConsumerRecord<String, String>) {
        logger.debug("Получен запрос на статус мониторинга: ${message.value()}")
    }
}
