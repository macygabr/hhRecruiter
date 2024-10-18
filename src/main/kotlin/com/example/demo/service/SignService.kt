package com.example.demo.service

import com.example.demo.entity.HHOAuth
import com.example.demo.entity.SignRequest
import com.example.demo.repository.HHOAuthRepository
import org.springframework.stereotype.Service

@Service
class SignService(
        private val hhOAuthRepository: HHOAuthRepository
) {
    fun signUp(request: SignRequest){
        val hhOAuth = HHOAuth()
        hhOAuth.token=request.token
        hhOAuth.userId=request.userId
        hhOAuthRepository.save(hhOAuth)
    }

    fun signIn(request: SignRequest){
        hhOAuthRepository.findByUserId(request.userId).token = request.token
    }
    fun signOut(request: SignRequest){
        hhOAuthRepository.findByUserId(request.userId).token = null
    }
}