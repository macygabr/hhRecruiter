package com.example.demo.controller

import com.example.demo.entity.AuthenticationServerResponse
import com.example.demo.entity.Request
import com.example.demo.entity.Response
import com.example.demo.service.OAuthService
import com.example.demo.service.Recruiter
import com.example.demo.service.kafka.KafkaProducerService
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.dao.EmptyResultDataAccessException
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
        val response = Response()
        val request = Request()

        try {
            request.readJson(message.value())
             if(oauthService.userAuthInHH(request)) {
                 response.status = HttpStatus.OK
                 kafkaProducer.sendMessage("response", message.key(), response.toJson())
                 recruiter.startMonitoringVacancies(request)
//                 oauthService.refreshAccessToken(request)
             } else {
                 response.message = "login hh.ru"
                 response.status = HttpStatus.UNAUTHORIZED
             }
        } catch (e:Exception) {
            response.message = e.message.toString()
            response.status = HttpStatus.BAD_REQUEST
            System.err.println(response.toJson())
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

        val response = Response()
        val request = Request()

        try {
            request.readJson(message.value())
            System.err.println(request)
            val hhoauth = oauthService.status(request)

            if(hhoauth == null){
                response.status = HttpStatus.UNAUTHORIZED
                response.message = "token invalid"
            } else if(oauthService.userAuthInHH(request)){
                response.status = HttpStatus.OK
                response.message = hhoauth.toString()
            } else {
                response.status = HttpStatus.FORBIDDEN
                response.message = "login hh.ru"
            }

        } catch (e: EmptyResultDataAccessException) {
            System.err.println(e.message)
            response.status = HttpStatus.UNAUTHORIZED
            response.message = "token invalid"
        } catch (e:Exception){
            System.err.println(e.message)
            response.status =HttpStatus.BAD_REQUEST
            response.message = e.message.toString()
        } finally {
            kafkaProducer.sendMessage("response", message.key(), response.toJson())
        }
    }
}
