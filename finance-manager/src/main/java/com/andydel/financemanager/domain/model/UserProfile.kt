package com.andydel.financemanager.domain.model

data class UserProfile(
    val id: Long = 0L,
    val name: String,
    val baseCurrency: Currency,
    val exchangeRateApiKey: String?
)
