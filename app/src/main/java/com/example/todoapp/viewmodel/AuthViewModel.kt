package com.example.finalDueDateCalendarApp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.finalDueDateCalendarApp.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val errorMessage: String? = null,
    val isLoading: Boolean = false
)

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {
    val currentUserId = repository.currentUserId

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun updateName(value: String) = _uiState.update { it.copy(name = value) }
    fun updateEmail(value: String) = _uiState.update { it.copy(email = value) }
    fun updatePassword(value: String) = _uiState.update { it.copy(password = value) }
    fun updateConfirmPassword(value: String) = _uiState.update { it.copy(confirmPassword = value) }
    fun clearMessage() = _uiState.update { it.copy(errorMessage = null) }

    fun login(onSuccess: (Int) -> Unit) {
        val state = _uiState.value
        val validationMessage = validateLogin(state.email, state.password)
        if (validationMessage != null) {
            _uiState.update { it.copy(errorMessage = validationMessage) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            repository.login(state.email.trim(), state.password)
                .onSuccess { userId ->
                    _uiState.value = AuthUiState()
                    onSuccess(userId)
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
                }
        }
    }

    fun register(onSuccess: (Int) -> Unit) {
        val state = _uiState.value
        val validationMessage = validateRegistration(state)
        if (validationMessage != null) {
            _uiState.update { it.copy(errorMessage = validationMessage) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            repository.register(state.name.trim(), state.email.trim(), state.password)
                .onSuccess { userId ->
                    _uiState.value = AuthUiState()
                    onSuccess(userId)
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
                }
        }
    }

    fun logout() = repository.logout()

    private fun validateLogin(email: String, password: String): String? {
        return when {
            email.isBlank() || password.isBlank() -> "Please fill in all fields"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Enter a valid email"
            else -> null
        }
    }

    private fun validateRegistration(state: AuthUiState): String? {
        return when {
            state.name.isBlank() || state.email.isBlank() || state.password.isBlank() || state.confirmPassword.isBlank() -> "Please fill in all fields"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(state.email).matches() -> "Enter a valid email"
            state.password.length < 6 -> "Password must be at least 6 characters"
            state.password != state.confirmPassword -> "Passwords do not match"
            else -> null
        }
    }
}

class AuthViewModelFactory(private val repository: AuthRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AuthViewModel(repository) as T
    }
}
