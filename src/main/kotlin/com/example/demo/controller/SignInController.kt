package com.example.demo.controller

import com.example.demo.entity.SignRequest
import com.example.demo.service.SignService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/sign") // только сервис аутентификации
class SignController(
        private val signService: SignService
) {

    @PostMapping("/up")
    fun signup(@RequestBody request: SignRequest):ResponseEntity<String>{
        return try {
            signService.signUp(request)
            ResponseEntity("", HttpStatus.OK)
        } catch (e: Exception) {
            ResponseEntity("${e.message}", HttpStatus.BAD_REQUEST)
        }
    }

    @PostMapping("/in")
    fun signin(@RequestBody request: SignRequest):ResponseEntity<String>{
        return try {
            signService.signIn(request)
            ResponseEntity("", HttpStatus.OK)
        } catch (e: Exception) {
            ResponseEntity("${e.message}", HttpStatus.BAD_REQUEST)
        }
    }

    @PostMapping("/out")
    fun signout(@RequestBody request: SignRequest):ResponseEntity<String>{
        return try {
            signService.signOut(request)
            ResponseEntity("", HttpStatus.OK)
        } catch (e: Exception) {
            ResponseEntity("${e.message}", HttpStatus.BAD_REQUEST)
        }
    }
}

