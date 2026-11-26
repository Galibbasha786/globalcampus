package com.globalcampus.support.data.model

data class Post(
    val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val type: String = "community",   // "community" | "help"
    val category: String = "General",
    val title: String = "",
    val body: String = "",
    val imageUrl: String = "",
    val urgent: Boolean = false,
    val resolved: Boolean = false,
    val likes: Int = 0,
    val likedBy: List<String> = emptyList(),
    val shares: Int = 0,
    val sharedBy: List<String> = emptyList(),
    val commentCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
