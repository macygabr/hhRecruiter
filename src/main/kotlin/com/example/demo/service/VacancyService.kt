package com.example.demo.service


import com.example.demo.repository.HHOAuthRepository
import com.example.demo.repository.VacancyRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import java.util.concurrent.*

@Service
class VacancyService(private val webClient: WebClient.Builder, private val repository: VacancyRepository, private val hhOAuthRepository: HHOAuthRepository) {
    @Value("\${content.type}")
    private val contentType: String = ""

    val totalVacancies = 200

    @Value("\${resume.id}")
    private val resumeId: String = ""

    private val scheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(1)
    private var scheduledTask: ScheduledFuture<*>? = null

    fun startMonitoringVacancies() {
        println("Запуск мониторинга вакансий...")
        scheduledTask = scheduler.scheduleAtFixedRate(
            { monitorVacancies() },
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

    private fun monitorVacancies() {
        val accessToken = hhOAuthRepository.findByToken("1").access_token
        val vacancies = repository.getVacancies(accessToken, contentType)
        println("Найдено вакансий: ${vacancies.size}")
        if (vacancies.isEmpty()) return

        vacancies.stream().forEach { vacancy ->
            val vacancyId = vacancy.id
            try {
                println("Пробую...: ${vacancy.url}")
                Thread.sleep(24*60*60*1000L/totalVacancies)
                val response = applyToVacancy(vacancyId, resumeId)
                println("Отклик на вакансию: ${vacancy.url}, Ответ: $response")
            } catch (e: Exception) {
                println("Ошибка при отклике на вакансию: ${vacancy.url}, Ошибка: ${e.message}")
            }
        }
    }

    fun applyToVacancy(vacancyId: String, resumeId: String): String? {
        val apiUrl = "https://api.hh.ru/negotiations?vacancy_id=$vacancyId&resume_id=$resumeId"

        val accessToken = hhOAuthRepository.findByToken("1").access_token
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