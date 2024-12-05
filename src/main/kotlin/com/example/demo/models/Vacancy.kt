package com.example.demo.models

import com.example.demo.dto.VacancyDTO

data class Vacancy(
    val id: String,
    val title: String,
    val salary: String,
    val responseLetterRequired: Boolean,
    val description: String,
    val url: String
){
    fun toDTO(): VacancyDTO {
        return VacancyDTO(id, url)
    }
}
