package com.example.demo.controller

import com.example.demo.service.OAuthService
import com.example.demo.service.ResumeService
import com.example.demo.service.VacancyService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class MonitoringController(
    private val vacancyService: VacancyService,
    private val oauthService: OAuthService,
    private val resumeService: ResumeService,
    private val oauthController: OAuthController,
) {
    @GetMapping("/start_monitoring")
    fun startMonitoring(@RequestHeader("Authorization") token: String): ResponseEntity<String> {
         try {
             if(!oauthController.checkOAuth(token)){
                 return ResponseEntity(oauthController.getOAuthURL(), HttpStatus.UNAUTHORIZED)
             }
             resumeService.searchResume(token)
             vacancyService.startMonitoringVacancies(token)
             oauthService.refreshAccessToken(token)
             return ResponseEntity("", HttpStatus.OK)
        } catch (e:Exception){
             return ResponseEntity("${e.message}", HttpStatus.BAD_REQUEST)
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