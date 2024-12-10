package com.example.demo.models.requests

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper


class RequestWithFilter: Request()  {
    @JsonProperty("text")
    var text: String? = null

    @JsonProperty("level")
    var level: String? = null

    @JsonProperty("area")
    var area: String?=null

    @JsonProperty("filter")
    var filter: String? = null

    override fun readJson(json: String): Request {
        val objectMapper = ObjectMapper()
        try {
            val tempResponse = objectMapper.readValue(json, RequestWithFilter::class.java)
            val token: String = tempResponse.authorizationHeader.split(" ")[1]
            this.authorizationHeader = token

            val requestJson = tempResponse.filter?.let { objectMapper.readTree(it) }
            this.text = requestJson?.get("text")?.asText()
            this.level = requestJson?.get("level")?.asText()
            this.area = requestJson?.get("area")?.asText()

        } catch (e: Exception) {
            throw RuntimeException("Failed to parse JSON: ${e.message}")
        }
        return this
    }
}