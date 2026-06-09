package com.example.vibespendanalyzer.data

import android.content.Context

class AiSettingsRepository(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun load(): AiSettings = AiSettings(
        apiKey = prefs.getString(KEY_API_KEY, "").orEmpty(),
        baseUrl = prefs.getString(KEY_BASE_URL, AiSettingsDefaults.BASE_URL)
            ?: AiSettingsDefaults.BASE_URL,
        modelName = prefs.getString(KEY_MODEL_NAME, AiSettingsDefaults.MODEL_NAME)
            ?: AiSettingsDefaults.MODEL_NAME
    )

    fun save(settings: AiSettings) {
        prefs.edit()
            .putString(KEY_API_KEY, settings.apiKey.trim())
            .putString(KEY_BASE_URL, settings.baseUrl.trim())
            .putString(KEY_MODEL_NAME, settings.modelName.trim())
            .apply()
    }

    fun hasApiKey(): Boolean = load().apiKey.isNotBlank()

    companion object {
        private const val PREFS_NAME = "ai_settings"
        private const val KEY_API_KEY = "api_key"
        private const val KEY_BASE_URL = "base_url"
        private const val KEY_MODEL_NAME = "model_name"
    }
}
