package com.globalcampus.support.data.repository

import com.globalcampus.support.data.model.LostFoundItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class LostFoundRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val uid get() = auth.currentUser?.uid ?: ""

    private val sampleItems = listOf(
        LostFoundItem("lf1", "b1", "Black Leather Wallet with Student ID", "Lost", "Personal Belongings", "Block 34 Food Court", "Contains LPU Student ID card and blue passport copy.", "Ph: +91-9876543210"),
        LostFoundItem("lf2", "b2", "Blue Water Bottle", "Found", "General", "Central Library - 2nd Floor", "Found near Reading Table 14.", "Left at Library Security Desk")
    )

    suspend fun getItems(): Result<List<LostFoundItem>> {
        return try {
            val snap = db.collection("lostfound").get().await()
            val list = snap.documents.mapNotNull { doc ->
                val item = doc.toObject(LostFoundItem::class.java)
                val isRes = doc.getBoolean("isResolved") ?: doc.getBoolean("resolved") ?: false
                item?.copy(id = doc.id, isResolved = isRes)
            }.sortedByDescending { it.createdAt }

            if (list.isEmpty()) Result.success(sampleItems) else Result.success(list)
        } catch (_: Exception) { Result.success(sampleItems) }
    }

    suspend fun addItem(
        title: String,
        type: String,
        category: String,
        location: String,
        desc: String,
        contact: String,
        imageUrl: String,
        authorId: String
    ): Result<Unit> = runCatching {
        val docRef = db.collection("lostfound").document()
        val item = LostFoundItem(
            id = docRef.id,
            authorId = authorId.ifBlank { uid },
            title = title,
            type = type,
            category = category,
            location = location,
            description = desc,
            contact = contact,
            imageUrl = imageUrl,
            isResolved = false,
            createdAt = System.currentTimeMillis()
        )
        docRef.set(item).await()
    }

    suspend fun markResolved(id: String): Result<Unit> = runCatching {
        db.collection("lostfound")
            .document(id)
            .update(mapOf("isResolved" to true, "resolved" to true))
            .await()
    }
}
