package com.agroSystem.app.data.models

data class Product(
    val id: Int,
    val name: String,
    val farmer: String,
    val rating: String,
    val price: Int,
    val unit: String,
    val imageResId: Int,
    val category: String,
    val isDiscounted: Boolean = false,
    val originalPrice: Int = 0,
    val isEcoFriendly: Boolean = false,
    val deliveryDays: Int = 1,
    val protein: String = "3g",
    val fat: String = "5g",
    val carbs: String = "4.7g",
    val calories: String = "64 Kcal",
    val ingredients: String = "",
    val shelfLife: String = "5 Hari",
    val storage: String = "Suhu Dingin (+2°C s.d +6°C)",
    val packaging: String = "Botol Kaca steril 1 Liter (Ramah Lingkungan)",
    val diets: List<String> = emptyList(),
    val allergens: List<String> = emptyList(),
    val nutrients: List<String> = emptyList()
)
