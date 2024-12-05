package com.example.demo.repository

import com.example.demo.models.HHOAuth
import org.springframework.data.jpa.repository.JpaRepository

interface HHOAuthRepository : JpaRepository<HHOAuth, Long> {
    fun findByUserId(userId: Long): HHOAuth?
    fun findByToken(token: String): HHOAuth?
}
