package com.example.vorratsheld.data.remote.model

import com.squareup.moshi.Json

// Nur die Felder, die wir für Vorratsheld tatsächlich brauchen
data class OpenFoodFactsProductDto(
    @Json(name = "product_name") val productName: String? = null,
    @Json(name = "image_url") val imageUrl: String? = null
)
