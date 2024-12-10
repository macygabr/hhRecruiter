package com.example.demo.controller

import com.example.demo.models.Filter
import com.example.demo.models.HttpException
import com.example.demo.models.requests.Request
import com.example.demo.models.Response
import com.example.demo.models.requests.RequestWithFilter
import com.example.demo.service.OAuthService
import com.example.demo.service.Recruiter
import com.example.demo.service.ResumeService
import com.example.demo.service.kafka.KafkaProducerService
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.http.HttpStatus
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service

@Service
class MonitoringController(
    private val recruiter: Recruiter,
    private val oauthService: OAuthService,
    private val kafkaProducer: KafkaProducerService,
    private val resumeService: ResumeService
) {
    @KafkaListener(topics = ["start"], containerFactory = "kafkaListenerAuthService")
    fun startMonitoring(message: ConsumerRecord<String, String>) {
        val response = Response()
        try {
            System.err.println(message.value())

            val filter = Filter()
            filter.readJson(message.value())

            recruiter.updateFilter(filter)
            recruiter.startMonitoringVacancies(filter)
            oauthService.refreshAccessToken(filter)

            response.status = HttpStatus.OK
            response.message = "OK"
        } catch (e:HttpException) {
            System.err.println(e.message)
            response.status = e.status
            response.message = e.message.toString()
        } finally {
            kafkaProducer.sendMessage("response", message.key(), response.toJson())
        }
    }

    @KafkaListener(topics = ["stop"], containerFactory = "kafkaListenerAuthService")
    fun stopMonitoring(message: ConsumerRecord<String, String>) {
        val response = Response()
        val request = Request()
        try {
            recruiter.stopMonitoringVacancies(request)
            oauthService.stopRefreshAccessToken(request)
        } catch (e:HttpException){
            System.err.println(e.message)
            response.status = e.status
            response.message = e.message.toString()
        } finally {
            kafkaProducer.sendMessage("response", message.key(), response.toJson())
        }
    }

    @KafkaListener(topics = ["status"], containerFactory = "kafkaListenerAuthService")
    fun statusMonitoring(message: ConsumerRecord<String, String>) {
        val response = Response()
        val request = Request()

        try {
            request.readJson(message.value())
            val hhoauth = oauthService.status(request)
            oauthService.checkUserAuth(request)
            response.status = HttpStatus.OK
            response.message = hhoauth.toString()
        } catch (e:HttpException){
            System.err.println(e.message)
            response.status = e.status
            response.message = e.message.toString()
        } finally {
            kafkaProducer.sendMessage("response", message.key(), response.toJson())
        }
    }
}
