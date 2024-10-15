package com.example.demo.controller

import com.example.demo.repository.UserRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.http.MediaType
import org.json.JSONObject
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.reactive.function.BodyInserters

@RestController
class OAuthController(
    private val userRepository: UserRepository
) {

    @Value("\${client.id}")
    private val clientId: String = ""

    @Value("\${client.secret}")
    private val clientSecret: String = ""

    @Value("\${redirect.url}")
    private val redirectUri: String = ""

    @GetMapping("/callback")
    fun callback(@RequestParam("code") code: String): String {

        val tokenResponse = getAccessToken(clientId, clientSecret, code, redirectUri)

        val user = userRepository.findByName("macygabr")
        if (user != null && tokenResponse != null) {
            user.access_token = tokenResponse.accessToken
            user.refresh_token = tokenResponse.refreshToken
            userRepository.save(user)
        }

        return "Готов начать, перейди на /start_monitoring"
    }

    fun getAccessToken(
        clientId: String,
        clientSecret: String,
        authorizationCode: String,
        redirectUri: String
    ): OAuthTokenResponse? {
        val webClient = WebClient.builder()
            .baseUrl("https://hh.ru/oauth")
            .build()

        val body = mapOf(
            "grant_type" to "authorization_code",
            "client_id" to clientId,
            "client_secret" to clientSecret,
            "code" to authorizationCode,
            "redirect_uri" to redirectUri
        )

        val responseBody = webClient.post()
            .uri("/token")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(
                BodyInserters.fromFormData("grant_type", "authorization_code")
                    .with("client_id", clientId)
                    .with("client_secret", clientSecret)
                    .with("code", authorizationCode)
                    .with("redirect_uri", redirectUri)
            )
            .retrieve()
            .bodyToMono(String::class.java)
            .block()


        return responseBody?.let {
            val json = JSONObject(it)
            OAuthTokenResponse(
                accessToken = json.getString("access_token"),
                tokenType = json.getString("token_type"),
                refreshToken = json.getString("refresh_token"),
                expiresIn = json.getInt("expires_in")
            )
        }
    }
}

data class OAuthTokenResponse(
    val accessToken: String,
    val tokenType: String,
    val refreshToken: String,
    val expiresIn: Int
)
