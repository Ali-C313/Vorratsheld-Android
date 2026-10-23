package com.example.vorratsheld.data.remote.model

// Antwort der Open Food Facts API für einen einzelnen Barcode
data class OpenFoodFactsResponse(
    val status: Int = 0,
    val product: OpenFoodFactsProductDto? = null
)
