package com.example.demo.controller

import com.example.demo.entity.HHOAuth
import com.example.demo.repository.HHOAuthRepository
import com.example.demo.service.OAuthService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
class TokenController(
    private val hhOAuthRepository: HHOAuthRepository
) {

    @PutMapping("/register")
    fun register(
        @RequestHeader("Authorization") token: String,
        @RequestHeader("User-ID") userId: Long
    ): ResponseEntity<String>  {
        return try {
            val hhOAuth = HHOAuth()
            val extractedToken = token.split("Bearer ").last().trim()
            hhOAuth.userId=userId
            hhOAuth.token=extractedToken
            hhOAuthRepository.save(hhOAuth)
            ResponseEntity("", HttpStatus.OK)
        } catch (e:Exception){
            ResponseEntity("${e.message}", HttpStatus.BAD_REQUEST)
        }
    }
}