package com.example.demo.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter


@Configuration
class GlobalCorsConfig {
    @Bean
    fun corsFilter(): CorsFilter {
        val config = CorsConfiguration()
        config.addAllowedOrigin("http://37.194.168.90:3000")
        config.addAllowedHeader("*")
        config.addAllowedMethod("*")
        config.allowCredentials = true
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", config)
        return CorsFilter(source)
    }
}

