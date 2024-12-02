package com.example.demo.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.http.HttpStatus


@JsonIgnoreProperties(ignoreUnknown = true)
data class Request(
    @JsonProperty("authorizationHeader")
    var authorizationHeader: String = "",

    @JsonProperty("code")
    var code: String? = null
){
    fun readJson(json: String):Request {
        val objectMapper = ObjectMapper()
        try {
            val tempResponse = objectMapper.readValue(json, Request::class.java)
            val token: String = tempResponse.authorizationHeader.split(" ")[1]
            this.authorizationHeader = token
            this.code = tempResponse.code
        } catch (e: Exception) {
            throw RuntimeException("Failed to parse JSON: ${e.message}")
        }
        return this
    }
}