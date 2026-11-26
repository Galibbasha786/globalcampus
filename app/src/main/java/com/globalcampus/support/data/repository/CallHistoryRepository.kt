package com.globalcampus.support.data.repository

import com.globalcampus.support.data.model.CallHistoryItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class CallHistoryRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val uid get() = auth.currentUser?.uid ?: ""

    private val sampleHistory = listOf(
        CallHistoryItem("ch1", "LPU Global Community", true, 245, System.currentTimeMillis() - 3600000, "Completed"),
        CallHistoryItem("ch2", "Amina Al-Mansoor", false, 120, System.currentTimeMillis() - 14400000, "Completed"),
        CallHistoryItem("ch3", "David Nguyen", false, 85, System.currentTimeMillis() - 86400000, "Completed")
    )

    suspend fun getCallHistory(): Result<List<CallHistoryItem>> = runCatching {
        if (uid.isBlank()) return@runCatching sampleHistory
        val snap = db.collection("users").document(uid).collection("call_history").get().await()
        val list = snap.documents.mapNotNull { doc ->
            doc.toObject(CallHistoryItem::class.java)?.copy(id = doc.id)
        }.sortedByDescending { it.timestamp }

        if (list.isEmpty()) sampleHistory else list
    }

    suspend fun recordCall(recipientName: String, isGroupCall: Boolean, durationSeconds: Int): Result<Unit> = runCatching {
        if (uid.isNotBlank()) {
            val docRef = db.collection("users").document(uid).collection("call_history").document()
            val item = CallHistoryItem(
                id = docRef.id,
                recipientName = recipientName,
                isGroupCall = isGroupCall,
                durationSeconds = durationSeconds,
                timestamp = System.currentTimeMillis(),
                status = "Completed"
            )
            docRef.set(item).await()
        }
    }
}
