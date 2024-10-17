package com.example.demo.repository

import com.example.demo.dto.VacancyDTO
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Repository
import org.springframework.web.reactive.function.client.WebClient
import java.net.URLEncoder

@Repository
class VacancyRepository(private val webClient: WebClient.Builder) {

    @Value("\${vacancy.query}")
    private val query: String = ""

    @Value("\${vacancy.level}")
    private val experienceLevel: String = ""


    fun getVacancies(accessToken:String, contentType:String): List<VacancyDTO> {
        println("Поиск вакансий...")

        val vacanciesList = mutableListOf<VacancyDTO>()

        for (i in 0 until 200) {
            val response = webClient.build()
                .get()
                .uri(getFilteredVacancies(i))
                .header("Authorization", "Bearer $accessToken")
                .header("Content-Type", contentType)
                .retrieve()
                .bodyToMono(Map::class.java)
                .block()

            response?.get("items")?.let { items ->
                (items as List<Map<*, *>>).forEach { item ->
                    if(item["response_letter_required"] == false && item["has_test"] == false){
                        val id = item["id"] as? String ?: throw IllegalArgumentException("Title cannot be null")
                        val url = item["alternate_url"] as? String ?: throw IllegalArgumentException("URL cannot be null")
                        vacanciesList.add(VacancyDTO(id, url))
                    }
                }
            }
        }
        println("Найдено вакансий: ${vacanciesList.size}")
        return vacanciesList
    }

    fun getFilteredVacancies(
        page: Int = 0
    ): String {
        val apiUrl = "https://api.hh.ru/vacancies"
        return "$apiUrl?order_by=publication_time&page=$page&per_page=10" +
                "&text=$query&experience=$experienceLevel"
    }
}