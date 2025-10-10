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

class ExchangeRateService {
    private val cacheMutex = Mutex()
    private val cache = mutableMapOf<CacheKey, CachedRates>()
    private val cacheTtlMillis = TimeUnit.MINUTES.toMillis(30)

    suspend fun latestRates(base: String, symbols: Set<String>, apiKey: String): Map<String, Double> {
        val trimmedKey = apiKey.trim()
        if (trimmedKey.isEmpty()) return emptyMap()

        val normalizedBase = base.uppercase()
        val normalizedSymbols = symbols.map(String::uppercase).filterNot { it == normalizedBase }.toSet()
        if (normalizedSymbols.isEmpty()) {
            return emptyMap()
        }

        val key = CacheKey(normalizedBase, normalizedSymbols.toList().sorted(), trimmedKey)
        val now = System.currentTimeMillis()

        cacheMutex.withLock {
            val cachedRates = cache[key]
            if (cachedRates != null && now - cachedRates.timestamp <= cacheTtlMillis) {
                return cachedRates.rates
            }
        }

        val fetched = fetchRates(normalizedBase, normalizedSymbols, trimmedKey)
        if (fetched.isEmpty()) {
            return emptyMap()
        }

        cacheMutex.withLock {
            cache[key] = CachedRates(timestamp = max(now, System.currentTimeMillis()), rates = fetched)
        }
        return fetched
    }

    private suspend fun fetchRates(base: String, symbols: Set<String>, apiKey: String): Map<String, Double> {
        val currenciesParam = (symbols + base).joinToString(",")
        val url = "https://api.currencylayer.com/live?access_key=$apiKey&currencies=$currenciesParam"

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
                    parseRates(response, base)
                } finally {
                    connection.disconnect()
                }
            }.getOrElse { emptyMap() }
        }
    }

    private fun parseRates(response: String, base: String): Map<String, Double> {
        val root = runCatching { JSONObject(response) }.getOrNull() ?: return emptyMap()
        if (!root.optBoolean("success", false)) {
            return emptyMap()
        }
        val quotes = root.optJSONObject("quotes") ?: return emptyMap()
        val source = root.optString("source", "USD").uppercase()
        val baseCode = base.uppercase()

        val sourceToBase = if (source == baseCode) {
            1.0
        } else {
            quotes.optDouble(source + baseCode, Double.NaN)
        }
        if (sourceToBase.isNaN() || sourceToBase <= 0.0) {
            return emptyMap()
        }

        val result = mutableMapOf<String, Double>()
        val iterator = quotes.keys()
        while (iterator.hasNext()) {
            val key = iterator.next()
            if (!key.startsWith(source)) continue
            val currencyCode = key.removePrefix(source).uppercase()
            if (currencyCode == baseCode) continue
            val rawRate = quotes.optDouble(key, Double.NaN)
            if (rawRate.isNaN() || rawRate <= 0.0) continue
            val rate = if (source == baseCode) rawRate else rawRate / sourceToBase
            result[currencyCode] = rate
        }
        return result
    }

    private data class CacheKey(val base: String, val symbols: List<String>, val apiKey: String)
    private data class CachedRates(val timestamp: Long, val rates: Map<String, Double>)

    private companion object {
        private const val DEFAULT_TIMEOUT = 10_000
    }
}
