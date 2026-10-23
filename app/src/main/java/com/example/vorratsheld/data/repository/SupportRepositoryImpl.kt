package com.example.vorratsheld.data.repository

import com.example.vorratsheld.data.model.SupportRequest
import com.example.vorratsheld.data.remote.CollectionNames
import com.example.vorratsheld.data.remote.EmailJsApiService
import com.example.vorratsheld.data.remote.EmailJsConfig
import com.example.vorratsheld.data.remote.model.EmailJsRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

// Speichert die Anfrage in Firestore und schickt sie zusätzlich per Mail
class SupportRepositoryImpl(
    firestore: FirebaseFirestore,
    private val emailJsApi: EmailJsApiService
) : SupportRepository {

    private val requestsCollection = firestore.collection(CollectionNames.SUPPORT_REQUESTS)

    override suspend fun send(request: SupportRequest): Result<Unit> = runCatching {
        // Wenn die Mail nicht klappt, wird die Anfrage trotzdem gespeichert
        val emailSent = EmailJsConfig.isConfigured && runCatching { sendMail(request) }.isSuccess

        requestsCollection.add(request.copy(emailSent = emailSent)).await()
    }

    private suspend fun sendMail(request: SupportRequest) {
        val response = emailJsApi.send(
            EmailJsRequest(
                serviceId = EmailJsConfig.SERVICE_ID,
                templateId = EmailJsConfig.TEMPLATE_ID,
                publicKey = EmailJsConfig.PUBLIC_KEY,
                accessToken = EmailJsConfig.PRIVATE_KEY,
                templateParams = mapOf(
                    "category" to request.category.name,
                    "subject" to request.subject,
                    "message" to request.message,
                    "reply_to" to request.replyEmail,
                    "user_id" to request.userId,
                    "platform" to request.platform,
                    "app_version" to request.appVersion
                )
            )
        )
        if (!response.isSuccessful) {
            throw Exception("EmailJS: ${response.code()}")
        }
    }
}
