package com.example.vorratsheld.data.remote.model

import com.squareup.moshi.Json

// Anfrage an die EmailJS-API
data class EmailJsRequest(
    @Json(name = "service_id") val serviceId: String,
    @Json(name = "template_id") val templateId: String,
    @Json(name = "user_id") val publicKey: String,
    val accessToken: String,
    @Json(name = "template_params") val templateParams: Map<String, String>
)
