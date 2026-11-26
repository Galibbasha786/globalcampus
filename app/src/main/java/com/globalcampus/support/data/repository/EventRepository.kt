package com.globalcampus.support.data.repository

import com.globalcampus.support.data.model.EventItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class EventRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val uid get() = auth.currentUser?.uid ?: ""

    private val sampleEvents = listOf(
        EventItem("ev1", "One World Cultural Fest", "LPU's flagship international cultural extravaganza featuring food, dance & music from 50+ nations.", "Cultural", "15 Nov 2025", "10:00 AM", "Unipolis Grand Arena", "Division of International Affairs"),
        EventItem("ev2", "Hindi & Punjabi Language Exchange", "Interactive workshop for international students to practice conversational local phrases.", "Social", "18 Nov 2025", "4:00 PM", "Block 32 - Conference Room 3", "LPU Global Volunteers"),
        EventItem("ev3", "International Football Tournament", "Friendly inter-country 7v7 football matches for international students.", "Sports", "22 Nov 2025", "3:00 PM", "Main Outdoor Sports Complex", "LPU Sports Club")
    )

    suspend fun getEvents(): Result<List<EventItem>> {
        return try {
            val snap = db.collection("events").get().await()
            val list = snap.documents.mapNotNull { it.toObject(EventItem::class.java)?.copy(id = it.id) }
                .sortedByDescending { it.createdAt }

            if (list.isEmpty()) Result.success(sampleEvents) else Result.success(list)
        } catch (_: Exception) { Result.success(sampleEvents) }
    }

    suspend fun addEvent(event: EventItem): Result<Unit> = runCatching {
        val docRef = db.collection("events").document()
        val item = event.copy(id = docRef.id)
        docRef.set(item).await()
    }

    suspend fun toggleRsvp(eventId: String, currentRsvp: Boolean): Result<Unit> = runCatching {
        val ref = db.collection("events").document(eventId)
        if (currentRsvp) {
            ref.update("rsvps", FieldValue.arrayRemove(uid)).await()
        } else {
            ref.update("rsvps", FieldValue.arrayUnion(uid)).await()
        }
    }
}
