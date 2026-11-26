package com.globalcampus.support.data.model

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val regdNo: String = "",
    val country: String = "",
    val course: String = "",
    val avatarUrl: String = "",
    val petName: String = "",
    val favoriteTeacher: String = "",
    val birthCity: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
