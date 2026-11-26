package com.globalcampus.support.ui.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.globalcampus.support.data.model.AppNotification
import com.globalcampus.support.data.repository.AuthRepository
import com.globalcampus.support.data.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NotificationsViewModel : ViewModel() {
    private val repo = NotificationRepository()
    private val auth = AuthRepository()
    private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val notifications: StateFlow<List<AppNotification>> = _notifications

    init { load() }

    fun load() {
        viewModelScope.launch {
            auth.currentUser?.uid?.let { uid ->
                repo.getMyNotifications(uid).onSuccess { _notifications.value = it }
            }
        }
    }

    fun deleteNotification(id: String) {
        viewModelScope.launch {
            _notifications.value = _notifications.value.filter { it.id != id }
            repo.deleteNotification(id)
        }
    }

    fun deleteAllNotifications() {
        viewModelScope.launch {
            val uid = auth.currentUser?.uid ?: ""
            _notifications.value = emptyList()
            if (uid.isNotBlank()) repo.deleteAllNotifications(uid)
        }
    }

    fun acceptBuddyRequest(notif: AppNotification) {
        viewModelScope.launch {
            deleteNotification(notif.id)
            repo.sendNotification(
                AppNotification(
                    userId = "all",
                    title = "Buddy Request Accepted!",
                    message = "Buddy connection established! You can now start chatting live."
                )
            )
        }
    }
}
