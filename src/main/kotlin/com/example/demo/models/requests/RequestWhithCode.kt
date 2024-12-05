package com.example.demo.models.requests

import com.example.demo.models.Filter
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper

class RequestWithCode : Request() {

    @JsonProperty("code")
    var code: String? = null

    override fun readJson(json: String): Request {
        val objectMapper = ObjectMapper()
        try {
            val tempResponse = objectMapper.readValue(json, RequestWithCode::class.java)
            val token: String = tempResponse.authorizationHeader.split(" ")[1]
            this.authorizationHeader = token
            this.code = tempResponse.code
        } catch (e: Exception) {
            throw RuntimeException("Failed to parse JSON: ${e.message}")
        }
        return this
    }
}