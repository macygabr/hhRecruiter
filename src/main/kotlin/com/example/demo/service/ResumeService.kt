package com.example.demo.service

import com.example.demo.dto.ResumeDTO
import com.example.demo.repository.HHOAuthRepository
import com.example.demo.repository.ResumeRepository
import org.springframework.stereotype.Service

@Service
class ResumeService(
        private val hhOAuthRepository: HHOAuthRepository,
        private val resumeRepository: ResumeRepository
) {

    fun searchResume(token:String): ResumeDTO {
        val hhOAuth = hhOAuthRepository.findByToken(token)?:throw RuntimeException("User not found")
        val resumesList = resumeRepository.getResumes(hhOAuth)
        if(resumesList.isEmpty()) throw RuntimeException("Not fond resumes")
        hhOAuth.resumeId = resumesList.first().id
        hhOAuthRepository.save(hhOAuth)
        return resumesList.first()
    }
}