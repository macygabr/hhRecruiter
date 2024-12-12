package com.example.demo.models.user

data class Vacancy(
    val id: String,
    val title: String,
    val salary: String,
    val responseLetterRequired: Boolean,
    val description: String,
    val url: String
)