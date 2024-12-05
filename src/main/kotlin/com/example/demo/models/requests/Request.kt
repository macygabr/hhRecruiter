package com.example.demo.models.requests

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper


@JsonIgnoreProperties(ignoreUnknown = true)
open class Request(
    @JsonProperty("authorizationHeader")
    var authorizationHeader: String = ""

){
    open fun readJson(json: String): Request {
        val objectMapper = ObjectMapper()
        try {
            val tempResponse = objectMapper.readValue(json, Request::class.java)
            val token: String = tempResponse.authorizationHeader.split(" ")[1]
            this.authorizationHeader = token
        } catch (e: Exception) {
            throw RuntimeException("Failed to parse JSON: ${e.message}")
        }
        return this
    }
}