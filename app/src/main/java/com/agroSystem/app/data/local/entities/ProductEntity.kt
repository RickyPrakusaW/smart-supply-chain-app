package com.agroSystem.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.agroSystem.app.data.models.Product

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val farmer: String,
    val rating: String,
    val price: Int,
    val unit: String,
    val imageResId: Int,
    val category: String,
    val isDiscounted: Boolean,
    val originalPrice: Int,
    val isEcoFriendly: Boolean,
    val deliveryDays: Int,
    val protein: String,
    val fat: String,
    val carbs: String,
    val calories: String,
    val ingredients: String,
    val shelfLife: String,
    val storage: String,
    val packaging: String,
    val diets: List<String>,
    val allergens: List<String>,
    val nutrients: List<String>,
    val ownerId: String?,
    val imageBytes: String?
) {
    fun toDomain(): Product = Product(
        id = id,
        name = name,
        farmer = farmer,
        rating = rating,
        price = price,
        unit = unit,
        imageResId = imageResId,
        category = category,
        isDiscounted = isDiscounted,
        originalPrice = originalPrice,
        isEcoFriendly = isEcoFriendly,
        deliveryDays = deliveryDays,
        protein = protein,
        fat = fat,
        carbs = carbs,
        calories = calories,
        ingredients = ingredients,
        shelfLife = shelfLife,
        storage = storage,
        packaging = packaging,
        diets = diets,
        allergens = allergens,
        nutrients = nutrients,
        ownerId = ownerId,
        imageBytes = imageBytes
    )
}

fun Product.toEntity(): ProductEntity = ProductEntity(
    id = id,
    name = name,
    farmer = farmer,
    rating = rating,
    price = price,
    unit = unit,
    imageResId = imageResId,
    category = category,
    isDiscounted = isDiscounted,
    originalPrice = originalPrice,
    isEcoFriendly = isEcoFriendly,
    deliveryDays = deliveryDays,
    protein = protein,
    fat = fat,
    carbs = carbs,
    calories = calories,
    ingredients = ingredients,
    shelfLife = shelfLife,
    storage = storage,
    packaging = packaging,
    diets = diets,
    allergens = allergens,
    nutrients = nutrients,
    ownerId = ownerId,
    imageBytes = imageBytes
)
