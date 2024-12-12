package com.example.demo.service

import com.example.demo.models.exceptions.AuthenticationException
import com.example.demo.models.exceptions.NotFoundException
import com.example.demo.models.user.Filter
import com.example.demo.models.user.User
import com.example.demo.repository.HhAuthInfoRepository
import com.example.demo.repository.UserRepository
import com.example.demo.repository.VacancyRepository
import org.json.JSONObject
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
    private val userRepository: UserRepository,
    private val webClient: WebClient.Builder,
    private val repository: VacancyRepository
) {
    @Value("\${content.type}")
    private val contentType: String = ""
    private val scheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(1)
    private var scheduledTask: ScheduledFuture<*>? = null

    fun startMonitoringVacancies(userId: Long) {
        val user = userRepository.findById(userId).orElseThrow {
            throw NotFoundException("User not found")
        }!!
        if (user.hhAuthInfo == null) throw AuthenticationException("User not authenticated")
        if(user.filter == null) throw AuthenticationException("User filter not set")
        println("Запуск мониторинга вакансий... ")
        scheduledTask = scheduler.scheduleAtFixedRate(
            { monitorVacancies(user) },
            0,
            1,
            TimeUnit.DAYS
        )
    }

    private fun monitorVacancies(user: User) {
        val accessToken = user.hhAuthInfo?.access_token!!

        val vacancies = repository.getUnappliedVacancies(accessToken, user.filter!!)
        println("Найдено вакансий: ${vacancies.size}")
        if (vacancies.isEmpty()) return

        var i = 1
        vacancies.forEach { (id, vacancy) ->
            try {
                Thread.sleep(1000L)
                val response = applyToVacancy(id, accessToken, user.resume!!.id)
                println("Отклик на ${i++} вакансию: ${vacancy.url}.")
            } catch (e: Exception) {
                System.err.println("Ошибка при отклике на ${i++} вакансию: ${vacancy.url}, Ошибка: ${e.message}")
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