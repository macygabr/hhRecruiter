package com.example.demo.repository


import com.example.demo.models.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Repository
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

@Repository
class VacancyRepository(private val webClient: WebClient.Builder) {

    private val applyVacanciesList = HashSet<String>()
//    private var accessToken: String = ""

    @Value("\${content.type}")
    private val contentType: String = ""

//    fun getInvitedVacancies():ConcurrentHashMap<String,Vacancy> {
//
//        return null
//    }
    fun getUnappliedVacancies(accessToken:String, filter: Filter): ConcurrentHashMap<String,Vacancy> {
        val apiUrl = "https://api.hh.ru/vacancies"
        val urlWithFilters = "$apiUrl?${filter.toQueryString()}"

        val vacanciesList = ConcurrentHashMap<String, Vacancy>()
        var iterations= 0

        getAppliedVacancies(accessToken)
        System.err.println("Start search vacancies by $urlWithFilters")
        while (vacanciesList.size < 200 && iterations < 20) {
            val response = createResponse(urlWithFilters, accessToken)

            response?.get("items")?.let { items ->
                (items as List<Map<*, *>>).forEach { item ->
                    val vacancy = readItem(item)
                    if(correctVacancy(vacancy)) {
                        vacanciesList[vacancy.id] = vacancy
                    }
                }
            }
            println("Search vacancies by $iterations: ${vacanciesList.size}")
            iterations++
        }
        return vacanciesList
    }

    private fun readItem(item: Map<*, *>) : Vacancy {
        val id = item["id"] as? String ?: throw IllegalArgumentException("Id cannot be null")
        val title = item["title"] as? String ?: "title is null"
        val salary = item["salary"] as? String ?: "salary is null"
        val responseLetterRequired = item["response_letter_required"] as? Boolean ?: false
        val description = item["description"] as? String ?: "description is null"
        val url = item["alternate_url"] as? String ?: throw IllegalArgumentException("URL cannot be null")
        return Vacancy(id,title,salary,responseLetterRequired, description,url)
    }

    private fun correctVacancy(vacancy: Vacancy): Boolean {
        if(vacancy.responseLetterRequired) return false
        if(applyVacanciesList.contains(vacancy.id)) return false
        return true
    }

    private fun getAppliedVacancies(accessToken:String){
        var totalPages = 1
        var perPage = 20
        val baseUrl = "https://api.hh.ru/negotiations"

        val responseParam = createResponse(baseUrl,accessToken)
        System.err.println("Start search ApplyVacancies...")

        responseParam?.get("per_page")?.let { items ->
            perPage = items as Int
        }
        responseParam?.get("pages")?.let { items ->
            totalPages = items as Int
        }

        for (page in 0 ..totalPages) {
            val url = "$baseUrl?page=$page&per_page=$perPage"
            println("page $page from $totalPages")
            val response = createResponse(url,accessToken)
            response?.get("items")?.let { items ->
                (items as List<Map<*, *>>).forEach { item ->
                    val id = item["id"] as? String
                    if (id != null) {
                        applyVacanciesList.add(id)
                    }
                }
            }
        }
        System.err.println("total ApplyVacancies: ${applyVacanciesList.size}")
    }

    private fun createResponse(url: String, accessToken: String): Map<*, *>? {
        val response = webClient.build()
            .get()
            .uri(url)
            .header("Authorization", "Bearer $accessToken")
            .header("Content-Type", contentType)
            .retrieve()
            .onStatus({ status -> status.isError }) { response ->
                System.err.println("Error response: ${response.statusCode()}")
                Mono.error(Exception("Request failed with status ${response.statusCode()}"))
            }
            .bodyToMono(Map::class.java)
            .timeout(Duration.ofSeconds(10))

        return response.block()
    }

}