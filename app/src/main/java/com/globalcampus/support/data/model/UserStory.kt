package com.globalcampus.support.data.model

data class UserStory(
    val id: String = "",
    val authorUid: String = "",
    val authorName: String = "",
    val authorAvatar: String = "",
    val imageUrl: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
