package com.androidagent.llm.providers

import com.androidagent.data.database.entities.LLMConfig
import com.androidagent.llm.LLMMessage
import com.androidagent.llm.LLMProvider
import com.google.gson.Gson
import com.google.gson.JsonParser
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class OpenAICompatibleProvider : LLMProvider {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    private val gson = Gson()
    private val mediaType = "application/json; charset=utf-8".toMediaType()

    override suspend fun chat(messages: List<LLMMessage>, config: LLMConfig): String {
        val messagesJson = messages.map { mapOf("role" to it.role, "content" to it.content) }
        val body = mutableMapOf<String, Any>(
            "model" to config.modelName,
            "messages" to messagesJson,
            "temperature" to config.temperature,
            "max_tokens" to config.maxTokens
        )

        val requestBuilder = Request.Builder()
            .url("${config.baseUrl}/v1/chat/completions")
            .post(gson.toJson(body).toRequestBody(mediaType))

        if (config.apiKey.isNotBlank()) {
            requestBuilder.header("Authorization", "Bearer ${config.apiKey}")
        }

        val response = client.newCall(requestBuilder.build()).execute()
        if (!response.isSuccessful) {
            throw Exception("API error ${response.code}: ${response.body?.string()}")
        }

        val json = JsonParser.parseString(response.body?.string()).asJsonObject
        val choices = json.getAsJsonArray("choices") ?: return ""
        if (choices.size() == 0) return ""
        val message = choices[0].asJsonObject
            .getAsJsonObject("message")
            ?: return ""
        return message.get("content")?.asString?.trim() ?: ""
    }

    override suspend fun listModels(config: LLMConfig): List<String> {
        val requestBuilder = Request.Builder()
            .url("${config.baseUrl}/v1/models")
            .get()

        if (config.apiKey.isNotBlank()) {
            requestBuilder.header("Authorization", "Bearer ${config.apiKey}")
        }

        val response = client.newCall(requestBuilder.build()).execute()
        if (!response.isSuccessful) return emptyList()

        val json = JsonParser.parseString(response.body?.string()).asJsonObject
        val data = json.getAsJsonArray("data") ?: return emptyList()
        return data.map { it.asJsonObject.get("id").asString }
    }
}
