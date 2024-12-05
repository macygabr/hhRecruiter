package com.example.demo.repository

import com.example.demo.models.Resume
import org.springframework.stereotype.Repository
import org.springframework.web.reactive.function.client.WebClient

@Repository
class ResumeRepository(
        private val webClient: WebClient.Builder
) {
    private val resumesList = HashMap<String, Resume>()

    fun getResumes(accessToken: String): HashMap<String, Resume> {
        val response = webClient.build()
                .get()
                .uri("https://api.hh.ru/resumes/mine")
                .header("Authorization", "Bearer $accessToken")
                .header("Content-Type", "application/json")
                .retrieve()
                .bodyToMono(Map::class.java)
                .block()


        response?.get("items")?.let { items ->
            (items as List<Map<*, *>>).forEach { item ->
                val id = item["id"] as? String ?: throw IllegalArgumentException("Resume ID cannot be null")
                val title = item["title"] as? String ?: throw IllegalArgumentException("Resume ID cannot be null")
                resumesList[id] = Resume(id, title)
            }
        }

        return resumesList
    }
}