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

    fun signIn(token: String, userId: Long) {
        val existingOAuth = hhOAuthRepository.findByUserId(userId)

        if (existingOAuth != null) {
            existingOAuth.token = token
            hhOAuthRepository.save(existingOAuth)
        } else {
            val newOAuth = HHOAuth().apply {
                this.userId = userId
                this.token = token
            }
            hhOAuthRepository.save(newOAuth)
        }
    }

    fun signOut(request: SignRequest){
        hhOAuthRepository.findByUserId(request.userId)?.token = null
    }
}