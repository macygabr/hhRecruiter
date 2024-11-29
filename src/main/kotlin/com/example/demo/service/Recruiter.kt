package com.example.demo.service

import com.example.demo.entity.AuthenticationServerResponse
import com.example.demo.repository.HHOAuthRepository
import com.example.demo.repository.VacancyRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

@Service
class Recruiter(private val webClient: WebClient.Builder,  private val repository: VacancyRepository, private val hhOAuthRepository: HHOAuthRepository)  {
    private val scheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(1)
    private var scheduledTask: ScheduledFuture<*>? = null

    @Value("\${content.type}")
    private val contentType: String = ""

    val totalVacancies = 200

    @Value("\${resume.id}")
    private val resumeId: String = ""
    fun startMonitoringVacancies(response: AuthenticationServerResponse) {
        println("Запуск мониторинга вакансий...")
        scheduledTask = scheduler.scheduleAtFixedRate(
            { monitorVacancies(response) },
            0,
            1,
            TimeUnit.DAYS
        )
    }

    fun stopMonitoringVacancies() {
        scheduledTask?.cancel(true)
        scheduledTask = null
        println("Мониторинг вакансий остановлен")
    }

    private fun monitorVacancies(data: AuthenticationServerResponse) {
        val accessToken = hhOAuthRepository.findByToken(data.token).access_token.let {""}
        val vacancies = repository.getVacancies(accessToken, contentType)
        println("Найдено вакансий: ${vacancies.size}")
        if (vacancies.isEmpty()) return

        vacancies.stream().forEach { vacancy ->
            val vacancyId = vacancy.id
            try {
                println("Пробую...: ${vacancy.url}")
                Thread.sleep(24*60*60*1000L/totalVacancies)
                val response = applyToVacancy(data, vacancyId, resumeId)
                println("Отклик на вакансию: ${vacancy.url}, Ответ: $response")
            } catch (e: Exception) {
                println("Ошибка при отклике на вакансию: ${vacancy.url}, Ошибка: ${e.message}")
            }
        }
    }

    fun applyToVacancy(data: AuthenticationServerResponse, vacancyId: String, resumeId: String): String? {
        val apiUrl = "https://api.hh.ru/negotiations?vacancy_id=$vacancyId&resume_id=$resumeId"

        val accessToken = hhOAuthRepository.findByToken(data.token).access_token
        return webClient.build()
            .post()
            .uri(apiUrl)
            .header("Authorization", "Bearer $accessToken")
            .header("Content-Type", contentType)
            .retrieve()
            .bodyToMono(String::class.java)
            .block()
    }
}