package com.globalcampus.support.data.repository

import com.globalcampus.support.data.model.Comment
import com.globalcampus.support.data.model.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class PostRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val uid get() = auth.currentUser?.uid ?: ""

    // ---------- Create ----------
    suspend fun createPost(
        type: String,
        category: String,
        title: String,
        body: String,
        imageUrl: String = "",
        urgent: Boolean = false
    ): Result<String> = try {
        val email = auth.currentUser?.email ?: "Student"
        var authorName = email.substringBefore("@")
        if (uid.isNotBlank()) {
            try {
                val userDoc = db.collection("users").document(uid).get().await()
                val name = userDoc.getString("name")
                if (!name.isNullOrBlank()) authorName = name
            } catch (_: Exception) {}
        }

        val ref = db.collection("posts").document()
        val post = Post(
            id = ref.id,
            authorId = uid,
            authorName = authorName,
            type = type,
            category = category,
            title = title,
            body = body,
            imageUrl = imageUrl,
            urgent = urgent,
            createdAt = System.currentTimeMillis()
        )
        ref.set(post).await()
        Result.success(ref.id)
    } catch (e: Exception) { Result.failure(e) }

    // ---------- Read (In-Memory Sort prevents Firestore Composite Index Requirement) ----------
    suspend fun getPosts(type: String): Result<List<Post>> = try {
        val snap = db.collection("posts")
            .whereEqualTo("type", type)
            .get().await()

        val posts = snap.documents.mapNotNull { doc ->
            doc.toObject(Post::class.java)?.copy(id = doc.id)
        }.sortedByDescending { it.createdAt }

        Result.success(posts)
    } catch (e: Exception) { Result.failure(e) }

    suspend fun getPost(id: String): Result<Post> = try {
        val doc = db.collection("posts").document(id).get().await()
        val post = doc.toObject(Post::class.java)?.copy(id = doc.id)
            ?: throw Exception("Post not found")
        Result.success(post)
    } catch (e: Exception) { Result.failure(e) }

    // ---------- Likes ----------
    suspend fun toggleLike(postId: String, currentlyLiked: Boolean): Result<Unit> = try {
        val ref = db.collection("posts").document(postId)
        if (currentlyLiked) {
            ref.update(
                "likes", FieldValue.increment(-1),
                "likedBy", FieldValue.arrayRemove(uid)
            ).await()
        } else {
            ref.update(
                "likes", FieldValue.increment(1),
                "likedBy", FieldValue.arrayUnion(uid)
            ).await()
        }
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    // ---------- Reshare ----------
    suspend fun resharePost(postId: String): Result<Unit> = try {
        val ref = db.collection("posts").document(postId)
        ref.update(
            "shares", FieldValue.increment(1),
            "sharedBy", FieldValue.arrayUnion(uid)
        ).await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    // ---------- Delete own ----------
    suspend fun deletePost(postId: String): Result<Unit> = try {
        val doc = db.collection("posts").document(postId).get().await()
        if (doc.getString("authorId") != uid) throw Exception("Not your post")
        db.collection("posts").document(postId).delete().await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    // ---------- Mark resolved (help) ----------
    suspend fun markResolved(postId: String, resolved: Boolean): Result<Unit> = try {
        db.collection("posts").document(postId)
            .update("resolved", resolved).await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    // ---------- Comments ----------
    suspend fun getComments(postId: String): Result<List<Comment>> = try {
        val snap = db.collection("posts").document(postId)
            .collection("comments")
            .get().await()

        val comments = snap.documents.mapNotNull { doc ->
            doc.toObject(Comment::class.java)?.copy(id = doc.id)
        }.sortedBy { it.createdAt }

        Result.success(comments)
    } catch (e: Exception) { Result.failure(e) }

    suspend fun addComment(postId: String, text: String): Result<Unit> = try {
        val email = auth.currentUser?.email ?: "Student"
        var authorName = email.substringBefore("@")
        if (uid.isNotBlank()) {
            try {
                val userDoc = db.collection("users").document(uid).get().await()
                val name = userDoc.getString("name")
                if (!name.isNullOrBlank()) authorName = name
            } catch (_: Exception) {}
        }

        val commentRef = db.collection("posts").document(postId)
            .collection("comments").document()
        commentRef.set(
            Comment(
                id = commentRef.id,
                authorId = uid,
                authorName = authorName,
                text = text
            )
        ).await()
        db.collection("posts").document(postId)
            .update("commentCount", FieldValue.increment(1)).await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    // ---------- Report ----------
    suspend fun reportPost(postId: String, reason: String): Result<Unit> = try {
        db.collection("reports").document().set(
            mapOf(
                "postId" to postId,
                "reportedBy" to uid,
                "reason" to reason,
                "createdAt" to System.currentTimeMillis()
            )
        ).await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }
}
