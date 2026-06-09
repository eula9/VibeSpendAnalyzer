package com.example.vibespendanalyzer.data

data class AiSettings(
    val apiKey: String = "",
    val baseUrl: String = AiSettingsDefaults.BASE_URL,
    val modelName: String = AiSettingsDefaults.MODEL_NAME
)

object AiSettingsDefaults {
    const val BASE_URL = "https://api.deepseek.com/v1"
    const val MODEL_NAME = "deepseek-chat"
}

const val MISSING_API_KEY_MESSAGE =
    "⚠️ 请先前往设置页面配置您的 AI 模型 API Key！"
