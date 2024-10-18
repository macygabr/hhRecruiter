package com.example.demo.controller

import com.example.demo.entity.SignRequest
import com.example.demo.service.OAuthService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
class OAuthController(
        private val oauthService: OAuthService
) {

    fun checkOAuth(token:String):Boolean {
        return oauthService.checkAccessToken(token)
    }
    fun getOAuthURL(): String {
        return oauthService.getURL()
    }

    @GetMapping("/callback")
    fun callback(@RequestParam("code") code: String, @RequestHeader("Authorization") token: String): ResponseEntity<String> {
        return try {
            oauthService.callback(code, token)
            ResponseEntity("", HttpStatus.OK)
        } catch (e:Exception) {
            ResponseEntity("${e.message}", HttpStatus.BAD_REQUEST)
        }
    }
}