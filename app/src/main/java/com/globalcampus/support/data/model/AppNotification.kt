package com.globalcampus.support.data.model

data class AppNotification(
    val id: String = "",
    val userId: String = "", // Who receives this
    val title: String = "",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)