package com.example.demo.service

import com.example.demo.models.exceptions.NotFoundException
import com.example.demo.models.user.User
import com.example.demo.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class UserService (
    private val userRepository: UserRepository
){
    fun profile(userId: Long) : User {
        val user = userRepository.findById(userId).orElseThrow {
            throw NotFoundException("User not found")
        }!!
        return user
    }

    fun save(user: User) : User {
        return userRepository.save(user)
    }

    fun getAll() : MutableList<User?> {
        return userRepository.findAll()
    }
}