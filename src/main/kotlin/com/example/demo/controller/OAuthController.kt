package com.example.demo.controller

import com.example.demo.repository.HHOAuthRepository
import com.example.demo.service.OAuthService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.http.MediaType
import org.json.JSONObject
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.reactive.function.BodyInserters

@RestController
class OAuthController(
    private val oauthService: OAuthService
) {

    @GetMapping("/oauht_url")
    fun getOAuthURL(): String {
        return oauthService.getURL()
    }

    @GetMapping("/callback")
    fun callback(@RequestParam("code") code: String): ResponseEntity<String> {
        return try {
            oauthService.callback(code)
            ResponseEntity("", HttpStatus.OK)
        } catch (e:Exception){
            ResponseEntity("${e.message}", HttpStatus.BAD_REQUEST)
        }
    }
}