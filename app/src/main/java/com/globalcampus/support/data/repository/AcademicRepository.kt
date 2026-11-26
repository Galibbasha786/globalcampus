package com.globalcampus.support.data.repository

import com.globalcampus.support.data.model.AcademicTask
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AcademicRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val uid get() = auth.currentUser?.uid ?: ""

    private val sampleTasks = listOf(
        AcademicTask("a1", "CSE320", "CA 1 Assignment Submission", "20 Nov 2025", true),
        AcademicTask("a2", "INT219", "Front-end Web Project Presentation", "25 Nov 2025", false),
        AcademicTask("a3", "MTH101", "Mid-Term Practice Paper Solutions", "28 Nov 2025", false)
    )

    suspend fun getTasks(): Result<List<AcademicTask>> {
        return try {
            if (uid.isBlank()) return Result.success(sampleTasks)
            val snap = db.collection("users").document(uid).collection("academic_tasks").get().await()
            if (snap.isEmpty) {
                sampleTasks.forEach { item ->
                    db.collection("users").document(uid).collection("academic_tasks").document(item.id).set(item).await()
                }
                Result.success(sampleTasks)
            } else {
                val list = snap.documents.mapNotNull { it.toObject(AcademicTask::class.java)?.copy(id = it.id) }
                Result.success(list)
            }
        } catch (_: Exception) { Result.success(sampleTasks) }
    }

    suspend fun toggleTask(id: String, completed: Boolean): Result<Unit> = try {
        if (uid.isNotBlank()) {
            db.collection("users").document(uid).collection("academic_tasks").document(id)
                .update("completed", completed).await()
        }
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    suspend fun addTask(subject: String, title: String, deadline: String): Result<Unit> = try {
        val ref = db.collection("users").document(uid).collection("academic_tasks").document()
        val item = AcademicTask(id = ref.id, subject = subject, title = title, deadline = deadline)
        ref.set(item).await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }
}
