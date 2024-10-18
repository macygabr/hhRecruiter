package com.example.demo.service


import com.example.demo.dto.VacancyDTO
import com.example.demo.entity.HHOAuth
import com.example.demo.repository.HHOAuthRepository
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
class VacancyService(
        private val webClient: WebClient.Builder,
        private val repository: VacancyRepository,
        private val hhOAuthRepository: HHOAuthRepository
) {

    @Value("\${content.type}")
    private val contentType: String = ""


    private val scheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(1)
    private var scheduledTask: ScheduledFuture<*>? = null

    fun startMonitoringVacancies(token:String) {
        println("Запуск мониторинга вакансий...")
        scheduledTask = scheduler.scheduleAtFixedRate(
            {
                monitoringVacancies()
            },
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


    private fun monitoringVacancies() {
        try {
            val hhOAuth = hhOAuthRepository.findByToken("1")
            val vacancies = repository.getVacancies(hhOAuth)

            for ((id, url) in vacancies) {
                try {
                    print("Try...: $url\t")
                    applyToVacancy(id, hhOAuth)
                    println("Successful")
                } catch (e: Exception) {
                    println("Error: ${e.message}")
                    if(e.message == "Daily negotiations limit is exceeded") {
                        return
                    }
                }
            }
        } catch (e:Exception){
            println("-----Error: ${e.message}")
        }
    }

    fun applyToVacancy(vacancyId: String, hhOAuth:HHOAuth): String? {
        val apiUrl = "https://api.hh.ru/negotiations?vacancy_id=$vacancyId&resume_id=${hhOAuth.resumeId}"
        return webClient.build()
                .post()
                .uri(apiUrl)
                .header("Authorization", "Bearer ${hhOAuth.access_token}")
                .header("Content-Type", contentType)
                .retrieve()
                .onStatus({ it.isError }) { response ->
                    response.bodyToMono(String::class.java).flatMap { errorBody ->
                        val jsonObject = JSONObject(errorBody)
                        val description = jsonObject.optString("description", "Неизвестная ошибка")
                        Mono.error(RuntimeException(description))
                    }
                }
                .bodyToMono(String::class.java)
                .block()
    }
}