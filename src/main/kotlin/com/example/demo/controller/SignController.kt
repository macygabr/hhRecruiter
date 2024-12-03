package com.example.demo.controller

import com.example.demo.entity.SignRequest
import com.example.demo.service.SignService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/sign") // только сервис аутентификации
class SignController(
        private val signService: SignService
) {
    @GetMapping("/in")
    fun signin(@RequestHeader("Authorization") token: String, @RequestHeader("User-ID")  userId: Long):ResponseEntity<String>{
        signService.signIn(token, userId)
        return ResponseEntity("", HttpStatus.OK)
    }

    @GetMapping("/out")
    fun signout(@RequestBody request: SignRequest):ResponseEntity<String>{
        signService.signOut(request)
        return ResponseEntity("", HttpStatus.OK)
    }
}

