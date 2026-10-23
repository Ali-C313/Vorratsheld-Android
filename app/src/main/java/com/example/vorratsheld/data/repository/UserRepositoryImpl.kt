package com.example.vorratsheld.data.repository

import com.example.vorratsheld.data.model.UserProfile
import com.example.vorratsheld.data.remote.CollectionNames
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

// Dokument-ID = uid
class UserRepositoryImpl(
    firestore: FirebaseFirestore
) : UserRepository {

    private val usersCollection = firestore.collection(CollectionNames.USERS)

    override suspend fun getProfile(userId: String): Result<UserProfile> = runCatching {
        val snapshot = usersCollection.document(userId).get().await()
        snapshot.toObject(UserProfile::class.java) ?: UserProfile()
    }

    override suspend fun saveProfile(userId: String, profile: UserProfile): Result<Unit> = runCatching {
        usersCollection.document(userId).set(profile).await()
    }

    override suspend fun deleteProfile(userId: String): Result<Unit> = runCatching {
        usersCollection.document(userId).delete().await()
    }
}
