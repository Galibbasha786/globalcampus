package com.globalcampus.support.data.model

data class ExpenseItem(
    val id: String = "",
    val title: String = "",
    val amount: Double = 0.0,
    val category: String = "Food", // Food | Housing | Transport | Supplies | Leisure
    val date: Long = System.currentTimeMillis()
)
