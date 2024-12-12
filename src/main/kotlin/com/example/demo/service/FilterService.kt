package com.example.demo.service

import com.example.demo.models.exceptions.NotFoundException
import com.example.demo.models.user.Filter
import com.example.demo.models.user.User
import com.example.demo.repository.FilterRepository
import com.example.demo.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class FilterService (
    private val userRepository: UserRepository
){
    fun setDefaultFilter(userId: Long): User {
        val user = userRepository.findById(userId).orElseThrow {
            throw NotFoundException("User not found")
        }!!

        if(user.filter == null) {
            println("Создание фильтра...")
            val filter = Filter()
            filter.user = user
            user.filter = filter
            println("Сохранение фильтра...")
            userRepository.save(user)
        }
        return user
    }

    fun setFilter(userId: Long, filter: Filter){
        val user = setDefaultFilter(userId)

        user.filter!!.copyFrom(filter)
    }
}