package com.example.demo.models.response

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.HttpStatus

data class Response(
    @JsonProperty("status")
    var status: HttpStatus = HttpStatus.BAD_REQUEST,

    @JsonProperty("message")
    var message: String = ""
) {

    override fun toString(): String {
        return try {
            ObjectMapper().writeValueAsString(this)
        } catch (e: JsonProcessingException) {
            throw RuntimeException("Error during JSON serialization: " + e.message)
        }
    }
}
