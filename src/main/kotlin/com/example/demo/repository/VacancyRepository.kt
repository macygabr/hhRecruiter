package com.example.demo.repository

import com.example.demo.controller.FilterController
import com.example.demo.models.user.Filter
import com.example.demo.models.vacancy.Vacancy
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Repository
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.util.concurrent.ConcurrentHashMap
import java.time.Duration
import kotlin.math.ceil
import kotlin.math.log

@Repository
class VacancyRepository(private val webClient: WebClient.Builder) {

    private val applyVacanciesList = HashSet<String>()
    private val per_page = 100
    private val logger: Logger = LoggerFactory.getLogger(VacancyRepository::class.java)

    @Value("\${content.type}")
    private val contentType: String = ""

    fun getUnusedVacancies(accessToken:String, filter: Filter): ConcurrentHashMap<String, Vacancy> {
        val apiUrl = "https://api.hh.ru/vacancies"
        val urlWithFilters = "$apiUrl?${filter.toQueryString()}"
        val vacanciesList = ConcurrentHashMap<String, Vacancy>()
        getAppliedVacancies(accessToken)

        val found = getSizeResult(urlWithFilters,accessToken)
        val totalPages = ceil(found.toDouble() / per_page).toInt()

        logger.info("Поиск вакансий удовлетворяющих фильтрам... Всех вакансий: $found, всего страниц: $totalPages")

        for (page in 0 ..totalPages) {
            if(vacanciesList.size >= 200) break
            val url = "$urlWithFilters&page=$page&per_page=$per_page"
            print("\r$page страниц из $totalPages")
            val response = createResponse(url, accessToken)
            response?.get("items")?.let { items ->
                (items as List<Map<*, *>>).forEach { item ->
                    val vacancy = Vacancy().readItem(item)
                    if(correctVacancy(vacancy)) {
                        vacanciesList[vacancy.id] = vacancy
                    }
                }
            }
            logger.info("Найдено вакансий: ${vacanciesList.size} на странице $page")
        }
        return vacanciesList
    }

    private fun correctVacancy(vacancy: Vacancy): Boolean {
        if(applyVacanciesList.contains(vacancy.id)) {
            logger.debug("Вакансия ${vacancy.url} уже применена")
            return false
        }
        if(vacancy.has_test) {
            logger.debug("Вакансия ${vacancy.url} имеет тест")
            return false
        }
        if(vacancy.response_letter_required) {
            logger.debug("Вакансия ${vacancy.url} требует ответа")
            return false
        }
        if(vacancy.relations.isNotEmpty()) {
            logger.debug("Вакансия ${vacancy.url} имеет связанные вакансии")
            return false
        }
        if(!vacancy.type.equals("open")) {
            logger.debug("Вакансия ${vacancy.url} не открыта")
            return false
        }
        return true
    }

    fun getAppliedVacancies(accessToken:String){
        val baseUrl = "https://api.hh.ru/negotiations"
        val found = getSizeResult(baseUrl,accessToken)
        val totalPages = ceil(found.toDouble() / per_page).toInt()

        logger.info("Поиск уже примененных вакансий - всего вакансий: $found, всего страниц: $totalPages")

        for (page in 0 ..totalPages) {
            val url = "$baseUrl?page=$page&per_page=$per_page"
            print("\r$page страниц из $totalPages")
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
        logger.info("Найдено примененных вакансий: ${applyVacanciesList.size}")
    }

    private fun createResponse(url: String, accessToken: String): Map<*, *>? {
        val response = webClient.build()
            .get()
            .uri(url)
            .header("Authorization", "Bearer $accessToken")
            .header("Content-Type", contentType)
            .retrieve()
            .onStatus({ status -> status.isError }) { response ->
                logger.error("Ошибка при выполнении запроса: ${response.statusCode()}.")
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



