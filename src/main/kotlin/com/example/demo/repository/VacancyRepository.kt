package com.example.demo.repository

import com.example.demo.dto.VacancyDTO
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Repository
import org.springframework.web.reactive.function.client.WebClient

@Repository
class VacancyRepository(private val webClient: WebClient.Builder) {

    @Value("\${vacancy.query}")
    private val query: String = ""

    @Value("\${vacancy.level}")
    private val experienceLevel: String = ""


    fun getVacancies(accessToken:String, contentType:String): List<VacancyDTO> {
        val apiUrl = "https://api.hh.ru/vacancies"
        val totalVacancies = 200
        val vacanciesPerPage = 20
        val totalPages = totalVacancies / vacanciesPerPage

        val vacanciesList = mutableListOf<VacancyDTO>()

        for (page in 0 until totalPages) {
            val response = webClient.build()
                .get()
                .uri("$apiUrl?text=$query&experience=$experienceLevel")
                .header("Authorization", "Bearer $accessToken")
                .header("Content-Type", contentType)
                .retrieve()
                .bodyToMono(Map::class.java)
                .block()


            response?.get("items")?.let { items ->
                (items as List<Map<*, *>>).forEach { item ->
                    val id = item["id"] as? String ?: throw IllegalArgumentException("Title cannot be null")
                    val url = item["alternate_url"] as? String ?: throw IllegalArgumentException("URL cannot be null")
                    vacanciesList.add(VacancyDTO(id, url))
                }
            }
        }

        return vacanciesList
    }
}