package com.example.vibespendanalyzer.data

import android.content.Context

/**
 * 全局 AI 配置访问入口，在 [MainActivity] 启动时初始化。
 */
object AiSettingsStore {

    private lateinit var repository: AiSettingsRepository

    fun init(context: Context) {
        repository = AiSettingsRepository(context)
    }

    fun load(): AiSettings {
        checkInitialized()
        return repository.load()
    }

    fun save(settings: AiSettings) {
        checkInitialized()
        repository.save(settings)
    }

    fun hasApiKey(): Boolean {
        checkInitialized()
        return repository.hasApiKey()
    }

    private fun checkInitialized() {
        check(::repository.isInitialized) {
            "AiSettingsStore 尚未初始化，请在 MainActivity.onCreate 中调用 init()"
        }
    }
}
