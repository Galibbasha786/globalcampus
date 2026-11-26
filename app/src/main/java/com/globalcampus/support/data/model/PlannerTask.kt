package com.globalcampus.support.data.model

data class PlannerTask(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val stage: String = "First Day", // "First Day" | "First Week" | "First Month"
    val completed: Boolean = false,
    val resourceLink: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
