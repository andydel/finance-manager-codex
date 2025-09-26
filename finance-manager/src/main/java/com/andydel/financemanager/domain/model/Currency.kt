package com.andydel.financemanager.domain.model

data class Currency(
    val id: Long,
    val name: String,
    val symbol: String
) {
    val displayName: String get() = "$symbol $name"
}
