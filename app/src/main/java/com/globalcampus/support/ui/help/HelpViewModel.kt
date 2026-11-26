package com.globalcampus.support.ui.help

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.globalcampus.support.data.model.AppNotification
import com.globalcampus.support.data.model.Post
import com.globalcampus.support.data.repository.AuthRepository
import com.globalcampus.support.data.repository.NotificationRepository
import com.globalcampus.support.data.repository.PostRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class HelpState(
    val loading: Boolean = true,
    val posts: List<Post> = emptyList(),
    val filter: String = "All",   // All | Urgent | Resolved | Open
    val creating: Boolean = false,
    val message: String? = null
) {
    val filtered: List<Post>
        get() = when (filter) {
            "Urgent" -> posts.filter { it.urgent && !it.resolved }
            "Resolved" -> posts.filter { it.resolved }
            "Open" -> posts.filter { !it.resolved }
            else -> posts
        }
}

class HelpViewModel : ViewModel() {
    private val repo = PostRepository()
    private val notifRepo = NotificationRepository()
    private val auth = AuthRepository()
    private val _state = MutableStateFlow(HelpState())
    val state: StateFlow<HelpState> = _state

    val myUid: String get() = auth.currentUser?.uid ?: ""

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true)
            val res = repo.getPosts("help")
            _state.value = _state.value.copy(
                loading = false,
                posts = res.getOrDefault(emptyList())
            )
        }
    }

    fun setFilter(f: String) {
        _state.value = _state.value.copy(filter = f)
    }

    fun createHelp(category: String, title: String, body: String, urgent: Boolean) {
        if (title.isBlank() || body.isBlank()) return
        viewModelScope.launch {
            _state.value = _state.value.copy(creating = true)
            val res = repo.createPost("help", category, title, body, imageUrl = "", urgent = urgent)
            if (res.isSuccess) {
                notifRepo.sendNotification(
                    AppNotification(
                        userId = "all",
                        title = if (urgent) "🚨 URGENT Help Request!" else "🆘 New Help Request ($category)",
                        message = "$title — Tap to view & assist in Help Center."
                    )
                )
            }
            _state.value = _state.value.copy(
                creating = false,
                message = if (res.isSuccess) "Help request posted successfully!"
                else "Failed: ${res.exceptionOrNull()?.message}"
            )
            load()
        }
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null)
    }
}
