package com.agroSystem.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "edamam_food_cache")
data class EdamamFoodEntity(
    @PrimaryKey val foodId: String,
    val label: String,
    val category: String,
    val imageUrl: String?,
    val calories: Double,
    val protein: Double,
    val fat: Double,
    val carbs: Double,
    val queryTerm: String
)
