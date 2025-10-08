package com.andydel.financemanager.data.remote

import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit
import kotlin.math.max
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONObject

class ExchangeRateService(
    private val apiKeyProvider: ExchangeRateApiKeyProvider
) {
    private val cacheMutex = Mutex()
    private val cache = mutableMapOf<CacheKey, CachedRates>()
    private val cacheTtlMillis = TimeUnit.MINUTES.toMillis(30)

    suspend fun latestRates(base: String, symbols: Set<String>): Map<String, Double> {
        val normalizedBase = base.uppercase()
        val normalizedSymbols = symbols.map(String::uppercase).filterNot { it == normalizedBase }.toSet()
        if (normalizedSymbols.isEmpty()) {
            return emptyMap()
        }

        val key = CacheKey(normalizedBase, normalizedSymbols.toList().sorted())
        val now = System.currentTimeMillis()

        cacheMutex.withLock {
            val cachedRates = cache[key]
            if (cachedRates != null && now - cachedRates.timestamp <= cacheTtlMillis) {
                return cachedRates.rates
            }
        }

        val fetched = fetchRates(normalizedBase, normalizedSymbols)
        if (fetched.isEmpty()) {
            return emptyMap()
        }

        cacheMutex.withLock {
            cache[key] = CachedRates(timestamp = max(now, System.currentTimeMillis()), rates = fetched)
        }
        return fetched
    }

    private suspend fun fetchRates(base: String, symbols: Set<String>): Map<String, Double> {
        val apiKey = apiKeyProvider.getApiKey() ?: return emptyMap()
        val symbolsParam = symbols.joinToString(",")
        val url = "https://api.exchangerate.host/latest?base=$base&symbols=$symbolsParam&access_key=$apiKey"

        return withContext(Dispatchers.IO) {
            runCatching {
                val connection = (URL(url).openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = DEFAULT_TIMEOUT
                    readTimeout = DEFAULT_TIMEOUT
                }

                try {
                    val stream = if (connection.responseCode in 200..299) {
                        connection.inputStream
                    } else {
                        connection.errorStream ?: connection.inputStream
                    }
                    val response = stream.bufferedReader().use { it.readText() }
                    parseRates(response)
                } finally {
                    connection.disconnect()
                }
            }.getOrElse { emptyMap() }
        }
    }

    private fun parseRates(response: String): Map<String, Double> {
        val root = runCatching { JSONObject(response) }.getOrNull() ?: return emptyMap()
        if (!root.optBoolean("success", false)) {
            return emptyMap()
        }
        val ratesJson = root.optJSONObject("rates") ?: return emptyMap()
        val iterator = ratesJson.keys()
        val result = mutableMapOf<String, Double>()
        while (iterator.hasNext()) {
            val code = iterator.next().uppercase()
            val value = ratesJson.optDouble(code, Double.NaN)
            if (!value.isNaN() && value > 0.0) {
                result[code] = value
            }
        }
        return result
    }

    private data class CacheKey(val base: String, val symbols: List<String>)
    private data class CachedRates(val timestamp: Long, val rates: Map<String, Double>)

    private companion object {
        private const val DEFAULT_TIMEOUT = 10_000
    }
}
