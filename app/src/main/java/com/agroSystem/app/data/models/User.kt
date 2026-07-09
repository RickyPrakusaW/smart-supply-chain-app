package com.agroSystem.app.data.models

data class User(
    val id: String,
    val name: String,
    val email: String?,
    val phone: String?,
    val role: String, // "Petani" or "Pembeli"
    val token: String?,
    val photoUrl: String?
)
