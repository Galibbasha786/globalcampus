package com.globalcampus.support.data.repository

import com.globalcampus.support.data.model.AppNotification
import com.globalcampus.support.data.model.ChatGroup
import com.globalcampus.support.data.model.ChatMessageItem
import com.globalcampus.support.data.model.UserStory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ChatRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val notifRepo = NotificationRepository()
    private val uid get() = auth.currentUser?.uid ?: ""

    private val defaultGroups = listOf(
        ChatGroup("global", "🌍 LPU Global Lounge", "global", "Official real-time campus chat room for all international students.", "", "", emptyList(), "🌍"),
        ChatGroup("grp_cse", "💻 B.Tech CSE International", "group", "Study & project discussion group for CSE international students.", "B.Tech CSE", "", emptyList(), "💻"),
        ChatGroup("grp_mba", "💼 LPU MBA Global Network", "group", "Management case studies, networking & career group.", "MBA", "", emptyList(), "💼"),
        ChatGroup("grp_africa", "🌍 African Students Union", "group", "Community group for African students at LPU.", "", "Africa", emptyList(), "🌍"),
        ChatGroup("grp_nepal", "🇳🇵 Nepalese Students Club", "group", "Cultural & festival meetup group for Nepalese students.", "", "Nepal", emptyList(), "🇳🇵")
    )

    // Real-Time Flow for Messages in a Chat
    fun getRealtimeMessages(chatId: String): Flow<List<ChatMessageItem>> = callbackFlow {
        val listener: ListenerRegistration = db.collection("chats")
            .document(chatId)
            .collection("messages")
            .addSnapshotListener { snap, err ->
                if (err != null || snap == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val msgs = snap.documents.mapNotNull { doc ->
                    doc.toObject(ChatMessageItem::class.java)?.copy(id = doc.id)
                }.sortedBy { it.timestamp }
                trySend(msgs)
            }
        awaitClose { listener.remove() }
    }

    // Real-Time Flow for Active Video Call in a Group
    fun getRealtimeActiveCall(groupId: String): Flow<Boolean> = callbackFlow {
        val listener = db.collection("active_calls")
            .document(groupId)
            .addSnapshotListener { snap, err ->
                if (err != null || snap == null || !snap.exists()) {
                    trySend(false)
                    return@addSnapshotListener
                }
                val active = snap.getBoolean("active") ?: false
                trySend(active)
            }
        awaitClose { listener.remove() }
    }

    // Start Group Video Call
    suspend fun startGroupCall(groupId: String, groupName: String): Result<Unit> = try {
        val email = auth.currentUser?.email?.substringBefore("@") ?: "Student"
        db.collection("active_calls").document(groupId).set(
            mapOf("active" to true, "startedBy" to email, "timestamp" to System.currentTimeMillis())
        ).await()

        // Send Broadcast Notification to all group members
        notifRepo.sendNotification(
            AppNotification(
                userId = "all",
                title = "🎥 Incoming Group Video Call in $groupName",
                message = "$email started a live video call! Tap to join the call now.",
                isRead = false,
                timestamp = System.currentTimeMillis()
            )
        )
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    // End Group Video Call
    suspend fun endGroupCall(groupId: String): Result<Unit> = try {
        db.collection("active_calls").document(groupId).set(
            mapOf("active" to false, "timestamp" to System.currentTimeMillis())
        ).await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    // Real-Time Stories Flow
    fun getRealtimeStories(): Flow<List<UserStory>> = callbackFlow {
        val listener = db.collection("stories")
            .addSnapshotListener { snap, err ->
                if (err != null || snap == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val stories = snap.documents.mapNotNull { doc ->
                    doc.toObject(UserStory::class.java)?.copy(id = doc.id)
                }.sortedByDescending { it.timestamp }
                trySend(stories)
            }
        awaitClose { listener.remove() }
    }

    // Post Story to Cloudinary / Firestore
    suspend fun postStory(imageUrl: String): Result<Unit> = try {
        val email = auth.currentUser?.email ?: "Student"
        var name = email.substringBefore("@")
        var avatar = ""

        if (uid.isNotBlank()) {
            try {
                val uDoc = db.collection("users").document(uid).get().await()
                val n = uDoc.getString("name")
                val a = uDoc.getString("avatarUrl")
                if (!n.isNullOrBlank()) name = n
                if (!a.isNullOrBlank()) avatar = a
            } catch (_: Exception) {}
        }

        val ref = db.collection("stories").document()
        val story = UserStory(
            id = ref.id,
            authorUid = uid,
            authorName = name,
            authorAvatar = avatar,
            imageUrl = imageUrl,
            timestamp = System.currentTimeMillis()
        )
        ref.set(story).await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    // Send Message
    suspend fun sendMessage(chatId: String, text: String): Result<Unit> = try {
        val email = auth.currentUser?.email ?: "Student"
        var name = email.substringBefore("@")
        var avatar = ""

        if (uid.isNotBlank()) {
            try {
                val uDoc = db.collection("users").document(uid).get().await()
                val n = uDoc.getString("name")
                val a = uDoc.getString("avatarUrl")
                if (!n.isNullOrBlank()) name = n
                if (!a.isNullOrBlank()) avatar = a
            } catch (_: Exception) {}
        }

        val ref = db.collection("chats").document(chatId).collection("messages").document()
        val msg = ChatMessageItem(
            id = ref.id,
            chatId = chatId,
            senderId = uid,
            senderName = name,
            senderAvatar = avatar,
            text = text,
            timestamp = System.currentTimeMillis()
        )
        ref.set(msg).await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    // Create Group Chat
    suspend fun createGroup(name: String, desc: String, course: String, country: String, emoji: String): Result<String> = try {
        val ref = db.collection("chat_groups").document()
        val group = ChatGroup(
            id = ref.id,
            name = name,
            type = "group",
            description = desc,
            course = course,
            country = country,
            members = listOf(uid),
            iconEmoji = emoji
        )
        ref.set(group).await()
        Result.success(ref.id)
    } catch (e: Exception) { Result.failure(e) }

    // Fetch Groups
    suspend fun getGroups(): Result<List<ChatGroup>> = try {
        val snap = db.collection("chat_groups").get().await()
        val list = snap.documents.mapNotNull { it.toObject(ChatGroup::class.java)?.copy(id = it.id) }
        Result.success(defaultGroups + list)
    } catch (_: Exception) { Result.success(defaultGroups) }
}
