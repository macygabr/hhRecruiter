package com.example.demo.controller

import com.example.demo.entity.AuthenticationServerResponse
import com.example.demo.entity.Request
import com.example.demo.entity.Response
import com.example.demo.service.OAuthService
import com.example.demo.service.Recruiter
import com.example.demo.service.kafka.KafkaProducerService
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.http.HttpStatus
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service

@Service
class MonitoringController(
    private val recruiter: Recruiter,
    private val oauthService: OAuthService,
    private val kafkaProducer: KafkaProducerService
) {
    @KafkaListener(topics = ["start"], containerFactory = "kafkaListenerAuthService")
    fun startMonitoring(message: ConsumerRecord<String, String>) {
        val response = AuthenticationServerResponse()

        try {
             System.err.println("start key: ${message.key()} value: ${message.value()}")
             response.readJson(message.value())

             if(oauthService.userAuthInHH(response)) {
                 recruiter.startMonitoringVacancies(response)
                 oauthService.refreshAccessToken()
                 response.status = HttpStatus.OK
             } else {
                 response.message = oauthService.getURL()
                 response.status = HttpStatus.UNAUTHORIZED
             }
        } catch (e:Exception) {
                response.message = e.message.toString()
                response.status = HttpStatus.BAD_REQUEST
        } finally {
            System.err.println( response.toJson())
             kafkaProducer.sendMessage("response", message.key(), response.toJson())
        }
    }

    @KafkaListener(topics = ["stop"], containerFactory = "kafkaListenerAuthService")
    fun stopMonitoring(message: ConsumerRecord<String, String>) {
         try {
            recruiter.stopMonitoringVacancies()
            oauthService.stopRefreshAccessToken()
        } catch (e:Exception){

        } finally {

        }
    }

    @KafkaListener(topics = ["status"], containerFactory = "kafkaListenerAuthService")
    fun statusMonitoring(message: ConsumerRecord<String, String>) {
        System.err.println("status key: ${message.key()} value: ${message.value()}")
        val response = Response()
        val request = Request()
        try {

            val hhoauth = oauthService.status()

            if(hhoauth.access_token == null || hhoauth.token == null){
                response.status = HttpStatus.FORBIDDEN
                response.message = "login hh.ru"
            } else {
                response.status = HttpStatus.OK
                response.message = hhoauth.toString()
            }

        } catch (e:Exception){
            System.err.println(e.message)
            response.status =HttpStatus.BAD_REQUEST
            response.message = e.message.toString()
        } finally {
            kafkaProducer.sendMessage("response", message.key(), response.toJson())
        }
    }
}
