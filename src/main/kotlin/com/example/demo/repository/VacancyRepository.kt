package com.example.demo.repository


import com.example.demo.entity.Vacancy
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


    private val applyVacanciesList = mutableListOf<String>()

    @Value("\${content.type}")
    private val contentType: String = ""


    fun getVacancies(accessToken:String): List<Vacancy> {
        val apiUrl = "https://api.hh.ru/vacancies"
        val vacanciesList = mutableListOf<Vacancy>()
        var iterations= 0
        System.err.println("Start search ApplyVacancies...")
        findApplyVacancies(accessToken)
        System.err.println("Start search vacancies...")
        while (vacanciesList.size < 200 && iterations < 20) {
            val response = webClient.build()
                .get()
                .uri(apiUrl)
                .header("Authorization", "Bearer $accessToken")
                .header("Content-Type", contentType)
                .retrieve()
                .bodyToMono(Map::class.java)
                .block()

            response?.get("items")?.let { items ->
                (items as List<Map<*, *>>).forEach { item ->
                    val vacancy = readItem(item)
                    if(correctVacancy(vacancy)) {
                        vacanciesList.add(vacancy)
                    } else {
                        println("уже откликнулся на ${vacancy.url}")
                    }
                }
            }
            iterations++
        }
        return vacanciesList
    }

    private fun readItem(item: Map<*, *>) : Vacancy{
        val id = item["id"] as? String ?: throw IllegalArgumentException("Id cannot be null")
        val title = item["title"] as? String ?: "title is null"
        val salary = item["salary"] as? String ?: "salary is null"
        val responseLetterRequired = item["response_letter_required"] as? Boolean ?: false
        val description = item["description"] as? String ?: "description is null"
        val url = item["alternate_url"] as? String ?: throw IllegalArgumentException("URL cannot be null")
        return Vacancy(id,title,salary,responseLetterRequired, description,url)
    }

    private fun correctVacancy(vacancy: Vacancy): Boolean {
        println(vacancy)
        if(vacancy.responseLetterRequired) return false
        if(applyVacanciesList.contains(vacancy.id)) return false
        return true
    }

    fun findApplyVacancies(accessToken:String){
//        "found": 2254,
//        "pages": 113,
//        "page": 0,
//        "per_page": 20
        val apiUrl = "https://api.hh.ru/negotiations"



        val response = webClient.build()
            .get()
            .uri(apiUrl)
            .header("Authorization", "Bearer $accessToken")
            .header("Content-Type", contentType)
            .retrieve()
            .bodyToMono(Map::class.java)
            .block()

        response?.get("items")?.let { items ->
            (items as List<Map<*, *>>).forEach { item ->
                val vacancy = readItem(item)
                    applyVacanciesList.add(vacancy.id)
            }
        }
    }
}