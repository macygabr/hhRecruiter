package com.example.demo.service

import com.example.demo.models.user.Filter
import com.example.demo.models.user.User
import com.example.demo.repository.FilterRepository
import org.springframework.stereotype.Service

@Service
class FilterService (
    private val userService: UserService,
    private val filterRepository: FilterRepository
){
    fun setDefaultFilter(userId: Long): User {
        val user = userService.profile(userId)
        if(user.filter == null) {
            val filter = Filter()
            filter.user = user
            user.filter = filter
            userService.save(user)
        }
        return user
    }

    fun setFilter(userId: Long, filter: Filter){
        val user = setDefaultFilter(userId)
        val newFilter =filterRepository.findById(user.filter!!.id).get().copyFrom(filter)
        filterRepository.save(newFilter)
    }
}