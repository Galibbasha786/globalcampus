package com.globalcampus.support.data.model

data class ChatMessageItem(
    val id: String = "",
    val chatId: String = "global",
    val senderId: String = "",
    val senderName: String = "",
    val senderAvatar: String = "🎓",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class ChatGroup(
    val id: String = "",
    val name: String = "",
    val type: String = "group", // global | group | direct
    val description: String = "",
    val course: String = "",
    val country: String = "",
    val members: List<String> = emptyList(),
    val iconEmoji: String = "💬",
    val createdAt: Long = System.currentTimeMillis()
)
