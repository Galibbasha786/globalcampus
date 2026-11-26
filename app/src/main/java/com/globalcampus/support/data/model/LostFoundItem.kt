package com.globalcampus.support.data.model

data class LostFoundItem(
    val id: String = "",
    val authorId: String = "",
    val title: String = "",
    val type: String = "Lost", // Lost | Found
    val category: String = "Electronics",
    val location: String = "",
    val description: String = "",
    val contact: String = "",
    val imageUrl: String = "",
    val isResolved: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
