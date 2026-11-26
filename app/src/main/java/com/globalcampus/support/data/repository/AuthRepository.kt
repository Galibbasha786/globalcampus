package com.globalcampus.support.data.repository

import com.globalcampus.support.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    val currentUser get() = auth.currentUser
    val isLoggedIn get() = auth.currentUser != null

    suspend fun signUp(
        email: String,
        password: String,
        name: String,
        regdNo: String = "",
        country: String = "",
        petName: String = "",
        favoriteTeacher: String = "",
        birthCity: String = ""
    ): Result<Unit> = try {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val uid = result.user?.uid ?: throw Exception("No UID")
        val user = User(
            uid = uid,
            name = name,
            email = email,
            regdNo = regdNo,
            country = country,
            petName = petName.trim().lowercase(),
            favoriteTeacher = favoriteTeacher.trim().lowercase(),
            birthCity = birthCity.trim().lowercase()
        )
        db.collection("users").document(uid).set(user).await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    suspend fun signIn(email: String, password: String): Result<Unit> = try {
        auth.signInWithEmailAndPassword(email, password).await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    suspend fun verifySecurityQuestionsOnly(
        email: String,
        petName: String,
        favoriteTeacher: String,
        birthCity: String
    ): Result<Unit> = try {
        val snap = db.collection("users")
            .whereEqualTo("email", email.trim())
            .get().await()

        if (snap.isEmpty) throw Exception("User with email '$email' not found")

        val doc = snap.documents.first()
        val savedPet = doc.getString("petName")?.trim()?.lowercase() ?: ""
        val savedTeacher = doc.getString("favoriteTeacher")?.trim()?.lowercase() ?: ""
        val savedCity = doc.getString("birthCity")?.trim()?.lowercase() ?: ""

        if (savedPet != petName.trim().lowercase() ||
            savedTeacher != favoriteTeacher.trim().lowercase() ||
            savedCity != birthCity.trim().lowercase()) {
            throw Exception("Security question answers do not match our records.")
        }

        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    suspend fun signInWithGoogle(idToken: String): Result<Unit> = try {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = auth.signInWithCredential(credential).await()
        val user = result.user ?: throw Exception("No user")
        val doc = db.collection("users").document(user.uid).get().await()
        if (!doc.exists()) {
            db.collection("users").document(user.uid)
                .set(User(
                    uid = user.uid,
                    name = user.displayName ?: "",
                    email = user.email ?: "",
                    avatarUrl = user.photoUrl?.toString() ?: ""
                )).await()
        }
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    suspend fun changePassword(newPassword: String): Result<Unit> = try {
        val user = auth.currentUser ?: throw Exception("Not signed in")
        user.updatePassword(newPassword).await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    suspend fun deleteAccount(): Result<Unit> = try {
        val user = auth.currentUser ?: throw Exception("Not signed in")
        val uid = user.uid
        db.collection("users").document(uid).delete().await()
        user.delete().await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    suspend fun resetPasswordDirectly(email: String, newPassword: String): Result<Unit> = try {
        // Sign in or re-authenticate, or update user directly
        val snap = db.collection("users").whereEqualTo("email", email.trim()).get().await()
        if (snap.isEmpty) throw Exception("User not found")
        // Trigger firebase password reset email as fallback or complete direct verification
        auth.sendPasswordResetEmail(email.trim()).await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    fun signOut() = auth.signOut()
}
