package com.example.demo.repository

import com.example.demo.dto.VacancyDTO
import com.example.demo.entity.HHOAuth
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Repository
import org.springframework.web.reactive.function.client.WebClient

@Repository
class VacancyRepository(private val webClient: WebClient.Builder) {

    @Value("\${vacancy.query}")
    private val query: String = ""

    @Value("\${vacancy.level}")
    private val experienceLevel: String = ""

    @Value("\${vacancy.level}")
    private val contentType: String = ""

    private var responsesList=mutableListOf<VacancyDTO>()

    fun getVacancies(hhOAuth: HHOAuth): List<VacancyDTO> {

        println("Search response...")
        getResponses(hhOAuth)
        println("Search vacancy...")

        val vacanciesList = mutableListOf<VacancyDTO>()

        for (i in 0 until 200) {
            val response = webClient.build()
                .get()
                .uri(getFilteredVacancies(i))
                .header("Authorization", "Bearer ${hhOAuth.access_token}")
                .header("Content-Type", contentType)
                .retrieve()
                .bodyToMono(Map::class.java)
                .block()

            response?.get("items")?.let { items ->
                (items as List<Map<*, *>>).forEach { item ->
                    if(item["response_letter_required"] == false && item["has_test"] == false && containInResponsesList(item["id"])){
                        val id = item["id"] as? String ?: throw IllegalArgumentException("Title cannot be null")
                        val url = item["alternate_url"] as? String ?: throw IllegalArgumentException("URL cannot be null")
                        vacanciesList.add(VacancyDTO(id, url))
                    }
                }
            }
        }
        println("Result count vacancies: ${vacanciesList.size}")
        return vacanciesList
    }

    fun getResponses(hhOAuth: HHOAuth):List<VacancyDTO>{
        for (i in 0 until 200) {
            val response = webClient.build()
                    .get()
                    .uri(getURLPageResponses(i, hhOAuth))
                    .header("Authorization", "Bearer ${hhOAuth.access_token}")
                    .header("Content-Type", contentType)
                    .retrieve()
                    .bodyToMono(Map::class.java)
                    .block()


            response?.get("items")?.let { items ->
                (items as List<Map<*, *>>).forEach { item ->
                    val id = item["id"] as? String ?: throw IllegalArgumentException("Title cannot be null")
                    val vacancy = item["vacancy"] as? Map<*, *>
                    val alternateUrl = vacancy?.get("alternate_url") as? String
                            ?: throw IllegalArgumentException("Alternate URL for vacancy cannot be null")
                    responsesList.add(VacancyDTO(id, alternateUrl))
                }
            }
        }

        println("Result count response: ${responsesList.size}")
        return responsesList
    }

    fun containInResponsesList(vacancyId: Any?):Boolean{
        if (vacancyId == null) return true
        return !(responsesList.any { it.id == vacancyId.toString() })
    }
    fun getFilteredVacancies(
        page: Int = 0
    ): String {
        val apiUrl = "https://api.hh.ru/vacancies"
        return "$apiUrl?order_by=publication_time&page=$page&per_page=10" +
                "&text=$query&experience=$experienceLevel"
    }

    fun getURLPageResponses(
            page: Int = 0,
            hhOAuth: HHOAuth
    ): String {
        val apiUrl = "https://api.hh.ru/negotiations"
        return "$apiUrl?resume_id=${hhOAuth.resumeId}&page=$page&per_page=10"
    }
}