package com.example.demo.controller

import com.example.demo.dto.VacancyDTO
import com.example.demo.entity.Vacancy
import com.example.demo.service.VacancyService
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HomeController(private val vacancyService: VacancyService) {

    @Value("\${client.id}")
    private val clientId: String = ""

    @Value("\${redirect.url}")
    private val redirectUrl: String = ""

    @GetMapping("/home")
    fun home(): String {
        return "https://hh.ru/oauth/authorize?response_type=code&client_id=${clientId}&redirect_uri=${redirectUrl}"
    }

    @GetMapping("/start_monitoring")
    fun vacancy(): String {
        vacancyService.startMonitoringVacancies()
        return "start_monitoring ..."
    }
}