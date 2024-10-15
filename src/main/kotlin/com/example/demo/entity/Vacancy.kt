package com.example.demo.entity

import com.example.demo.dto.VacancyDTO

data class Vacancy(
    val id: String,
    val title: String,
    val employer: String,
    val salary: String,
    val description: String,
    val url: String
){
    fun toDTO(): VacancyDTO {
        return VacancyDTO(id, url)
    }
}
