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

    @Query("DELETE FROM products WHERE id = :id")
    fun deleteProductById(id: Int): Int

    @Query("SELECT * FROM products WHERE isSynced = 0")
    fun getUnsyncedProducts(): List<ProductEntity>

    @Query("UPDATE products SET isSynced = 1 WHERE id = :id")
    fun markProductAsSynced(id: Int): Int
}
