package com.agroSystem.app.data.remote

import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("auth/google")
    suspend fun loginWithGoogle(@Body request: GoogleLoginRequest): AuthResponse

    @POST("auth/phone")
    suspend fun loginWithPhone(@Body request: PhoneLoginRequest): AuthResponse

    @POST("auth/update-profile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): AuthResponse
}

data class GoogleLoginRequest(
    val idToken: String,
    val name: String,
    val email: String
)

data class PhoneLoginRequest(
    val phone: String,
    val name: String
)

data class UpdateProfileRequest(
    val userId: String,
    val name: String,
    val email: String?,
    val phone: String?,
    val address: String?,
    val photoUrl: String?,
    val role: String
)

data class AuthResponse(
    val success: Boolean,
    val token: String,
    val id: String,
    val name: String,
    val email: String?,
    val phone: String?,
    val role: String,
    val photoUrl: String?,
    val address: String?
)
