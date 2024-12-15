package com.example.demo.service

import com.example.demo.controller.FilterController
import com.example.demo.models.exceptions.AuthenticationException
import org.json.JSONObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

@Service
class MonitoringService (
    private val userService: UserService,
    private val webClient: WebClient.Builder,
    private val repository: VacancyService
) {
    @Value("\${content.type}")
    private val contentType: String = ""
    private val scheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(1)
    private var scheduledTask: ScheduledFuture<*>? = null
    private val logger: Logger = LoggerFactory.getLogger(FilterController::class.java)

    fun stopMonitoringVacancies(userId: Long) {
        val user = userService.profile(userId)
        scheduledTask?.cancel(true)
        scheduledTask = null
        user.monitoring = false
        userService.save(user)
        logger.info("Мониторинг вакансий остановлен")
    }

    fun startMonitoringVacancies(userId: Long) {
        val user = userService.profile(userId)
        if (user.hhAuthInfo == null) throw AuthenticationException("User not authenticated")
        if(user.filter == null) throw AuthenticationException("User filter not set")

        user.monitoring = true
        userService.save(user)
        logger.info("Мониторинг вакансий запущен")

        scheduledTask = scheduler.scheduleAtFixedRate(
            { monitorVacancies(userId) },
            0,
            1,
            TimeUnit.DAYS
        )
    }

    private fun monitorVacancies(userId: Long) {
        val user = userService.profile(userId)

        val accessToken = user.hhAuthInfo?.access_token!!
        val vacancies = repository.getUnusedVacancies(accessToken, user.filter!!)
        logger.info("Всего найдено ${vacancies.size} вакансий")
        if (vacancies.isEmpty()) return

        var i = 1
        vacancies.forEach { (id, vacancy) ->
            try {
                if(scheduledTask == null) return
                Thread.sleep(1000L)
                applyToVacancy(id, accessToken, user.resume!!.id)
                logger.info("Отклик на ${i++} вакансию: ${vacancy.url}")
            } catch (e: Exception) {
                logger.error("Ошибка при отклике на ${i++} вакансию: ${vacancy.url}, Ошибка: ${e.message}")
            }
        }
    }

    private fun applyToVacancy(vacancyId: String, accessToken: String, resumeId: String): String? {
        return webClient.build()
            .post()
            .uri("https://api.hh.ru/negotiations?vacancy_id=$vacancyId&resume_id=$resumeId")
            .header("Authorization", "Bearer $accessToken")
            .header("Content-Type", contentType)
            .retrieve()
            .onStatus({ it.isError }) { response ->
                response.bodyToMono(String::class.java).flatMap { errorBody ->
                    val jsonObject = JSONObject(errorBody)
                    val description = jsonObject.optString("description", "Неизвестная ошибка")
                    Mono.error(RuntimeException(description))
                }
            }.bodyToMono(String::class.java).block()
    }
}