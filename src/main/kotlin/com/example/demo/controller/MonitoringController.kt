package com.example.demo.controller

import com.example.demo.service.OAuthService
import com.example.demo.service.VacancyService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class MonitoringController(
    private val vacancyService: VacancyService,
    private val oauthService: OAuthService
) {
    @GetMapping("/start_monitoring")
    fun startMonitoring(): ResponseEntity<String> {
        return try {
            vacancyService.startMonitoringVacancies()
            oauthService.refreshAccessToken()
            ResponseEntity("", HttpStatus.OK)
        } catch (e:Exception){
            ResponseEntity("${e.message}", HttpStatus.BAD_REQUEST)
        }
    }

    @GetMapping("/stop_monitoring")
    fun stopMonitoring(): ResponseEntity<String> {
        return try {
            vacancyService.stopMonitoringVacancies()
            oauthService.stopRefreshAccessToken()
            ResponseEntity("", HttpStatus.OK)
        } catch (e:Exception){
            ResponseEntity("${e.message}", HttpStatus.BAD_REQUEST)
        }
    }
}