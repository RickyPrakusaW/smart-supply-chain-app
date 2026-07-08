package com.agroSystem.app.data.models

data class Recipe(
    val id: Int,
    val name: String,
    val rating: String,
    val time: String,
    val difficulty: String,
    val imageResId: Int,
    val ingredients: List<Int>
)
