package com.example.demo.controller

import com.example.demo.service.OAuthService
import com.example.demo.service.ResumeService
import com.example.demo.service.VacancyService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/monitoring")
class MonitoringController(
    private val vacancyService: VacancyService,
    private val oauthService: OAuthService,
    private val resumeService: ResumeService,
) {
    @GetMapping("/start")
    fun startMonitoring(@RequestHeader("Authorization") token: String): ResponseEntity<String> {
        if(!oauthService.checkAccessToken(token)){
            return ResponseEntity(oauthService.getURL(), HttpStatus.ACCEPTED)
        }

        val resume = resumeService.searchResume(token)
        vacancyService.startMonitoringVacancies(token)
        oauthService.refreshAccessToken(token)

        return ResponseEntity.ok(resume.title)
    }

    @GetMapping("/stop")
    fun stopMonitoring(@RequestHeader("Authorization") token: String): ResponseEntity<String> {
        vacancyService.stopMonitoringVacancies(token)
        oauthService.stopRefreshAccessToken(token)
        return ResponseEntity("", HttpStatus.OK)
    }

    @GetMapping("/status")
    fun statusMonitoring(@RequestHeader("Authorization") token: String): ResponseEntity<Boolean> {
        val res = vacancyService.getStatus(token)
        return ResponseEntity.ok(res)
    }
}