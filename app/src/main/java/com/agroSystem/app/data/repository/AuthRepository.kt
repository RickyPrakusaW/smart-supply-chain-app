package com.agroSystem.app.data.repository

import android.util.Log
import com.agroSystem.app.data.local.dao.UserDao
import com.agroSystem.app.data.local.entities.toEntity
import com.agroSystem.app.data.models.User
import com.agroSystem.app.data.remote.AuthApiService
import com.agroSystem.app.data.remote.GoogleLoginRequest
import com.agroSystem.app.data.remote.PhoneLoginRequest
import com.agroSystem.app.data.remote.UpdateProfileRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

class AuthRepository(
    private val userDao: UserDao,
    private val authApiService: AuthApiService
) {
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    suspend fun loadSession(): User? = withContext(Dispatchers.IO) {
        val entity = userDao.getLoggedInUser()
        val user = entity?.toDomain()
        _currentUser.value = user
        return@withContext user
    }

    suspend fun loginWithGoogle(idToken: String, name: String, email: String): User = withContext(Dispatchers.IO) {
        var user: User? = null
        try {
            val response = authApiService.loginWithGoogle(GoogleLoginRequest(idToken, name, email))
            if (response.success) {
                user = User(
                    id = response.id,
                    name = response.name,
                    email = response.email,
                    phone = response.phone,
                    role = response.role,
                    token = response.token,
                    photoUrl = response.photoUrl
                )
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Remote Google Login failed, using local fallback", e)
        }

        if (user == null) {
            user = User(
                id = "google_" + email.hashCode(),
                name = name,
                email = email,
                phone = null,
                role = "Pembeli", // Default role
                token = "jwt_mock_token_for_" + idToken.take(10),
                photoUrl = null
            )
        }

        userDao.clearUser()
        userDao.insertUser(user.toEntity())
        _currentUser.value = user
        return@withContext user
    }

    suspend fun loginWithPhone(phone: String, name: String): User = withContext(Dispatchers.IO) {
        var user: User? = null
        try {
            val response = authApiService.loginWithPhone(PhoneLoginRequest(phone, name))
            if (response.success) {
                user = User(
                    id = response.id,
                    name = response.name,
                    email = response.email,
                    phone = response.phone,
                    role = response.role,
                    token = response.token,
                    photoUrl = response.photoUrl
                )
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Remote Phone Login failed, using local fallback", e)
        }

        if (user == null) {
            user = User(
                id = "phone_" + phone.hashCode(),
                name = name.ifEmpty { "User Telpon" },
                email = null,
                phone = phone,
                role = "Pembeli",
                token = "jwt_mock_token_for_" + phone,
                photoUrl = null
            )
        }

        userDao.clearUser()
        userDao.insertUser(user.toEntity())
        _currentUser.value = user
        return@withContext user
    }

    suspend fun updateProfile(name: String, role: String): User? = withContext(Dispatchers.IO) {
        val current = _currentUser.value ?: return@withContext null
        var updated: User? = null
        try {
            val response = authApiService.updateProfile(UpdateProfileRequest(current.id, name, role))
            if (response.success) {
                updated = User(
                    id = response.id,
                    name = response.name,
                    email = response.email,
                    phone = response.phone,
                    role = response.role,
                    token = response.token,
                    photoUrl = response.photoUrl
                )
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Remote Profile Update failed, using local fallback", e)
        }

        if (updated == null) {
            updated = current.copy(name = name, role = role)
        }

        userDao.insertUser(updated.toEntity())
        _currentUser.value = updated
        return@withContext updated
    }

    suspend fun logout() = withContext(Dispatchers.IO) {
        userDao.clearUser()
        _currentUser.value = null
    }
}
