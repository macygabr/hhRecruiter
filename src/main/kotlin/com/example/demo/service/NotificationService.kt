package com.example.demo.service

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

import org.json.JSONObject

@Service
class NotificationService {
    val token = "YOUR_BOT_TOKEN"
    val message = "Привет, Telegram!"
    val chatId = "YOUR_CHAT_ID"


    private val logger: Logger = LoggerFactory.getLogger(NotificationService::class.java)
    private val scheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(1)
    private var scheduledTask: ScheduledFuture<*>? = null


    @EventListener(ApplicationReadyEvent::class)
    fun startNotificationSystem() {

        logger.info("")

        scheduledTask = scheduler.scheduleAtFixedRate(
            { sendNotificationByTelegramBot("123") },
            0,
            1,
            TimeUnit.DAYS
        )
    }

    fun sendNotificationByTelegramBot(message: String) {
        if(scheduledTask == null) return
        logger.info("Отправка уведомления в Telegram: $message")
        val url = "https://api.telegram.org/bot$token/sendMessage"

        val json = JSONObject()
        json.put("chat_id", chatId)
        json.put("text", message)

        val request = Request.Builder()
            .url(url)
            .post(json.toString().toRequestBody())
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw Exception("Failed to send message: ${response.body?.string()}")
            }
        }
    }
}