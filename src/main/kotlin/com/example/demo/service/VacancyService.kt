package com.example.demo.service

import com.example.demo.dto.VacancyDTO
import com.example.demo.repository.ApplicationRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

@Service
class VacancyService(private val repository: ApplicationRepository) {

    val totalVacancies = 200

    @Value("\${resume.id}")
    private val resumeId: String = ""

    private var started = false
    private val scheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(1)
    private val executorService: ExecutorService = Executors.newSingleThreadExecutor()

    fun startMonitoringVacancies() {
        if (!started) {
            started = true
            println("Запуск мониторинга вакансий...")
            executorService.submit {
                monitorVacancies()
                scheduler.scheduleAtFixedRate(
                    { monitorVacancies() },
                    1000,
                    1,
                    TimeUnit.DAYS
                )
            }
        } else {
            println("Мониторинг уже запущен.")
        }
    }

    fun monitorVacancies() {
        val vacancies = repository.getVacancies()
        println("Найдено вакансий: ${vacancies.size}")
        if (vacancies.isEmpty()) return

        vacancies.stream().forEach { vacancy ->
            val vacancyId = vacancy.id
            try {
                println("Пробую...: ${vacancy.url}")
                Thread.sleep(24*60*60*1000L/totalVacancies)
                val response = repository.applyToVacancy(vacancyId, resumeId)
                println("Отклик на вакансию: ${vacancy.url}, Ответ: $response")
            } catch (e: Exception) {
                println("Ошибка при отклике на вакансию: $vacancyId, Ошибка: ${e.message}")
            }
        }
    }

    @Scheduled(fixedRate = 1209599)
    fun refreshAccessToken() {
        try {
            repository.refreshAccessToken()
        } catch (e:Exception){
            println(e.message)
        }
    }
}