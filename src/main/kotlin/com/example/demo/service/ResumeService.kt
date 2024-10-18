package com.example.demo.service

import com.example.demo.dto.ResumeDTO
import com.example.demo.entity.HHOAuth
import com.example.demo.repository.HHOAuthRepository
import com.example.demo.repository.ResumeRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

@Service
class ResumeService(
        private val hhOAuthRepository: HHOAuthRepository,
        private val resumeRepository: ResumeRepository

) {

    fun searchResume(token:String): ResumeDTO {
        val hhOAuth = hhOAuthRepository.findByToken(token)
        val resumesList = resumeRepository.getResumes(hhOAuth)
        if(resumesList.isEmpty()) throw RuntimeException("Not fond resumes")
        hhOAuth.resumeId = resumesList.first().id
        hhOAuthRepository.save(hhOAuth)
        return resumesList.first()
    }
}