package com.globalcampus.support.data.model

data class CallHistoryItem(
    val id: String = "",
    val recipientName: String = "",
    val isGroupCall: Boolean = true,
    val durationSeconds: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "Completed"
)
