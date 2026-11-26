package com.globalcampus.support.data.repository

import com.globalcampus.support.data.model.Article
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class ArticleRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun getArticles(): Result<List<Article>> = try {
        val snap = db.collection("articles")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get().await()
        Result.success(snap.documents.mapNotNull { it.toObject(Article::class.java)?.copy(id = it.id) })
    } catch (e: Exception) { Result.failure(e) }

    suspend fun getArticle(id: String): Result<Article> = try {
        val doc = db.collection("articles").document(id).get().await()
        val article = doc.toObject(Article::class.java)?.copy(id = doc.id)
            ?: throw Exception("Article not found")
        Result.success(article)
    } catch (e: Exception) { Result.failure(e) }

    suspend fun getBookmarkedIds(): Result<Set<String>> = try {
        val uid = auth.currentUser?.uid ?: throw Exception("Not logged in")
        val snap = db.collection("bookmarks").document(uid).collection("items").get().await()
        Result.success(snap.documents.map { it.id }.toSet())
    } catch (e: Exception) { Result.failure(e) }

    suspend fun toggleBookmark(articleId: String, bookmarked: Boolean): Result<Unit> = try {
        val uid = auth.currentUser?.uid ?: throw Exception("Not logged in")
        val ref = db.collection("bookmarks").document(uid)
            .collection("items").document(articleId)
        if (bookmarked) {
            ref.set(mapOf("savedAt" to System.currentTimeMillis())).await()
        } else {
            ref.delete().await()
        }
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    suspend fun seedArticles(): Result<Int> {
        return try {
            val existing = db.collection("articles").limit(1).get().await()
            if (!existing.isEmpty) {
                return Result.success(0)
            }

            val samples = listOf(
                Article(
                    title = "How to Get Your Student ID Card",
                    category = "Campus",
                    body = "Visit the Student Services Office in Building A, Room 102. Bring your passport, admission letter, and one passport-size photo. Processing takes 2 working days. Your ID gives you library access, gym entry, and cafeteria discounts.",
                    tags = listOf("id", "campus", "student services")
                ),
                Article(
                    title = "Visa Extension: Step-by-Step Guide",
                    category = "Visa",
                    body = "Start 30 days before expiry. 1) Download the extension form from the immigration website. 2) Get a enrollment letter from your international office. 3) Book an appointment. 4) Bring passport, current visa, enrollment letter, bank statement, and photos. Fee: approx $50. Processing: 2–4 weeks.",
                    tags = listOf("visa", "immigration", "documents")
                ),
                Article(
                    title = "Opening a Local Bank Account",
                    category = "Money",
                    body = "You need: passport, student ID, enrollment letter, and proof of address (dorm letter works). Most banks offer free student accounts. Recommended: Bank A (no monthly fee), Bank B (best app). Bring $20 minimum deposit for most accounts.",
                    tags = listOf("bank", "money", "finance")
                ),
                Article(
                    title = "Finding Housing Near Campus",
                    category = "Housing",
                    body = "Options: 1) University dorms — apply early via the housing portal. 2) Private apartments — check local Facebook groups and campus notice boards. 3) Homestay — through the international office. Average rent: $300–600/month. Always visit before signing and never pay a deposit without a contract.",
                    tags = listOf("housing", "rent", "apartment")
                ),
                Article(
                    title = "Health Insurance for International Students",
                    category = "Health",
                    body = "Most universities require health insurance. The campus health center offers a student plan (~$200/semester). It covers GP visits, emergency care, and basic prescriptions. Register at the health center in your first week. Keep your insurance card with your student ID.",
                    tags = listOf("health", "insurance", "medical")
                ),
                Article(
                    title = "Getting a SIM Card and Mobile Plan",
                    category = "Practical",
                    body = "Bring your passport to any carrier store. Prepaid SIMs cost $5–10. Recommended plans: Carrier A ($15/month, 10GB), Carrier B ($20/month, unlimited). You need a local address for registration. Campus WiFi covers most buildings, so a small plan is often enough.",
                    tags = listOf("sim", "phone", "mobile")
                ),
                Article(
                    title = "Using Public Transport on a Student Discount",
                    category = "Practical",
                    body = "Apply for a student transit card at the main station with your student ID. Discounts are 30–50%. Monthly pass: ~$40. The campus shuttle is free with your student ID. Download the local transit app for schedules and real-time updates.",
                    tags = listOf("transport", "bus", "discount")
                ),
                Article(
                    title = "Where to Find Part-Time Work",
                    category = "Money",
                    body = "International students can usually work 20 hrs/week during term. Check: 1) Campus job board (library, cafeteria, admin). 2) International office — they list approved employers. 3) Local cafes and retail. You need a work permit stamp in your passport — apply at the immigration office. Never work without permission.",
                    tags = listOf("work", "job", "part-time")
                ),
                Article(
                    title = "Registering with a Local Doctor (GP)",
                    category = "Health",
                    body = "Register with a GP near your dorm in your first month. Bring passport, student ID, and insurance card. Walk-in clinics are available for urgent issues. For emergencies, call the local emergency number (usually 112 or 911). The campus health center can refer you to specialists.",
                    tags = listOf("health", "doctor", "gp")
                ),
                Article(
                    title = "Academic Writing Support on Campus",
                    category = "Study",
                    body = "The Writing Center (Library, 2nd floor) offers free 1-on-1 sessions. Book online. They help with essays, citations, and proofreading. Also check the library's citation guides and the international office's academic orientation workshops.",
                    tags = listOf("study", "writing", "academic")
                ),
                Article(
                    title = "Understanding Your Tuition and Fees",
                    category = "Money",
                    body = "Your tuition invoice is in the student portal. Payment deadlines are strict. International payment options: bank transfer, Flywire, or card. Late fees apply. Contact the finance office if you have payment issues — they offer installment plans. Keep all receipts.",
                    tags = listOf("tuition", "fees", "payment")
                ),
                Article(
                    title = "Joining Student Clubs and Societies",
                    category = "Community",
                    body = "The Students' Union hosts a clubs fair in Week 2. Popular international clubs: cultural societies, sports teams, debate, and volunteering. Membership is usually $5–20/year. It's the fastest way to make friends and build a support network.",
                    tags = listOf("clubs", "social", "community")
                ),
                Article(
                    title = "Emergency Contacts Every Student Should Save",
                    category = "Health",
                    body = "Save these now: Campus security (24/7), International Office, Campus Health Center, Local emergency (112/911), Your embassy, and a trusted friend. Also save the mental health helpline — most campuses offer free counseling.",
                    tags = listOf("emergency", "safety", "contacts")
                ),
                Article(
                    title = "Renewing Your Passport While Abroad",
                    category = "Visa",
                    body = "Start 6–9 months before expiry. Contact your country's embassy or consulate. You'll need: old passport, photos, application form, and fee. Processing can take 4–8 weeks. Do not travel with an expired passport. Keep a photocopy of your passport in a safe place.",
                    tags = listOf("passport", "embassy", "renewal")
                ),
                Article(
                    title = "Budgeting for International Students",
                    category = "Money",
                    body = "Typical monthly budget: rent $400, food $200, transport $50, phone $20, misc $100 = ~$770. Use a budgeting app. Cook at home to save. Student discounts exist everywhere — always ask. Keep an emergency fund of at least $500.",
                    tags = listOf("budget", "money", "finance")
                )
            )

            val batch = db.batch()
            samples.forEach { article ->
                val ref = db.collection("articles").document()
                batch.set(ref, article)
            }
            batch.commit().await()
            Result.success(samples.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}