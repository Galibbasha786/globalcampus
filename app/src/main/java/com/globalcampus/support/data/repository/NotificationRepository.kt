package com.globalcampus.support.data.repository

import com.globalcampus.support.data.model.AppNotification
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class NotificationRepository {
    private val db = FirebaseFirestore.getInstance().collection("notifications")

    suspend fun sendNotification(notification: AppNotification): Result<Unit> = runCatching {
        val docRef = db.document()
        val notif = notification.copy(id = docRef.id)
        db.document(docRef.id).set(notif).await()
    }

    suspend fun getMyNotifications(userId: String): Result<List<AppNotification>> = runCatching {
        val personalSnap = db.whereEqualTo("userId", userId).get().await()
        val broadcastSnap = db.whereEqualTo("userId", "all").get().await()

        val personal = personalSnap.toObjects(AppNotification::class.java)
        val broadcast = broadcastSnap.toObjects(AppNotification::class.java)

        (personal + broadcast)
            .distinctBy { it.id }
            .sortedByDescending { it.timestamp }
    }

    suspend fun deleteNotification(id: String): Result<Unit> = runCatching {
        db.document(id).delete().await()
    }

    suspend fun deleteAllNotifications(userId: String): Result<Unit> = runCatching {
        val personalSnap = db.whereEqualTo("userId", userId).get().await()
        personalSnap.documents.forEach { doc ->
            db.document(doc.id).delete().await()
        }
        val broadcastSnap = db.whereEqualTo("userId", "all").get().await()
        broadcastSnap.documents.forEach { doc ->
            db.document(doc.id).delete().await()
        }
    }
}
