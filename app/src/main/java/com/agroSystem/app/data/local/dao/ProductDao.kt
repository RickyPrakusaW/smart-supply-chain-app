package com.agroSystem.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.agroSystem.app.data.local.entities.ProductEntity

@Dao
interface ProductDao {
    @Query("SELECT * FROM products")
    fun getAllProducts(): List<ProductEntity>

    @Query("SELECT * FROM products WHERE id = :id")
    fun getProductById(id: Int): ProductEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertProducts(products: List<ProductEntity>): List<Long>

    @Query("DELETE FROM products")
    fun clearAllProducts(): Int
}
