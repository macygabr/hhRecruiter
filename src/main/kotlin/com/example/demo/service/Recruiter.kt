package com.example.demo.service


import com.example.demo.entity.*
import com.example.demo.repository.HHOAuthRepository
import com.example.demo.repository.VacancyRepository
import org.json.JSONObject
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.awt.SystemColor
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

@Service
class Recruiter(private val webClient: WebClient.Builder,  private val repository: VacancyRepository, private val hhOAuthRepository: HHOAuthRepository, private val resumeService: ResumeService) {
    private val scheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(1)
    private var scheduledTask: ScheduledFuture<*>? = null

    @Value("\${content.type}")
    private val contentType: String = ""

    private var accessToken: String = ""

    private var resume: Resume = Resume()

    private var hhOAuth: HHOAuth = HHOAuth()


    fun startMonitoringVacancies(request: Request) {

        hhOAuth = hhOAuthRepository.findByToken( request.authorizationHeader) ?: throw HttpException(HttpStatus.UNAUTHORIZED, "invalid token")
        accessToken = hhOAuth.access_token ?: throw HttpException(HttpStatus.FORBIDDEN, "login hh.ru")

        resume = resumeService.getResume(request)

        println("Запуск мониторинга вакансий... $request")
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
        val vacancies = repository.getVacancies(accessToken)
        println("Найдено вакансий: ${vacancies.size}")
        if (vacancies.isEmpty()) return

        var i =1
        vacancies.forEach { (id, vacancy) ->
            try {
//                Thread.sleep(24*60*60*1000L/totalVacancies)
                Thread.sleep(1000L)
                val response = applyToVacancy(id)
                println("Отклик на ${i++} вакансию: ${vacancy.url}. Ответ: $response")
            } catch (e: Exception) {
                System.err.println("Ошибка при отклике на ${i++} вакансию: ${vacancy.url}, Ошибка: ${e.message}")
            }
        }
    }


    private fun applyToVacancy(vacancyId: String): String? {
        return webClient.build()
            .post()
            .uri("https://api.hh.ru/negotiations?vacancy_id=$vacancyId&resume_id=${resume.id}")
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