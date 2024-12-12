package com.example.demo.models.requests

import com.example.demo.models.user.Experience
import com.example.demo.models.user.Filter
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper


@JsonIgnoreProperties(ignoreUnknown = true)
class RequestWithFilter: Request()  {
    @JsonProperty("text")
    var text: String? = null

    @JsonProperty("level")
    var level: Experience? = null

    @JsonProperty("area")
    var area: Int?=null

    val filter: Filter = Filter()

    override fun readJson(json: String): Request {
        val objectMapper = ObjectMapper()
        try {
            val tempResponse = objectMapper.readValue(json, RequestWithFilter::class.java)
            filter.text = tempResponse.text
            filter.experienceLevel = tempResponse.level
            filter.area = tempResponse.area
        } catch (e: Exception) {
            throw RuntimeException("Failed to parse JSON: ${e.message}")
        }
        return this
    }
}