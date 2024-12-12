package com.example.demo.repository

import com.example.demo.models.user.Resume
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.web.reactive.function.client.WebClient

interface ResumeRepository : JpaRepository<Resume, Long> {

}