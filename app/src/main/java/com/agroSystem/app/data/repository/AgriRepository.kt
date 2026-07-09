package com.agroSystem.app.data.repository

import com.agroSystem.app.R
import com.agroSystem.app.data.local.dao.FarmerDao
import com.agroSystem.app.data.local.dao.ProductDao
import com.agroSystem.app.data.local.entities.toEntity
import com.agroSystem.app.data.models.Farmer
import com.agroSystem.app.data.models.Product
import com.agroSystem.app.data.remote.ApiService

class AgriRepository(
    private val productDao: ProductDao,
    private val farmerDao: FarmerDao,
    private val apiService: ApiService
) {

    val defaultProducts = listOf(
        Product(1, "Telur Ayam Kampung Segar", "Peternakan Tani Jaya, Malang", "5.0", 24000, "10 pcs", R.drawable.padi, "Telur", isEcoFriendly = true, deliveryDays = 1, protein = "13g", fat = "11g", carbs = "1.1g", calories = "155 Kcal", ingredients = "Telur ayam kampung organik segar hasil pakan jagung alami bebas antibiotik."),
        Product(2, "Keju Kambing Organik", "Koperasi Susu Pujon, Batu", "4.9", 45000, "200 g", R.drawable.sapi, "Susu", isDiscounted = true, originalPrice = 50000, isEcoFriendly = false, deliveryDays = 2, protein = "22g", fat = "24g", carbs = "3g", calories = "360 Kcal", ingredients = "Keju artisan semi-hard buatan tangan dari 100% susu kambing murni berkualitas tinggi."),
        Product(3, "Bayam Hidroponik Bersih", "Agro Makmur, Batu", "4.8", 12000, "250 g", R.drawable.sayuran, "Sayuran", isEcoFriendly = true, deliveryDays = 1, protein = "2.9g", fat = "0.4g", carbs = "3.6g", calories = "23 Kcal", ingredients = "Sayur bayam hijau segar hidroponik bebas pestisida kimia. Dikemas steril."),
        Product(4, "Daging Sapi Potong Premium", "Peternakan Singosari, Malang", "5.0", 95000, "500 g", R.drawable.sapi, "Daging", isEcoFriendly = false, deliveryDays = 3, protein = "26g", fat = "15g", carbs = "0g", calories = "250 Kcal", ingredients = "Daging sapi bagian tenderloin segar lokal berkualitas, tanpa hormon pertumbuhan."),
        Product(5, "Beras Merah Organik Cianjur", "Mitra Tani Sejahtera", "4.9", 35000, "1 kg", R.drawable.padi, "Bahan Sup", isEcoFriendly = true, deliveryDays = 5, protein = "7g", fat = "2.5g", carbs = "76g", calories = "350 Kcal", ingredients = "Beras merah pecah kulit organik bermutu tinggi dengan serat pangan alami yang kaya."),
        Product(6, "Tomat Beef Hidroponik", "Agro Makmur, Batu", "4.7", 15000, "500 g", R.drawable.sayuran, "Sayuran", isDiscounted = true, originalPrice = 18000, isEcoFriendly = true, deliveryDays = 2, protein = "0.9g", fat = "0.2g", carbs = "3.9g", calories = "18 Kcal", ingredients = "Tomat beef merah ukuran besar berdaging tebal, manis, dan berair tinggi."),
        Product(7, "Susu Sapi Murni Pasteurisasi", "Peternakan Pujon, Batu", "4.9", 18000, "1 L", R.drawable.sapi, "Susu", isEcoFriendly = false, deliveryDays = 1, protein = "3.2g", fat = "3.6g", carbs = "4.7g", calories = "62 Kcal", ingredients = "Susu sapi segar hasil perahan pagi hari yang dipasteurisasi kilat untuk menjaga kealamian rasa."),
        Product(8, "Wortel Manis Organik", "Kaki Gunung Panderman", "4.8", 14000, "500 g", R.drawable.sayuran, "Sayuran", isEcoFriendly = true, deliveryDays = 3, protein = "0.9g", fat = "0.2g", carbs = "9.6g", calories = "41 Kcal", ingredients = "Wortel lokal manis organik ditanam langsung di lahan tinggi bebas pencemaran udara.")
    )

    val defaultFarmers = listOf(
        Farmer(1, "Koperasi Susu & Keju Pujon", "5.0", "12 km", "Keju, Susu, Mentega", R.drawable.sapi, "Pujon, Malang", description = "Koperasi susu terpercaya di wilayah Pujon. Kami mengelola ratusan sapi perah lokal secara berkelanjutan dan memproduksi produk susu segar organik harian.", certs = listOf("Sertifikasi Organik Kementan", "Sertifikasi Halal MUI", "Sertifikasi Uji Lab Dinkes")),
        Farmer(2, "Madu Hutan Batu & Herbal", "4.9", "24 km", "Madu, Jamu, Manisan", R.drawable.sayuran, "Bumiaji, Batu", description = "Komunitas peternak lebah madu liar yang berfokus melestarikan hutan lindung Batu. Menghasilkan madu hutan liar murni berkualitas tinggi.", certs = listOf("Sertifikasi Organik Kementan", "Sertifikasi Halal MUI")),
        Farmer(3, "Agro Makmur Sayur & Buah", "4.8", "15 km", "Sayur, Tomat, Wortel", R.drawable.sayuran, "Batu, Malang", description = "Pertanian hidroponik modern yang menyuplai berbagai sayur dan buah dataran tinggi segar organik bebas pestisida kimia.", certs = listOf("Sertifikasi Organik Kementan", "Sertifikasi Uji Lab Dinkes")),
        Farmer(4, "Mitra Tani Padi Organik", "5.0", "32 km", "Beras Merah, Beras Putih", R.drawable.padi, "Cianjur, Jabar", description = "Kelompok tani tradisional yang melestarikan penanaman padi organik khas Cianjur menggunakan mata air pegunungan murni.", certs = listOf("Sertifikasi Organik Kementan", "Sertifikasi Halal MUI"))
    )

    suspend fun getProducts(forceRefresh: Boolean = false): List<Product> = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val cached = productDao.getAllProducts()
        if (cached.isEmpty() || forceRefresh) {
            try {
                val remote = apiService.getProducts()
                productDao.clearAllProducts()
                productDao.insertProducts(remote.map { it.toEntity() })
                return@withContext remote
            } catch (e: Exception) {
                if (cached.isEmpty()) {
                    productDao.insertProducts(defaultProducts.map { it.toEntity() })
                    return@withContext defaultProducts
                }
            }
        }
        productDao.getAllProducts().map { it.toDomain() }
    }

    suspend fun getFarmers(forceRefresh: Boolean = false): List<Farmer> = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val cached = farmerDao.getAllFarmers()
        if (cached.isEmpty() || forceRefresh) {
            try {
                val remote = apiService.getFarmers()
                farmerDao.clearAllFarmers()
                farmerDao.insertFarmers(remote.map { it.toEntity() })
                return@withContext remote
            } catch (e: Exception) {
                if (cached.isEmpty()) {
                    farmerDao.insertFarmers(defaultFarmers.map { it.toEntity() })
                    return@withContext defaultFarmers
                }
            }
        }
        farmerDao.getAllFarmers().map { it.toDomain() }
    }
}
