package com.example.demo.service

import com.example.demo.dto.ResumeDTO
import com.example.demo.entity.HttpException
import com.example.demo.entity.Request
import com.example.demo.entity.Resume
import com.example.demo.repository.HHOAuthRepository
import com.example.demo.repository.ResumeRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

@Service
class ResumeService(
        private val hhOAuthRepository: HHOAuthRepository,
        private val resumeRepository: ResumeRepository
) {

    fun setResume(request:Request, resumeId: String) {
        val hhOAuth = hhOAuthRepository.findByToken(request.authorizationHeader)?:throw HttpException(HttpStatus.UNAUTHORIZED, "Token invalid")
        val accessToken = hhOAuth.access_token ?: throw HttpException(HttpStatus.FORBIDDEN, "Login hh.ru")

        val resumesList = resumeRepository.getResumes(accessToken)
        if(resumesList.isEmpty() || !resumesList.containsKey(resumeId)) throw HttpException(HttpStatus.NOT_FOUND, "Resume not found")

        hhOAuth.resumeId = resumesList[resumeId]?.id
        hhOAuthRepository.save(hhOAuth)
    }

    fun getResume(request:Request) : Resume {
        val hhOAuth = hhOAuthRepository.findByToken(request.authorizationHeader)?:throw HttpException(HttpStatus.UNAUTHORIZED, "Token invalid")
        val accessToken = hhOAuth.access_token ?: throw HttpException(HttpStatus.FORBIDDEN, "Login hh.ru")

        val resumesList = resumeRepository.getResumes(accessToken)
        return resumesList[hhOAuth.resumeId]?:throw RuntimeException("ResumeId invalid")
    }

    fun getAllResume(){

    }
}