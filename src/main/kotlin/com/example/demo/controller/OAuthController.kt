package com.example.demo.controller


import com.example.demo.models.requests.Request
import com.example.demo.models.requests.RequestWithCode
import com.example.demo.models.Response
import com.example.demo.service.OAuthService
import com.example.demo.service.kafka.KafkaProducerService
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.web.bind.annotation.RestController
import org.springframework.http.HttpStatus
import org.springframework.kafka.annotation.KafkaListener

@RestController
class OAuthController(
    private val kafkaProducer: KafkaProducerService,
    private val oauthService: OAuthService
) {

    @KafkaListener(topics = ["getLink"], containerFactory = "kafkaListenerAuthService")
    fun getOAuthURL(message: ConsumerRecord<String, String>) {
        val response = Response()
        val request = Request()

        try {
            response.message = oauthService.getURL()
            response.status = HttpStatus.OK
        } catch (e:Exception) {

        } finally {
            System.err.println( response.toJson())
            kafkaProducer.sendMessage("response", message.key(), response.toJson())
        }
    }

    @KafkaListener(topics = ["registry"], containerFactory = "kafkaListenerAuthService")
    fun registry(message: ConsumerRecord<String, String>) {
        val response = Response()
        val request = RequestWithCode()

        try {
            request.readJson(message.value())
            oauthService.callback(request)
            response.status = HttpStatus.OK
        } catch (e:Exception) {
            System.err.println(e)
            response.status = HttpStatus.BAD_REQUEST
            response.message = e.message.toString()
        } finally {
            System.err.println(response.toJson())
            kafkaProducer.sendMessage("response", message.key(), response.toJson())
        }

    }
}