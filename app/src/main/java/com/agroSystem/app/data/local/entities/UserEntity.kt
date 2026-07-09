package com.agroSystem.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.agroSystem.app.data.models.User

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String?,
    val phone: String?,
    val role: String,
    val token: String?,
    val photoUrl: String?,
    val address: String?
) {
    fun toDomain(): User {
        return User(
            id = id,
            name = name,
            email = email,
            phone = phone,
            role = role,
            token = token,
            photoUrl = photoUrl,
            address = address
        )
    }
}

fun User.toEntity(): UserEntity {
    return UserEntity(
        id = id,
        name = name,
        email = email,
        phone = phone,
        role = role,
        token = token,
        photoUrl = photoUrl,
        address = address
    )
}
