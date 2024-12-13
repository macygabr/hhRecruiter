package com.example.demo.repository

import com.example.demo.models.user.Filter
import com.example.demo.models.user.Vacancy
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Repository
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.util.concurrent.ConcurrentHashMap
import java.time.Duration
import kotlin.math.ceil

@Repository
class VacancyRepository(private val webClient: WebClient.Builder) {

    private val applyVacanciesList = HashSet<String>()
    private val per_page = 100

    @Value("\${content.type}")
    private val contentType: String = ""

    fun getUnappliedVacancies(accessToken:String, filter: Filter): ConcurrentHashMap<String, Vacancy> {
        val apiUrl = "https://api.hh.ru/vacancies"
        val urlWithFilters = "$apiUrl?${filter.toQueryString()}"
        val vacanciesList = ConcurrentHashMap<String, Vacancy>()
        getAppliedVacancies(accessToken)

        val found = getSizeResult(urlWithFilters,accessToken)
        val totalPages = ceil(found.toDouble() / per_page).toInt()

        System.err.println("Start search UnappliedVacancies. found: $found, totalPages: $totalPages")

        for (page in 0 ..totalPages){
            if(vacanciesList.size >= 200) break
            val url = "$urlWithFilters&page=$page&per_page=$per_page"
            val response = createResponse(url, accessToken)
            response?.get("items")?.let { items ->
                (items as List<Map<*, *>>).forEach { item ->
                    val vacancy = Vacancy().readItem(item)
                    if(correctVacancy(vacancy)) {
                        vacanciesList[vacancy.id] = vacancy
                    }
                }
            }
            println("Search vacancies by $page: ${vacanciesList.size}")
        }
        return vacanciesList
    }

    private fun correctVacancy(vacancy: Vacancy): Boolean {
        if(applyVacanciesList.contains(vacancy.id)) return false
        if(vacancy.has_test) return false
        if(vacancy.response_letter_required) return false
        if(vacancy.relations.isNotEmpty()) return false
        if(!vacancy.type.equals("open")) return false
        if(vacancy.schedule.id != "remote") return false
        return true
    }

    private fun getAppliedVacancies(accessToken:String){
        val baseUrl = "https://api.hh.ru/negotiations"
        val found = getSizeResult(baseUrl,accessToken)
        val totalPages = ceil(found.toDouble() / per_page).toInt()

        System.err.println("Start search ApplyVacancies. found: $found, totalPages: $totalPages")

        for (page in 0 ..totalPages) {
            val url = "$baseUrl?page=$page&per_page=$per_page"
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

    private fun getSizeResult(url:String, accessToken:String): Int{
        val responseParam = createResponse(url,accessToken)
        var found = 0
        responseParam?.get("found")?.let { items -> found = items as Int }
        return found
    }

}



