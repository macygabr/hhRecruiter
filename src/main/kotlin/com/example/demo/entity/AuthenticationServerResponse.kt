package com.example.demo.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.HttpStatus
import java.util.*

@JsonIgnoreProperties(ignoreUnknown = true)
class AuthenticationServerResponse() {
    @JsonProperty("user_id")
    var userId: Long = 0L

    @JsonProperty("token")
    var token: String = UUID.randomUUID().toString()

    @JsonProperty("token_name")
    var tokenName: String = "authToken"

    @JsonProperty("status")
    var status: HttpStatus = HttpStatus.BAD_REQUEST

    @JsonProperty("message")
    var message: String = ""

    fun readJson(json: String):AuthenticationServerResponse {
        val objectMapper = ObjectMapper()
        try {
            val tempResponse = objectMapper.readValue(json, TempResponse::class.java)
            this.token = tempResponse.token
        } catch (e: Exception) {
            throw RuntimeException("Failed to parse JSON: ${e.message}")
        }
        return this
    }

    private data class TempResponse(
        @JsonProperty("user_id") val userId: Long = 0L,
        @JsonProperty("token") val token: String = UUID.randomUUID().toString(),
        @JsonProperty("token_name") val tokenName: String = "authToken",
        @JsonProperty("status") val status: HttpStatus = HttpStatus.BAD_REQUEST,
        @JsonProperty("message") val message: String = ""
    )

    fun toJson(): String {
        return try {
            ObjectMapper().writeValueAsString(this)
        } catch (e: JsonProcessingException) {
            throw RuntimeException("Error during JSON serialization: " + e.message)
        }
    }
}
