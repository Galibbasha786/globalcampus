package com.globalcampus.support.data.model

data class Article(
    val id: String = "",
    val title: String = "",
    val category: String = "",
    val body: String = "",
    val tags: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)