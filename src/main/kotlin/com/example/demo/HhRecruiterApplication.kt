package com.example.demo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class HhRecruiterApplication
fun main(args: Array<String>) {
    runApplication<HhRecruiterApplication>(*args)
}