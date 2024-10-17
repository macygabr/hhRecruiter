package com.example.demo.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class ResumeService {

    @Value("\${resume.id}")
    private val resumeId: String = ""

    fun getResumeId():String{
        return resumeId
    }
}