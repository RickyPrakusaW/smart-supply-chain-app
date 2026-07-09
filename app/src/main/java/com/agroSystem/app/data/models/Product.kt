package com.agroSystem.app.data.models

import com.agroSystem.app.R

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
    val nutrients: List<String> = emptyList(),
    val ownerId: String? = null,
    val imageBytes: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Product) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + (farmer?.hashCode() ?: 0)
        result = 31 * result + (category?.hashCode() ?: 0)
        return result
    }
}

fun Product.bindImageTo(imageView: android.widget.ImageView) {
    if (!this.imageBytes.isNullOrEmpty()) {
        try {
            val decodedString = android.util.Base64.decode(this.imageBytes, android.util.Base64.DEFAULT)
            val decodedByte = android.graphics.BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
            imageView.setImageBitmap(decodedByte)
            return
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val fallbackResId = when (this.category?.lowercase()) {
        "susu" -> R.drawable.sapi
        "sayuran" -> R.drawable.sayuran
        "daging" -> R.drawable.sapi
        else -> R.drawable.padi
    }
    imageView.setImageResource(fallbackResId)
}
