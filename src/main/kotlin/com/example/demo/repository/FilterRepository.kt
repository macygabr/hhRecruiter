package com.example.demo.repository

import com.example.demo.models.Filter
import com.example.demo.models.HHOAuth
import org.springframework.data.jpa.repository.JpaRepository

interface FilterRepository: JpaRepository<Filter, Long> {

}