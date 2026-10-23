package com.example.vorratsheld.data.remote

import com.example.vorratsheld.data.remote.model.OpenFoodFactsResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

// Zugriff auf Open Food Facts mit Retrofit + Moshi
object OpenFoodFactsApi {
    private const val BASE_URL = "https://world.openfoodfacts.org/"

    // Felder, die wir nicht brauchen, ignoriert Moshi
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val service: OpenFoodFactsApiService by lazy { retrofit.create(OpenFoodFactsApiService::class.java) }
}

interface OpenFoodFactsApiService {
    // Beispiel: GET https://world.openfoodfacts.org/api/v2/product/4008400301012.json
    @GET("api/v2/product/{barcode}.json")
    suspend fun getProduct(@Path("barcode") barcode: String): OpenFoodFactsResponse
}
