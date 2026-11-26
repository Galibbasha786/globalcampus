package com.globalcampus.support.data.repository

import com.globalcampus.support.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class UserRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun getMyProfile(): Result<User> = try {
        val uid = auth.currentUser?.uid ?: throw Exception("Not logged in")
        val email = auth.currentUser?.email ?: ""
        val doc = db.collection("users").document(uid).get().await()
        val user = doc.toObject(User::class.java)?.copy(uid = uid)
            ?: User(uid = uid, email = email, name = email.substringBefore("@"))
        Result.success(user)
    } catch (e: Exception) { Result.failure(e) }

    suspend fun updateProfile(name: String, country: String, course: String, avatarUrl: String = ""): Result<Unit> = try {
        val uid = auth.currentUser?.uid ?: throw Exception("Not logged in")
        val data = mutableMapOf<String, Any>(
            "name" to name,
            "country" to country,
            "course" to course
        )
        if (avatarUrl.isNotBlank()) {
            data["avatarUrl"] = avatarUrl
        }
        db.collection("users").document(uid).set(data, SetOptions.merge()).await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }
}