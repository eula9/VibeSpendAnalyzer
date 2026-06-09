package com.example.vibespendanalyzer

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * OpenAI 兼容格式的 Chat Completions 客户端（支持 DeepSeek、Kimi 等自定义 Base URL）。
 */
object AiChatClient {

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun chat(
        baseUrl: String,
        apiKey: String,
        model: String,
        userPrompt: String
    ): String = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            throw IllegalStateException("API Key 未配置")
        }

        val endpoint = buildChatCompletionsUrl(baseUrl)
        val requestJson = JSONObject().apply {
            put("model", model)
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", userPrompt)
                })
            })
        }

        val request = Request.Builder()
            .url(endpoint)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(requestJson.toString().toRequestBody(jsonMediaType))
            .build()

        httpClient.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                val errMsg = parseErrorMessage(body) ?: "HTTP ${response.code}"
                throw IllegalStateException("接口请求失败：$errMsg")
            }
            parseAssistantContent(body)
        }
    }

    /**
     * 兼容多种 Base URL 写法：
     * - https://api.deepseek.com
     * - https://api.deepseek.com/v1
     * - https://api.deepseek.com/v1/chat/completions
     */
    internal fun buildChatCompletionsUrl(baseUrl: String): String {
        val normalized = baseUrl.trim().trimEnd('/')
        return when {
            normalized.endsWith("/chat/completions") -> normalized
            normalized.endsWith("/v1") -> "$normalized/chat/completions"
            else -> "$normalized/v1/chat/completions"
        }
    }

    private fun parseAssistantContent(json: String): String {
        val root = JSONObject(json)
        val choices = root.optJSONArray("choices")
            ?: throw IllegalStateException("响应格式异常：缺少 choices 字段")
        if (choices.length() == 0) {
            throw IllegalStateException("响应格式异常：choices 为空")
        }
        val message = choices.getJSONObject(0).optJSONObject("message")
            ?: throw IllegalStateException("响应格式异常：缺少 message 字段")
        return message.optString("content").trim().ifEmpty {
            throw IllegalStateException("AI 返回了空内容")
        }
    }

    private fun parseErrorMessage(json: String): String? {
        return try {
            JSONObject(json).optJSONObject("error")?.optString("message")
        } catch (_: Exception) {
            json.take(200).ifEmpty { null }
        }
    }
}
