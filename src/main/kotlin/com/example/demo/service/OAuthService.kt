package com.example.demo.service

import com.example.demo.repository.HHOAuthRepository
import org.json.JSONObject
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

@Service
class OAuthService (
    private val hhOAuthRepository: HHOAuthRepository,
    private val webClient: WebClient.Builder
){

    @Value("\${refreshtoken.url}")
    private val refreshTokenUrl: String = ""

    @Value("\${client.id}")
    private val clientId: String = ""

    @Value("\${client.secret}")
    private val clientSecret: String = ""

    @Value("\${redirect.url}")
    private val redirectUrl: String = ""

    @Value("\${redirect.url}")
    private val redirectUri: String = ""

    private val scheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(1)
    private var scheduledTask: ScheduledFuture<*>? = null
    fun getURL():String{
        return "https://hh.ru/oauth/authorize?response_type=code&client_id=${clientId}&redirect_uri=${redirectUrl}"
    }

    fun callback(code:String) {
        val tokenResponse = getAccessToken(clientId, clientSecret, code, redirectUri)
        val hhOAuth = hhOAuthRepository.findByToken("1")
        hhOAuth.access_token = tokenResponse.accessToken
        hhOAuth.refresh_token = tokenResponse.refreshToken
        hhOAuth.expiresIn=tokenResponse.expiresIn
        hhOAuthRepository.save(hhOAuth)
    }
    fun refreshAccessToken() {
        println("Запуск обновения токена...")

        val hhOAuth = hhOAuthRepository.findByToken("1")
        val refreshToken = hhOAuth.refresh_token

        scheduledTask = hhOAuth.expiresIn?.let {
            scheduler.scheduleAtFixedRate(
                {
                    webClient.build()
                        .post()
                        .uri(refreshTokenUrl)
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .bodyValue("grant_type=refresh_token&refresh_token=$refreshToken&client_id=$clientId&client_secret=$clientSecret")
                        .retrieve()
                        .bodyToMono(String::class.java)
                        .map { response ->
                            val jsonResponse = JSONObject(response)
                            hhOAuth.access_token = jsonResponse.getString("access_token")
                            hhOAuthRepository.save(hhOAuth)
                        }
                        .subscribe()
                },
                0,
                it.toLong(),
                TimeUnit.MILLISECONDS
            )
        }
    }

    fun stopRefreshAccessToken(){
        scheduledTask?.cancel(true)
        scheduledTask = null
    }

    private fun getAccessToken(
        clientId: String,
        clientSecret: String,
        authorizationCode: String,
        redirectUri: String
    ): OAuthTokenResponse {
        val webClient = WebClient.builder()
            .baseUrl("https://hh.ru/oauth")
            .build()

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


        return responseBody.let {
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