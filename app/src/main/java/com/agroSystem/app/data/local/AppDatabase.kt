package com.agroSystem.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.agroSystem.app.data.local.converters.Converters
import com.agroSystem.app.data.local.dao.FarmerDao
import com.agroSystem.app.data.local.dao.ProductDao
import com.agroSystem.app.data.local.dao.UserDao
import com.agroSystem.app.data.local.entities.FarmerEntity
import com.agroSystem.app.data.local.entities.ProductEntity
import com.agroSystem.app.data.local.entities.UserEntity

@Database(entities = [ProductEntity::class, FarmerEntity::class, UserEntity::class], version = 4, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun farmerDao(): FarmerDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "agri_mitra_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
