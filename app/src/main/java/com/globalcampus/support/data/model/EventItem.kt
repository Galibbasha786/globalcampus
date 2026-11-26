package com.globalcampus.support.data.model

data class EventItem(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "Cultural", // Cultural | Academic | Sports | Social
    val date: String = "",
    val time: String = "",
    val location: String = "",
    val organizer: String = "LPU DIA",
    val imageUrl: String = "",
    val rsvps: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)
