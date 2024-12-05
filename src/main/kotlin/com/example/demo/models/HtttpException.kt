package com.example.demo.models

import org.springframework.http.HttpStatus

class HttpException(
    val status: HttpStatus,
    message: String
) : Exception(message) {
    override fun toString(): String {
        return "HttpException(status=$status, message=${this.message})"
    }
}
