package com.example.demo.repository

import com.example.demo.models.user.HhAuthInfo
import org.springframework.data.jpa.repository.JpaRepository

interface HhAuthInfoRepository : JpaRepository<HhAuthInfo, Long> {

}
