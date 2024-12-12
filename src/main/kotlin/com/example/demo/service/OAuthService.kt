package com.example.demo.service

import com.example.demo.models.*
import com.example.demo.models.exceptions.NotFoundException
import com.example.demo.models.requests.Request
import com.example.demo.models.requests.RequestWithCode
import com.example.demo.models.user.HhAuthInfo
import com.example.demo.repository.HhAuthInfoRepository
import com.example.demo.repository.UserRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
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
    private val hhAuthInfoRepository: HhAuthInfoRepository,
    private val userRepository: UserRepository,
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
    private val objectMapper = ObjectMapper()

    fun getURL():String{
        return "https://hh.ru/oauth/authorize?response_type=code&client_id=${clientId}&redirect_uri=${redirectUrl}"
    }

    fun registry(userId: Long, code: String) {
        val user = userRepository.findById(userId).orElseThrow {
            throw NotFoundException("User not found")
        }!!

        val tokenResponse = getAccessToken(clientId, clientSecret, code, redirectUri)

        if(user.hhAuthInfo == null){
            user.hhAuthInfo = HhAuthInfo(
                user = user,
                access_token = tokenResponse.accessToken,
                refresh_token = tokenResponse.refreshToken,
                expiresIn = tokenResponse.expiresIn
            )
        } else {
            user.hhAuthInfo!!.access_token = tokenResponse.accessToken
            user.hhAuthInfo!!.refresh_token = tokenResponse.refreshToken
            user.hhAuthInfo!!.expiresIn = tokenResponse.expiresIn
        }
        userRepository.save(user)
    }


    fun refreshAccessToken(userId: Long) {
        println("Запуск обновения токена...")

//        val HhAuthInfo = hhAuthInfoRepository.findByToken(request.authorizationHeader)?:throw HttpException(HttpStatus.UNAUTHORIZED, "token invalid")
//        val refreshToken = HhAuthInfo.refresh_token
//        scheduledTask = HhAuthInfo.expiresIn?.let { expiresIn ->
//            scheduler.scheduleAtFixedRate(
//                {
//                    try {
//                        webClient.build()
//                            .post()
//                            .uri(refreshTokenUrl)
//                            .header("Content-Type", "application/x-www-form-urlencoded")
//                            .bodyValue("grant_type=refresh_token&refresh_token=$refreshToken&client_id=$clientId&client_secret=$clientSecret")
//                            .retrieve()
//                            .bodyToMono(String::class.java)
//                            .doOnError { e -> println("Ошибка при обращении к HH API: ${e.message}") }
//                            .onErrorReturn("")
//                            .map { response ->
//                                try {
//                                    val jsonResponse = JSONObject(response)
//                                    HhAuthInfo.access_token = jsonResponse.getString("access_token")
//                                    hhAuthInfoRepository.save(HhAuthInfo)
//                                } catch (jsonException: JSONException) {
//                                    println("Ошибка парсинга ответа: ${jsonException.message}")
//                                }
//                            }
//                            .subscribe()
//                    } catch (e: Exception) {
//                        println("Ошибка при обновлении токена: ${e.message}")
//                    }
//                },
//                    expiresIn.toLong(),
//                expiresIn.toLong(),
//                TimeUnit.SECONDS
//            )
//        }
    }


    fun stopRefreshAccessToken(request: Request){
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
                    val jsonNode = objectMapper.readTree(errorBody)
                    val error = jsonNode["error"]?.asText() ?: "Unknown error"
                    val errorDescription = jsonNode["error_description"]?.asText() ?: "No description available"
                    Mono.error(IllegalStateException("Error: $error, Description: $errorDescription"))
                }
            }
            .bodyToMono(String::class.java)
            .doOnError { e -> println("Error occurred while calling HH API: ${e.message}") }
            .block()


        return responseBody.let {
            val json = objectMapper.readTree(it)
            OAuthTokenResponse(
                accessToken = json["access_token"]?.asText() ?: throw IllegalArgumentException("Access token cannot be null"),
                tokenType = json["token_type"]?.asText() ?: throw IllegalArgumentException("Token type cannot be null"),
                refreshToken = json["refresh_token"]?.asText() ?: throw IllegalArgumentException("Refresh token cannot be null"),
                expiresIn = json["expires_in"]?.asInt() ?: throw IllegalArgumentException("Expires in cannot be null")
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