package com.example.demo.models.requests

import com.example.demo.models.user.Experience
import com.example.demo.models.user.Filter
import com.example.demo.models.user.Schedule
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper


@JsonIgnoreProperties(ignoreUnknown = true)
class RequestWithFilter: Request()  {
    @JsonProperty("text")
    var text: String? = null

    @JsonProperty("experience")
    var level: String? = null

    @JsonProperty("area")
    var area: Int?=null

    @JsonProperty("schedule")
    var schedule: String?=null

    val filter: Filter = Filter()

    override fun readJson(json: String): Request {
        val objectMapper = ObjectMapper()
        try {
            val tempResponse = objectMapper.readValue(json, RequestWithFilter::class.java)
            filter.text = tempResponse.text
            filter.area = tempResponse.area
            tempResponse.level?.let { level ->
                try {
                    filter.experience = Experience.valueOf(level)
                } catch (e: IllegalArgumentException) {
                    throw RuntimeException("Invalid experience level: $level")
                }
            }

            tempResponse.schedule?.let { schedule ->
                try {
                    filter.schedule = Schedule.valueOf(schedule)
                } catch (e: IllegalArgumentException) {
                    throw RuntimeException("Invalid schedule: $schedule")
                }
            }
        } catch (e: Exception) {
            throw RuntimeException("Failed to parse JSON: ${e.message}")
        }
        return this
    }
}