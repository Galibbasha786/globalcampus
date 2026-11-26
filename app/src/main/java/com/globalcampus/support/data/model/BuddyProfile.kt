package com.globalcampus.support.data.model

data class BuddyProfile(
    val uid: String = "",
    val name: String = "",
    val regdNo: String = "",
    val country: String = "",
    val course: String = "",
    val languages: List<String> = emptyList(),
    val interests: List<String> = emptyList(),
    val avatarUrl: String = "🎓",
    val bio: String = ""
)

data class BuddyRequest(
    val id: String = "",
    val senderUid: String = "",
    val receiverUid: String = "",
    val status: String = "pending", // pending | accepted | rejected
    val createdAt: Long = System.currentTimeMillis()
)
