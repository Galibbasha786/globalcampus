package com.globalcampus.support.data.model

data class Comment(
    val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val text: String = "",
    val createdAt: Long = System.currentTimeMillis()
)