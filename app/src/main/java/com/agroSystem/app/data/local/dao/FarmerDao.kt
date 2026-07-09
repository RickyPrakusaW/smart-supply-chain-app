package com.agroSystem.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.agroSystem.app.data.local.entities.FarmerEntity

@Dao
interface FarmerDao {
    @Query("SELECT * FROM farmers")
    fun getAllFarmers(): List<FarmerEntity>

    @Query("SELECT * FROM farmers WHERE id = :id")
    fun getFarmerById(id: Int): FarmerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertFarmers(farmers: List<FarmerEntity>): List<Long>

    @Query("DELETE FROM farmers")
    fun clearAllFarmers(): Int
}
