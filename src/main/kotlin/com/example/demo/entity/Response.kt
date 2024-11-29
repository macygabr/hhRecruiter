package com.example.demo.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.HttpStatus

@JsonIgnoreProperties(ignoreUnknown = true)
data class Response(
    @JsonProperty("status")
    var status: HttpStatus = HttpStatus.BAD_REQUEST,

    @JsonProperty("message")
    var message: String = ""
) {

    fun toJson(): String {
        return try {
            ObjectMapper().writeValueAsString(this)
        } catch (e: JsonProcessingException) {
            throw RuntimeException("Error during JSON serialization: " + e.message)
        }
    }
}
