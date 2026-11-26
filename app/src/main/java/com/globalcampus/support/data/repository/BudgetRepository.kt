package com.globalcampus.support.data.repository

import com.globalcampus.support.data.model.ExpenseItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class BudgetRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val uid get() = auth.currentUser?.uid ?: ""

    private val defaultExpenses = listOf(
        ExpenseItem("e1", "Mess Fees & Groceries", 4500.0, "Food"),
        ExpenseItem("e2", "Hostel Electricity", 800.0, "Housing"),
        ExpenseItem("e3", "Local E-Rickshaw Fare", 350.0, "Transport"),
        ExpenseItem("e4", "Study Books & Stationery", 600.0, "Supplies")
    )

    suspend fun getExpenses(): Result<List<ExpenseItem>> {
        return try {
            if (uid.isBlank()) return Result.success(defaultExpenses)
            val snap = db.collection("users").document(uid).collection("expenses").get().await()
            if (snap.isEmpty) {
                defaultExpenses.forEach { item ->
                    db.collection("users").document(uid).collection("expenses").document(item.id).set(item).await()
                }
                Result.success(defaultExpenses)
            } else {
                val list = snap.documents.mapNotNull { it.toObject(ExpenseItem::class.java)?.copy(id = it.id) }
                Result.success(list)
            }
        } catch (_: Exception) { Result.success(defaultExpenses) }
    }

    suspend fun addExpense(title: String, amount: Double, category: String): Result<Unit> = try {
        val ref = db.collection("users").document(uid).collection("expenses").document()
        val item = ExpenseItem(id = ref.id, title = title, amount = amount, category = category)
        ref.set(item).await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }
}
