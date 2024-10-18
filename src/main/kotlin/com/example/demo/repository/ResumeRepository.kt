package com.example.demo.repository

import com.example.demo.dto.ResumeDTO
import com.example.demo.dto.VacancyDTO
import com.example.demo.entity.HHOAuth
import com.example.demo.entity.Resume
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Repository
import org.springframework.web.reactive.function.client.WebClient

@Repository
class ResumeRepository(
        private val webClient: WebClient.Builder
) {
    private val resumesList = mutableListOf<ResumeDTO>()

    fun getResumes(hhOAuth: HHOAuth): List<ResumeDTO> {
        val response = webClient.build()
                .get()
                .uri("https://api.hh.ru/resumes/mine")
                .header("Authorization", "Bearer ${hhOAuth.access_token}")
                .header("Content-Type", "application/json")
                .retrieve()
                .bodyToMono(Map::class.java)
                .block()


        response?.get("items")?.let { items ->
            (items as List<Map<*, *>>).forEach { item ->
                val id = item["id"] as? String ?: throw IllegalArgumentException("Resume ID cannot be null")
                resumesList.add(ResumeDTO(id))
            }
        }

        println("Полученные резюме: $resumesList")
        return resumesList
    }
}