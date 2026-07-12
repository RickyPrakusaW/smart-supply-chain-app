package com.agroSystem.app.features.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.agroSystem.app.data.local.AppDatabase
import com.agroSystem.app.data.models.User
import com.agroSystem.app.data.repository.AuthRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val authRepository: AuthRepository

    init {
        val database = AppDatabase.getDatabase(application)
        authRepository = AuthRepository(database.userDao(), application)
        
        viewModelScope.launch {
            authRepository.loadSession()
        }
    }

    val currentUser: StateFlow<User?> = authRepository.currentUser

    private val _phone = MutableLiveData<String>("")
    val phone: LiveData<String> = _phone

    private val _tempName = MutableLiveData<String>("")
    val tempName: LiveData<String> = _tempName

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    fun appendPhoneDigit(char: String) {
        val current = _phone.value ?: ""
        if (current.length < 12) {
            _phone.value = current + char
        }
    }

    fun deletePhoneDigit() {
        val current = _phone.value ?: ""
        if (current.isNotEmpty()) {
            _phone.value = current.dropLast(1)
        }
    }

    fun setPhone(value: String) {
        _phone.value = value
    }

    fun setTempName(name: String) {
        _tempName.value = name
    }

    fun loginWithGoogle(idToken: String, name: String, email: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                authRepository.loginWithGoogle(idToken, name, email)
                _isLoading.value = false
                onSuccess()
            } catch (e: Exception) {
                _isLoading.value = false
                _error.value = "Google login gagal: ${e.message}"
            }
        }
    }

    fun loginWithPhone(onSuccess: () -> Unit) {
        val phoneNum = _phone.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                authRepository.loginWithPhone(phoneNum, "")
                _isLoading.value = false
                onSuccess()
            } catch (e: Exception) {
                _isLoading.value = false
                _error.value = "Phone login gagal: ${e.message}"
            }
        }
    }

    fun updateProfile(
        name: String,
        email: String?,
        phone: String?,
        address: String?,
        photoUrl: String?,
        role: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                authRepository.updateProfile(name, email, phone, address, photoUrl, role)
                _isLoading.value = false
                onSuccess()
            } catch (e: Exception) {
                _isLoading.value = false
                _error.value = "Gagal memperbarui profil: ${e.message}"
            }
        }
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onSuccess()
        }
    }
}
