package com.agroSystem.app.features.seller

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.agroSystem.app.data.local.AppDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.gms.tasks.Tasks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OfflineSyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.d("OfflineSyncWorker", "Starting offline synchronization work...")
        val db = AppDatabase.getDatabase(applicationContext)
        val unsyncedProducts = db.productDao().getUnsyncedProducts()

        if (unsyncedProducts.isEmpty()) {
            Log.d("OfflineSyncWorker", "No unsynced products found. Work complete.")
            return@withContext Result.success()
        }

        Log.d("OfflineSyncWorker", "Found ${unsyncedProducts.size} unsynced products.")
        var hasError = false
        val firestore = FirebaseFirestore.getInstance()

        for (entity in unsyncedProducts) {
            try {
                val product = entity.toDomain()
                val syncedProduct = product.copy(isSynced = true)

                // Upload to firestore synchronously using Tasks.await
                val task = firestore.collection("products")
                    .document(syncedProduct.id.toString())
                    .set(syncedProduct)
                
                Tasks.await(task)

                // Update local Room database to set isSynced = true
                db.productDao().markProductAsSynced(entity.id)
                Log.d("OfflineSyncWorker", "Product '${entity.name}' successfully synced to Firestore.")

                // Show push notification
                NotificationHelper.showNotification(
                    applicationContext,
                    "Produk Disinkronkan",
                    "Produk Tani '${entity.name}' berhasil diunggah ke server cloud!"
                )
            } catch (e: Exception) {
                Log.e("OfflineSyncWorker", "Failed to sync product '${entity.name}': ${e.message}", e)
                hasError = true
            }
        }

        if (hasError) {
            Log.d("OfflineSyncWorker", "One or more products failed to sync, marking retry.")
            Result.retry()
        } else {
            Log.d("OfflineSyncWorker", "All products successfully synchronized.")
            Result.success()
        }
    }
}
