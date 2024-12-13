package com.example.demo.controller

import com.example.demo.models.requests.Request
import com.example.demo.models.requests.RequestWithCode
import com.example.demo.models.requests.RequestWithFilter
import com.example.demo.models.response.Response
import com.example.demo.service.FilterService
import com.example.demo.service.MonitoringService
import com.example.demo.service.OAuthService
import com.example.demo.service.kafka.KafkaProducerService
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.http.HttpStatus
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.RestController

@RestController
class MonitoringController(
    private val oauthService: OAuthService,
    private val kafkaProducer: KafkaProducerService,
    private val monitoringService: MonitoringService
) {

    @KafkaListener(topics = ["start"], containerFactory = "kafkaListenerAuthService")
    fun startMonitoring(message: ConsumerRecord<String, String>) {
        val response = Response()
        try {
            val request = Request().readJson(message.value())
            monitoringService.startMonitoringVacancies(request.userId)
            oauthService.refreshAccessToken(request.userId)

            response.status = HttpStatus.OK
            response.message = "OK"
        } catch (e:Exception){
            System.err.println(e.message)
            response.status = HttpStatus.BAD_REQUEST
            response.message = e.message.toString()
        } finally {
            kafkaProducer.sendMessage("response", message.key(), response.toString())
        }
    }

    @KafkaListener(topics = ["stop"], containerFactory = "kafkaListenerAuthService")
    fun stopMonitoring(message: ConsumerRecord<String, String>) {
        val response = Response()
        val request = Request()
        try {
//            recruiter.stopMonitoringVacancies(request)
//            oauthService.stopRefreshAccessToken(request)
        } catch (e:Exception){
            System.err.println(e.message)
            response.status = HttpStatus.BAD_REQUEST
            response.message = e.message.toString()
        } finally {
            kafkaProducer.sendMessage("response", message.key(), response.toString())
        }
    }

    @KafkaListener(topics = ["status"], containerFactory = "kafkaListenerAuthService")
    fun statusMonitoring(message: ConsumerRecord<String, String>) {

    }
}
