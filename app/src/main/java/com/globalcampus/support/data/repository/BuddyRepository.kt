package com.globalcampus.support.data.repository

import com.globalcampus.support.data.model.AppNotification
import com.globalcampus.support.data.model.BuddyProfile
import com.globalcampus.support.data.model.BuddyRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class BuddyRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val uid get() = auth.currentUser?.uid ?: ""

    private val sampleBuddies = listOf(
        BuddyProfile(
            uid = "b1",
            name = "Amina Al-Mansoor",
            regdNo = "12204859",
            country = "Nigeria",
            course = "B.Tech CSE",
            languages = listOf("English", "Hausa"),
            interests = listOf("Coding", "Football", "Music"),
            avatarUrl = "https://i.pravatar.cc/150?img=1",
            bio = "Looking for study partners in CSE Block 34!"
        ),
        BuddyProfile(
            uid = "b2",
            name = "David Nguyen",
            regdNo = "12218940",
            country = "Vietnam",
            course = "MBA",
            languages = listOf("English", "Vietnamese"),
            interests = listOf("Badminton", "Photography", "Travel"),
            avatarUrl = "https://i.pravatar.cc/150?img=2",
            bio = "Excited to explore Punjab & university sports!"
        ),
        BuddyProfile(
            uid = "b3",
            name = "Sonia Sharma",
            regdNo = "12209312",
            country = "Nepal",
            course = "B.Pharm",
            languages = listOf("English", "Nepali", "Hindi"),
            interests = listOf("Dancing", "Reading", "Cooking"),
            avatarUrl = "https://i.pravatar.cc/150?img=3",
            bio = "Happy to guide new international arrivals!"
        ),
        BuddyProfile(
            uid = "b4",
            name = "Kwame Mensah",
            regdNo = "12210492",
            country = "Ghana",
            course = "BCA",
            languages = listOf("English", "Twi"),
            interests = listOf("Basketball", "Tech", "Gaming"),
            avatarUrl = "https://i.pravatar.cc/150?img=4",
            bio = "LPU Basketball team player. Connect anytime!"
        )
    )

    suspend fun getBuddyProfiles(): Result<List<BuddyProfile>> {
        return try {
            val snap = db.collection("users").get().await()
            val list = snap.documents.mapNotNull { doc ->
                val u = doc.toObject(BuddyProfile::class.java)?.copy(uid = doc.id)
                if (u != null && u.uid != uid && u.name.isNotBlank()) u else null
            }
            if (list.isEmpty()) Result.success(sampleBuddies) else Result.success(list)
        } catch (_: Exception) { Result.success(sampleBuddies) }
    }

    suspend fun sendBuddyRequest(receiverUid: String): Result<Unit> {
        return try {
            val ref = db.collection("buddy_requests").document()
            val senderEmail = auth.currentUser?.email?.substringBefore("@") ?: "Student"
            val req = BuddyRequest(id = ref.id, senderUid = uid, receiverUid = receiverUid)
            ref.set(req).await()

            // Dispatch Notification to Target Receiver
            val notifRef = db.collection("notifications").document()
            val notif = AppNotification(
                id = notifRef.id,
                userId = receiverUid,
                title = "Buddy Connection Request",
                message = "$senderEmail wants to connect as a Buddy. Tap Accept below!",
                isRead = false,
                timestamp = System.currentTimeMillis()
            )
            notifRef.set(notif).await()

            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }
}
