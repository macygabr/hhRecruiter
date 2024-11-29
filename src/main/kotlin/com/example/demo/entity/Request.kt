package com.example.demo.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.HttpStatus


@JsonIgnoreProperties(ignoreUnknown = true)
data class Request(
    @JsonProperty("token")
    var token: String = ""
){
    fun readJson(json: String):Request {
        val objectMapper = ObjectMapper()
        try {
            val tempResponse = objectMapper.readValue(json, Request::class.java)
            this.token = tempResponse.token
        } catch (e: Exception) {
            throw RuntimeException("Failed to parse JSON: ${e.message}")
        }
        return this
    }
}
