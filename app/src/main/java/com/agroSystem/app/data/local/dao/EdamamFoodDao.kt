package com.agroSystem.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.agroSystem.app.data.local.entities.EdamamFoodEntity

@Dao
interface EdamamFoodDao {
    @Query("SELECT * FROM edamam_food_cache WHERE queryTerm = :query")
    suspend fun getCachedFoods(query: String): List<EdamamFoodEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoods(foods: List<EdamamFoodEntity>)
}
