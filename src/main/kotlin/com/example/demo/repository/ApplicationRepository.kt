package com.example.demo.repository

import com.example.demo.dto.VacancyDTO
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Repository
import org.springframework.web.reactive.function.client.WebClient
import org.json.JSONObject

@Repository
class ApplicationRepository(private val webClient: WebClient.Builder, private val repository: VacancyRepository, private val hhOAuthRepository: HHOAuthRepository) {

    @Value("\${refreshtoken.url}")
    private val refreshTokenUrl: String = ""

    @Value("\${content.type}")
    private val contentType: String = ""

    @Value("\${refresh.token}")
    private val refreshToken: String = ""

    @Value("\${client.id}")
    private val clientId: String = ""

    @Value("\${client.secret}")
    private val clientSecret: String = ""

    fun applyToVacancy(vacancyId: String, resumeId: String): String? {
        val apiUrl = "https://api.hh.ru/negotiations?vacancy_id=$vacancyId&resume_id=$resumeId"

        val accessToken = hhOAuthRepository.findByUserId(1L)?.access_token ?: return null
        return webClient.build()
            .post()
            .uri(apiUrl)
            .header("Authorization", "Bearer $accessToken")
            .header("Content-Type", contentType)
            .retrieve()
            .bodyToMono(String::class.java)
            .block()
    }

    fun getVacancies(): List<VacancyDTO> {
        val accessToken = hhOAuthRepository.findByUserId(1L)?.access_token ?: return emptyList()
        return repository.getVacancies(accessToken, contentType)
    }

    fun refreshAccessToken() {
        var accessToken = ""
        webClient.build()
            .post()
            .uri(refreshTokenUrl)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .bodyValue("grant_type=refresh_token&refresh_token=$refreshToken&client_id=$clientId&client_secret=$clientSecret")
            .retrieve()
            .bodyToMono(String::class.java)
            .map { response ->
                val jsonResponse = JSONObject(response)
                accessToken = jsonResponse.getString("access_token")
            }
            .block()
    }
}
