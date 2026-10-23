package com.example.vorratsheld.data.remote

import com.example.vorratsheld.data.remote.model.EmailJsRequest
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

// Verschickt die Support-Anfrage als Mail an dich
object EmailJsApi {
    private const val BASE_URL = "https://api.emailjs.com/"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val service: EmailJsApiService by lazy { retrofit.create(EmailJsApiService::class.java) }
}

interface EmailJsApiService {
    // Antwort ist nur Text ("OK"), deshalb reicht Response<Unit>
    @POST("api/v1.0/email/send")
    suspend fun send(@Body request: EmailJsRequest): Response<Unit>
}
