package com.globalcampus.support.data.repository

import com.globalcampus.support.data.model.PlannerTask
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class PlannerRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val uid get() = auth.currentUser?.uid ?: ""

    private val defaultTasks = listOf(
        // LPU Onboarding Certificates Checklist
        PlannerTask("c1", "📄 Passport & Valid Student Visa", "Original passport with minimum 6 months validity & Student Visa.", "Required Certificates", true),
        PlannerTask("c2", "📜 10th & 12th / Bachelor's Marksheets", "Original certificates & marksheets verified by Ministry of Education.", "Required Certificates", true),
        PlannerTask("c3", "✉️ LPU Admission & Offer Letter", "Provisional Admission Letter and Official Offer Letter from LPU.", "Required Certificates", true),
        PlannerTask("c4", "🏥 Medical Fitness Certificate", "Medical checkup report & Yellow Fever Vaccination card if coming from endemic regions.", "Required Certificates", false),
        PlannerTask("c5", "📸 Passport Size Photographs", "12 recent passport-size photographs with plain white background.", "Required Certificates", false),
        PlannerTask("c6", "💳 Fee Payment Receipts", "Official LPU fee payment transaction receipts or bank transfer confirmation.", "Required Certificates", false),
        PlannerTask("c7", "🏠 C-Form & Hostel Allotment Slip", "C-Form for FRRO registration issued by Hostel Warden in BH/GH block.", "Required Certificates", false),

        // First Day
        PlannerTask("t1", "Check into LPU Hostel / Housing", "Report to Hostel Warden Office in BH/GH block for room key & inventory sheet.", "First Day", true),
        PlannerTask("t2", "Get Indian SIM Card", "Visit Airtel/Jio kiosk at Uni-Mall with passport, visa & passport photo.", "First Day", true),
        PlannerTask("t3", "Get C-Form Copy", "Collect your official C-Form for FRRO registration from Hostel Warden.", "First Day", false),

        // First Week
        PlannerTask("t4", "Visit DIA Office (Block 32)", "Report to Division of International Affairs for document verification & orientation.", "First Week", false),
        PlannerTask("t5", "Open Indian Bank Account", "Visit HDFC / SBI Bank branch at Uni-Mall with passport, admission letter & photos.", "First Week", false),
        PlannerTask("t6", "Submit FRRO Registration Online", "Complete FRRO online registration at indianfrro.gov.in within 14 days.", "First Week", false),

        // First Month
        PlannerTask("t7", "Biometric Medical Checkup", "Complete health screening at Uni-Hospital (Block 27).", "First Month", false),
        PlannerTask("t8", "Explore Jalandhar & Phagwara", "Visit local markets for essentials and try North Indian cuisine.", "First Month", false)
    )

    suspend fun getTasks(): Result<List<PlannerTask>> {
        return try {
            if (uid.isBlank()) return Result.success(defaultTasks)
            val snap = db.collection("users").document(uid).collection("planner_tasks").get().await()
            if (snap.isEmpty) {
                defaultTasks.forEach { task ->
                    db.collection("users").document(uid).collection("planner_tasks").document(task.id).set(task).await()
                }
                Result.success(defaultTasks)
            } else {
                val list = snap.documents.mapNotNull { it.toObject(PlannerTask::class.java)?.copy(id = it.id) }
                Result.success(list)
            }
        } catch (_: Exception) { Result.success(defaultTasks) }
    }

    suspend fun toggleTask(taskId: String, completed: Boolean): Result<Unit> = try {
        if (uid.isNotBlank()) {
            db.collection("users").document(uid).collection("planner_tasks").document(taskId)
                .update("completed", completed).await()
        }
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    suspend fun addTask(title: String, description: String, stage: String): Result<Unit> = try {
        val ref = db.collection("users").document(uid).collection("planner_tasks").document()
        val task = PlannerTask(id = ref.id, title = title, description = description, stage = stage)
        ref.set(task).await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }
}
