package com.example.demo.service

import com.example.demo.models.exceptions.AuthenticationException
import com.example.demo.models.exceptions.NotFoundException
import com.example.demo.models.requests.Request
import com.example.demo.models.user.Resume
import com.example.demo.repository.ResumeRepository
import com.example.demo.repository.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

@Service
class ResumeService(
    private val userService: UserService
) {
    fun setDefault(userId: Long) :Resume{
        println("Поиск пользователя...")
        val user = userService.profile(userId)

        if(user.hhAuthInfo == null) throw AuthenticationException("User not authenticated in HH")
        val resume = getFirstResume(user.hhAuthInfo?.access_token!!)
        resume.user = user
        user.resume = resume
        println("Сохранение резюме...")
        userService.save(user)
        return resume
    }

    private fun getResumesFromHH(accessToken: String): HashMap<String, Resume> {
        println("Поиск резюме...")
        val webClient = WebClient.builder()
        val resumesList = HashMap<String, Resume>()

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
        println("Резюме найдено: ${resumesList.size}")
        return resumesList
    }

    private fun getFirstResume(accessToken: String): Resume {
        val resumesList = getResumesFromHH(accessToken)
        return resumesList.values.first()
    }
}