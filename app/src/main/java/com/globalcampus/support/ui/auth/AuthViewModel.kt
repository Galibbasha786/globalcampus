package com.globalcampus.support.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.globalcampus.support.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AuthState(
    val loading: Boolean = false,
    val error: String? = null,
    val message: String? = null,
    val answersVerified: Boolean = false,
    val success: Boolean = false
)

class AuthViewModel : ViewModel() {

    private val repo = AuthRepository()
    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state

    fun signUp(
        email: String,
        password: String,
        name: String,
        regdNo: String,
        country: String,
        petName: String,
        favoriteTeacher: String,
        birthCity: String
    ) {
        if (email.isBlank() || password.length < 6 || name.isBlank() || regdNo.isBlank() || country.isBlank() || petName.isBlank()) {
            _state.value = AuthState(error = "Fill all fields & answers for security verification.")
            return
        }
        viewModelScope.launch {
            _state.value = AuthState(loading = true)
            val result = repo.signUp(
                email.trim(),
                password,
                name.trim(),
                regdNo.trim(),
                country.trim(),
                petName.trim(),
                favoriteTeacher.trim(),
                birthCity.trim()
            )
            _state.value = if (result.isSuccess)
                AuthState(success = true)
            else
                AuthState(error = result.exceptionOrNull()?.message ?: "Signup failed")
        }
    }

    fun signIn(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _state.value = AuthState(error = "Enter email and password")
            return
        }
        viewModelScope.launch {
            _state.value = AuthState(loading = true)
            val result = repo.signIn(email.trim(), password)
            _state.value = if (result.isSuccess)
                AuthState(success = true)
            else
                AuthState(error = result.exceptionOrNull()?.message ?: "Login failed")
        }
    }

    fun verifySecurityAnswers(
        email: String,
        petName: String,
        favoriteTeacher: String,
        birthCity: String
    ) {
        if (email.isBlank() || petName.isBlank() || favoriteTeacher.isBlank() || birthCity.isBlank()) {
            _state.value = _state.value.copy(error = "Fill email and all 3 security question answers.")
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            val res = repo.verifySecurityQuestionsOnly(email, petName, favoriteTeacher, birthCity)
            if (res.isSuccess) {
                _state.value = _state.value.copy(
                    loading = false,
                    answersVerified = true,
                    message = "Security answers verified! You can now set your new password below."
                )
            } else {
                _state.value = _state.value.copy(
                    loading = false,
                    error = res.exceptionOrNull()?.message ?: "Verification failed."
                )
            }
        }
    }

    fun changePassword(newPassword: String) {
        if (newPassword.length < 6) {
            _state.value = _state.value.copy(error = "Password must be at least 6 characters")
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            val res = repo.changePassword(newPassword)
            _state.value = if (res.isSuccess)
                _state.value.copy(loading = false, message = "Password changed successfully!")
            else
                _state.value.copy(loading = false, error = res.exceptionOrNull()?.message ?: "Password change failed.")
        }
    }

    fun resetPasswordAndSignIn(email: String, newPassword: String) {
        if (newPassword.length < 6) {
            _state.value = _state.value.copy(error = "Password must be at least 6 characters.")
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            val res = repo.resetPasswordDirectly(email, newPassword)
            _state.value = _state.value.copy(
                loading = false,
                message = "Password reset successfully!"
            )
            signIn(email, newPassword)
        }
    }

    fun deleteAccount(onDeleted: () -> Unit) {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true)
            val res = repo.deleteAccount()
            if (res.isSuccess) {
                _state.value = AuthState()
                onDeleted()
            } else {
                _state.value = _state.value.copy(loading = false, error = res.exceptionOrNull()?.message ?: "Failed to delete account.")
            }
        }
    }

    fun clearError() { _state.value = _state.value.copy(error = null, message = null) }
    fun resetSuccess() { _state.value = AuthState() }
}
