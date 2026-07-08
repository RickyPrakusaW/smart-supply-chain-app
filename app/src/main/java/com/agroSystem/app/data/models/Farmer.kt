package com.agroSystem.app.data.models

data class Farmer(
    val id: Int,
    val name: String,
    val rating: String,
    val distance: String,
    val productsList: String,
    val imageResId: Int,
    val location: String,
    val description: String = "",
    val certs: List<String> = emptyList()
)
