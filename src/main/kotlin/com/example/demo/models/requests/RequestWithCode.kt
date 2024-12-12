package com.example.demo.models.requests

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import org.json.JSONObject

@JsonIgnoreProperties(ignoreUnknown = true)
class RequestWithCode : Request() {

    @JsonProperty("code")
    var code: String = ""

    override fun readJson(json: String): Request {

        try {
            val jsonObject = JSONObject(json)
            this.userId = jsonObject.getLong("userId")
            this.code = jsonObject.getString("code")
        } catch (e: Exception) {
            throw RuntimeException("Failed to parse JSON: ${e.message}")
        }
        return this
    }
}