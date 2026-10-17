package com.example.finalDueDateCalendarApp.data.repository

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.finalDueDateCalendarApp.data.local.UserDao
import com.example.finalDueDateCalendarApp.data.local.UserEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthRepository(
    context: Context,
    private val userDao: UserDao
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "secure_session",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val _currentUserId = MutableStateFlow(prefs.getInt("current_user_id", -1).takeIf { it != -1 })
    val currentUserId: StateFlow<Int?> = _currentUserId.asStateFlow()

    suspend fun register(name: String, email: String, password: String): Result<Int> {
        val existingUser = userDao.getUserByEmail(email)
        if (existingUser != null) {
            return Result.failure(IllegalArgumentException("Email already registered"))
        }

        val newUserId = userDao.insertUser(
            UserEntity(name = name, email = email, password = password)
        ).toInt()
        saveUserSession(newUserId)
        return Result.success(newUserId)
    }

    suspend fun login(email: String, password: String): Result<Int> {
        val user = userDao.login(email, password)
            ?: return Result.failure(IllegalArgumentException("Invalid email or password"))

        saveUserSession(user.id)
        return Result.success(user.id)
    }

    fun logout() {
        prefs.edit().remove("current_user_id").apply()
        _currentUserId.value = null
    }

    private fun saveUserSession(userId: Int) {
        prefs.edit().putInt("current_user_id", userId).apply()
        _currentUserId.value = userId
    }
}
