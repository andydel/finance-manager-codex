package com.andydel.financemanager.data.remote

import android.content.Context
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ExchangeRateApiKeyProvider(private val context: Context) {
    @Volatile
    private var cachedKey: String? = null

    suspend fun getApiKey(): String? {
        cachedKey?.let { return it }
        val key = withContext(Dispatchers.IO) { readKey() }
        cachedKey = key
        return key
    }

    private fun readKey(): String? {
        val candidates = buildList {
            val home = System.getProperty("user.home")
            if (!home.isNullOrBlank()) {
                add(File(home, KEY_FILE_NAME))
            }
            add(File(context.filesDir, KEY_FILE_NAME))
        }
        candidates.forEach { file ->
            val value = runCatching { file.takeIf { it.exists() && it.canRead() }?.readText()?.trim() }
                .getOrNull()
            if (!value.isNullOrBlank()) {
                return value
            }
        }
        return null
    }

    private companion object {
        const val KEY_FILE_NAME = "exchangerate.key"
    }
}
