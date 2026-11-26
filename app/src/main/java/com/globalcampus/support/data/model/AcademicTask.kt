package com.globalcampus.support.data.model

data class AcademicTask(
    val id: String = "",
    val subject: String = "",
    val title: String = "",
    val deadline: String = "",
    val completed: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
