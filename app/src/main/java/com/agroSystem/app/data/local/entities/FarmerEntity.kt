package com.agroSystem.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.agroSystem.app.data.models.Farmer

@Entity(tableName = "farmers")
data class FarmerEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val rating: String,
    val distance: String,
    val productsList: String,
    val imageResId: Int,
    val location: String,
    val description: String,
    val certs: List<String>
) {
    fun toDomain(): Farmer = Farmer(
        id = id,
        name = name,
        rating = rating,
        distance = distance,
        productsList = productsList,
        imageResId = imageResId,
        location = location,
        description = description,
        certs = certs
    )
}

fun Farmer.toEntity(): FarmerEntity = FarmerEntity(
    id = id,
    name = name,
    rating = rating,
    distance = distance,
    productsList = productsList,
    imageResId = imageResId,
    location = location,
    description = description,
    certs = certs
)
