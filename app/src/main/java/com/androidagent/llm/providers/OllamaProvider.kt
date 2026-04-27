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

class OllamaProvider : LLMProvider {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    private val gson = Gson()
    private val mediaType = "application/json; charset=utf-8".toMediaType()

    override suspend fun chat(messages: List<LLMMessage>, config: LLMConfig): String {
        val messagesJson = messages.map { mapOf("role" to it.role, "content" to it.content) }
        val body = mapOf(
            "model" to config.modelName,
            "messages" to messagesJson,
            "stream" to true,
            "options" to mapOf(
                "temperature" to config.temperature,
                "num_predict" to config.maxTokens
            )
        )
        val requestBody = gson.toJson(body).toRequestBody(mediaType)
        val request = Request.Builder()
            .url("${config.baseUrl}/api/chat")
            .post(requestBody)
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            throw Exception("Ollama error ${response.code}: ${response.body?.string()}")
        }

        val sb = StringBuilder()
        response.body?.byteStream()?.bufferedReader()?.use { reader ->
            reader.forEachLine { line ->
                if (line.isNotBlank()) {
                    runCatching {
                        val json = JsonParser.parseString(line).asJsonObject
                        val messageObj = json.getAsJsonObject("message")
                        val content = messageObj?.get("content")?.asString ?: ""
                        sb.append(content)
                    }
                }
            }
        }
        return sb.toString().trim()
    }

    override suspend fun listModels(config: LLMConfig): List<String> {
        val request = Request.Builder()
            .url("${config.baseUrl}/api/tags")
            .get()
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) return emptyList()

        val json = JsonParser.parseString(response.body?.string()).asJsonObject
        val models = json.getAsJsonArray("models") ?: return emptyList()
        return models.map { it.asJsonObject.get("name").asString }
    }
}
