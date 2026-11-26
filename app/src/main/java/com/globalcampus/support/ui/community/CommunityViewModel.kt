package com.globalcampus.support.ui.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.globalcampus.support.data.model.Post
import com.globalcampus.support.data.repository.AuthRepository
import com.globalcampus.support.data.repository.PostRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class CommunityState(
    val loading: Boolean = true,
    val posts: List<Post> = emptyList(),
    val selectedCategory: String = "All",
    val creating: Boolean = false,
    val message: String? = null,
    val error: String? = null
) {
    val categories = listOf("All", "Events", "Housing", "Study", "Social", "General")
    val filtered: List<Post>
        get() = posts.filter { selectedCategory == "All" || it.category == selectedCategory }
}

class CommunityViewModel : ViewModel() {

    private val repo = PostRepository()
    private val auth = AuthRepository()
    private val _state = MutableStateFlow(CommunityState())
    val state: StateFlow<CommunityState> = _state

    val myUid: String get() = auth.currentUser?.uid ?: ""

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            val res = repo.getPosts("community")
            _state.value = _state.value.copy(
                loading = false,
                posts = res.getOrDefault(emptyList()),
                error = res.exceptionOrNull()?.message
            )
        }
    }

    fun setCategory(c: String) {
        _state.value = _state.value.copy(selectedCategory = c)
    }

    fun createPost(category: String, title: String, body: String, imageUrl: String = "") {
        if (title.isBlank() || body.isBlank()) {
            _state.value = _state.value.copy(error = "Title and body required")
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(creating = true, error = null)
            val res = repo.createPost("community", category, title, body, imageUrl = imageUrl, urgent = false)
            _state.value = _state.value.copy(
                creating = false,
                message = if (res.isSuccess) "Posted successfully! ✅" else "Failed: ${res.exceptionOrNull()?.message}"
            )
            load()
        }
    }

    fun toggleLike(post: Post) {
        val liked = post.likedBy.contains(myUid)
        viewModelScope.launch {
            repo.toggleLike(post.id, liked)
            load()
        }
    }

    fun resharePost(post: Post) {
        viewModelScope.launch {
            repo.resharePost(post.id)
            _state.value = _state.value.copy(message = "Post reshared! 🚀")
            load()
        }
    }

    fun deletePost(id: String) {
        viewModelScope.launch {
            repo.deletePost(id)
            load()
        }
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null, error = null)
    }
}
