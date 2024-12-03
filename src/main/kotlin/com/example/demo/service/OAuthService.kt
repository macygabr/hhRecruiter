package com.example.demo.service

import com.example.demo.repository.HHOAuthRepository
import com.example.demo.entity.AuthenticationServerResponse
import com.example.demo.entity.HHOAuth
import com.example.demo.entity.Request
import com.example.demo.entity.Response
import org.json.JSONException
import org.json.JSONObject
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
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


    fun status(request: Request): HHOAuth? {
        return hhOAuthRepository.findByToken(request.authorizationHeader)
    }

    fun userAuthInHH(request:  Request):Boolean {
        val hhoauth = hhOAuthRepository.findByToken(request.authorizationHeader)?:throw RuntimeException("User not found")
        return hhoauth.access_token!=null && hhoauth.refresh_token !=null
    }
    fun callback(request:Request) {
        if(request.code == null) throw RuntimeException("code is null")
        val tokenResponse = getAccessToken(clientId, clientSecret, request.code!!, redirectUri)
        val hhOAuth = hhOAuthRepository.findByToken(request.authorizationHeader)?:throw RuntimeException("User not found")

        hhOAuth.access_token = tokenResponse.accessToken
        hhOAuth.refresh_token = tokenResponse.refreshToken
        hhOAuth.expiresIn=tokenResponse.expiresIn
        hhOAuthRepository.save(hhOAuth)
    }

    fun refreshAccessToken(request: Request) {
        println("Запуск обновения токена...")

        val hhOAuth = hhOAuthRepository.findByToken(request.authorizationHeader)?:throw RuntimeException("User not found")
        val refreshToken = hhOAuth.refresh_token
        scheduledTask = hhOAuth.expiresIn?.let { expiresIn ->
            scheduler.scheduleAtFixedRate(
                {
                    try {
                        webClient.build()
                            .post()
                            .uri(refreshTokenUrl)
                            .header("Content-Type", "application/x-www-form-urlencoded")
                            .bodyValue("grant_type=refresh_token&refresh_token=$refreshToken&client_id=$clientId&client_secret=$clientSecret")
                            .retrieve()
                            .bodyToMono(String::class.java)
                            .doOnError { e -> println("Ошибка при обращении к HH API: ${e.message}") }
                            .onErrorReturn("")
                            .map { response ->
                                try {
                                    val jsonResponse = JSONObject(response)
                                    hhOAuth.access_token = jsonResponse.getString("access_token")
                                    hhOAuthRepository.save(hhOAuth)
                                } catch (jsonException: JSONException) {
                                    println("Ошибка парсинга ответа: ${jsonException.message}")
                                }
                            }
                            .subscribe()
                    } catch (e: Exception) {
                        println("Ошибка при обновлении токена: ${e.message}")
                    }
                },
                    expiresIn.toLong(),
                expiresIn.toLong(),
                TimeUnit.SECONDS
            )
        }
    }


    fun stopRefreshAccessToken(token:String){
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
            .onStatus({ status -> status.is4xxClientError || status.is5xxServerError }) { response ->
                response.bodyToMono(String::class.java).flatMap { errorBody ->
                    val errorJson = JSONObject(errorBody)
                    val error = errorJson.optString("error", "Unknown error")
                    val errorDescription = errorJson.optString("error_description", "No description available")
                    Mono.error(IllegalStateException("Error: $error, Description: $errorDescription"))
                }
            }
            .bodyToMono(String::class.java)
            .doOnError { e -> println("Error occurred while calling HH API: ${e.message}") }
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