package com.agroSystem.app.features.shared

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.AndroidViewModel
import com.agroSystem.app.R
import com.agroSystem.app.data.local.AppDatabase
import com.agroSystem.app.data.local.entities.toEntity
import com.agroSystem.app.data.models.Product
import com.agroSystem.app.data.models.Farmer
import com.agroSystem.app.data.models.Recipe
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import com.agroSystem.app.data.remote.OrderItemResponse

class MainSharedViewModel(application: Application) : AndroidViewModel(application) {

    val allProducts = mutableListOf(
        Product(1, "Telur Ayam Kampung Segar", "Peternakan Tani Jaya, Malang", "5.0", 24000, "10 pcs", R.drawable.padi, "Telur", isEcoFriendly = true, deliveryDays = 1, protein = "13g", fat = "11g", carbs = "1.1g", calories = "155 Kcal", ingredients = "Telur ayam kampung organik segar hasil pakan jagung alami bebas antibiotik."),
        Product(2, "Keju Kambing Organik", "Koperasi Susu Pujon, Batu", "4.9", 45000, "200 g", R.drawable.sapi, "Susu", isDiscounted = true, originalPrice = 50000, isEcoFriendly = false, deliveryDays = 2, protein = "22g", fat = "24g", carbs = "3g", calories = "360 Kcal", ingredients = "Keju artisan semi-hard buatan tangan dari 100% susu kambing murni berkualitas tinggi."),
        Product(3, "Bayam Hidroponik Bersih", "Agro Makmur, Batu", "4.8", 12000, "250 g", R.drawable.sayuran, "Sayuran", isEcoFriendly = true, deliveryDays = 1, protein = "2.9g", fat = "0.4g", carbs = "3.6g", calories = "23 Kcal", ingredients = "Sayur bayam hijau segar hidroponik bebas pestisida kimia. Dikemas steril."),
        Product(4, "Daging Sapi Potong Premium", "Peternakan Singosari, Malang", "5.0", 95000, "500 g", R.drawable.sapi, "Daging", isEcoFriendly = false, deliveryDays = 3, protein = "26g", fat = "15g", carbs = "0g", calories = "250 Kcal", ingredients = "Daging sapi bagian tenderloin segar lokal berkualitas, tanpa hormon pertumbuhan."),
        Product(5, "Beras Merah Organik Cianjur", "Mitra Tani Sejahtera", "4.9", 35000, "1 kg", R.drawable.padi, "Bahan Sup", isEcoFriendly = true, deliveryDays = 5, protein = "7g", fat = "2.5g", carbs = "76g", calories = "350 Kcal", ingredients = "Beras merah pecah kulit organik bermutu tinggi dengan serat pangan alami yang kaya."),
        Product(6, "Tomat Beef Hidroponik", "Agro Makmur, Batu", "4.7", 15000, "500 g", R.drawable.sayuran, "Sayuran", isDiscounted = true, originalPrice = 18000, isEcoFriendly = true, deliveryDays = 2, protein = "0.9g", fat = "0.2g", carbs = "3.9g", calories = "18 Kcal", ingredients = "Tomat beef merah ukuran besar berdaging tebal, manis, dan berair tinggi."),
        Product(7, "Susu Sapi Murni Pasteurisasi", "Peternakan Pujon, Batu", "4.9", 18000, "1 L", R.drawable.sapi, "Susu", isEcoFriendly = false, deliveryDays = 1, protein = "3.2g", fat = "3.6g", carbs = "4.7g", calories = "62 Kcal", ingredients = "Susu sapi segar hasil perahan pagi hari yang dipasteurisasi kilat untuk menjaga kealamian rasa."),
        Product(8, "Wortel Manis Organik", "Kaki Gunung Panderman", "4.8", 14000, "500 g", R.drawable.sayuran, "Sayuran", isEcoFriendly = true, deliveryDays = 3, protein = "0.9g", fat = "0.2g", carbs = "9.6g", calories = "41 Kcal", ingredients = "Wortel lokal manis organik ditanam langsung di lahan tinggi bebas pencemaran udara.")
    )

    val allFarmers = listOf(
        Farmer(1, "Koperasi Susu & Keju Pujon", "5.0", "12 km", "Keju, Susu, Mentega", R.drawable.sapi, "Pujon, Malang", description = "Koperasi susu terpercaya di wilayah Pujon. Kami mengelola ratusan sapi perah lokal secara berkelanjutan dan memproduksi produk susu segar organik harian.", certs = listOf("Sertifikasi Organik Kementan", "Sertifikasi Halal MUI", "Sertifikasi Uji Lab Dinkes")),
        Farmer(2, "Madu Hutan Batu & Herbal", "4.9", "24 km", "Madu, Jamu, Manisan", R.drawable.sayuran, "Bumiaji, Batu", description = "Komunitas peternak lebah madu liar yang berfokus melestarikan hutan lindung Batu. Menghasilkan madu hutan liar murni berkualitas tinggi.", certs = listOf("Sertifikasi Organik Kementan", "Sertifikasi Halal MUI")),
        Farmer(3, "Agro Makmur Sayur & Buah", "4.8", "15 km", "Sayur, Tomat, Wortel", R.drawable.sayuran, "Batu, Malang", description = "Pertanian hidroponik modern yang menyuplai berbagai sayur dan buah dataran tinggi segar organik bebas pestisida kimia.", certs = listOf("Sertifikasi Organik Kementan", "Sertifikasi Uji Lab Dinkes")),
        Farmer(4, "Mitra Tani Padi Organik", "5.0", "32 km", "Beras Merah, Beras Putih", R.drawable.padi, "Cianjur, Jabar", description = "Kelompok tani tradisional yang melestarikan penanaman padi organik khas Cianjur menggunakan mata air pegunungan murni.", certs = listOf("Sertifikasi Organik Kementan", "Sertifikasi Halal MUI"))
    )

    val allRecipes = listOf(
        Recipe(1, "Omelet Sayur Segar Hijau", "5.0", "15 min", "Mudah", R.drawable.sayuran, listOf(1, 3)),
        Recipe(2, "Smoothie Susu Alpukat", "4.9", "10 min", "Mudah", R.drawable.sapi, listOf(7)),
        Recipe(3, "Casserole Kentang Keju", "5.0", "35 min", "Sedang", R.drawable.sapi, listOf(2)),
        Recipe(4, "Sup Bayam Kaldu Organik", "4.8", "20 min", "Mudah", R.drawable.padi, listOf(3, 5))
    )

    val categories = listOf("Semua", "Telur", "Susu", "Sayuran", "Daging", "Bahan Sup")

    // Core States
    private val _cartItems = MutableLiveData<MutableMap<Product, Int>>(mutableMapOf())
    val cartItems: LiveData<MutableMap<Product, Int>> = _cartItems

    private val _favoriteProductIds = MutableLiveData<MutableList<Int>>(mutableListOf())
    val favoriteProductIds: LiveData<MutableList<Int>> = _favoriteProductIds

    val searchQuery = MutableLiveData<String>("")
    val selectedCategory = MutableLiveData<String>("Semua")

    // Undo states
    val recentlyRemovedProduct = MutableLiveData<Product?>(null)
    val recentlyRemovedQty = MutableLiveData<Int>(0)
    val isUndoVisible = MutableLiveData<Boolean>(false)

    // Filter states
    val filterDeliveryDays = MutableLiveData<Int>(0)
    val filterRegion = MutableLiveData<String>("Semua")
    val filterEcoFriendly = MutableLiveData<Boolean>(false)
    val filterDiscountedOnly = MutableLiveData<Boolean>(false)
    val filterSelectedDiets = MutableLiveData<MutableList<String>>(mutableListOf())
    val filterSelectedAllergens = MutableLiveData<MutableList<String>>(mutableListOf())
    val filterSelectedNutrients = MutableLiveData<MutableList<String>>(mutableListOf())

    private val sharedPrefs = application.getSharedPreferences("agrimitra_cart_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    val productsList = MutableLiveData<List<Product>>(allProducts)
    val sellerOrders = MutableLiveData<List<OrderItemResponse>>(emptyList())

    init {
        loadCartFromPrefs()
        fetchProductsFromServer()
    }

    fun fetchProductsFromServer() {
        viewModelScope.launch {
            try {
                val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                db.collection("products").get()
                    .addOnSuccessListener { result ->
                        val list = result.map { doc ->
                            Product(
                                id = doc.getLong("id")?.toInt() ?: 0,
                                name = doc.getString("name") ?: "",
                                farmer = doc.getString("farmer") ?: "",
                                rating = doc.getString("rating") ?: "5.0",
                                price = doc.getLong("price")?.toInt() ?: 0,
                                unit = doc.getString("unit") ?: "1 kg",
                                imageResId = doc.getLong("imageResId")?.toInt() ?: R.drawable.padi,
                                category = doc.getString("category") ?: "Lainnya",
                                isDiscounted = doc.getBoolean("isDiscounted") ?: false,
                                originalPrice = doc.getLong("originalPrice")?.toInt() ?: 0,
                                isEcoFriendly = doc.getBoolean("isEcoFriendly") ?: false,
                                deliveryDays = doc.getLong("deliveryDays")?.toInt() ?: 1,
                                protein = doc.getString("protein") ?: "3g",
                                fat = doc.getString("fat") ?: "5g",
                                carbs = doc.getString("carbs") ?: "4.7g",
                                calories = doc.getString("calories") ?: "64 Kcal",
                                ingredients = doc.getString("ingredients") ?: "",
                                shelfLife = doc.getString("shelfLife") ?: "5 Hari",
                                storage = doc.getString("storage") ?: "Suhu Dingin (+2°C s.d +6°C)",
                                packaging = doc.getString("packaging") ?: "Botol Kaca steril 1 Liter (Ramah Lingkungan)",
                                ownerId = doc.getString("ownerId"),
                                imageBytes = doc.getString("imageBytes")
                            )
                        }
                        
                        if (list.isNotEmpty()) {
                            viewModelScope.launch(Dispatchers.IO) {
                                val localDb = AppDatabase.getDatabase(getApplication())
                                localDb.productDao().clearAllProducts()
                                localDb.productDao().insertProducts(list.map { it.toEntity() })
                            }
                            
                            allProducts.clear()
                            allProducts.addAll(list)
                            productsList.value = allProducts
                        } else {
                            seedProductsToFirestore()
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("MainSharedViewModel", "Firestore fetch failed, reading from Room", e)
                        loadProductsFromLocalRoom()
                    }
            } catch (e: Exception) {
                e.printStackTrace()
                loadProductsFromLocalRoom()
            }
        }
    }

    private fun loadProductsFromLocalRoom() {
        viewModelScope.launch(Dispatchers.IO) {
            val localDb = AppDatabase.getDatabase(getApplication())
            val localProducts = localDb.productDao().getAllProducts().map { it.toDomain() }
            if (localProducts.isNotEmpty()) {
                viewModelScope.launch(Dispatchers.Main) {
                    allProducts.clear()
                    allProducts.addAll(localProducts)
                    productsList.value = allProducts
                }
            }
        }
    }

    private fun seedProductsToFirestore() {
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        val batch = db.batch()
        allProducts.forEach { product ->
            val docRef = db.collection("products").document(product.id.toString())
            batch.set(docRef, product)
        }
        batch.commit().addOnSuccessListener {
            productsList.value = allProducts
        }
    }

    fun createProduct(product: Product, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                db.collection("products").document(product.id.toString()).set(product)
                    .addOnSuccessListener {
                        viewModelScope.launch(Dispatchers.IO) {
                            val localDb = AppDatabase.getDatabase(getApplication())
                            localDb.productDao().insertProducts(listOf(product.toEntity()))
                        }
                        allProducts.add(product)
                        productsList.value = allProducts
                        onResult(true)
                    }
                    .addOnFailureListener {
                        onResult(false)
                    }
            } catch (e: Exception) {
                e.printStackTrace()
                onResult(false)
            }
        }
    }

    fun updateProduct(product: Product, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                db.collection("products").document(product.id.toString()).set(product)
                    .addOnSuccessListener {
                        viewModelScope.launch(Dispatchers.IO) {
                            val localDb = AppDatabase.getDatabase(getApplication())
                            localDb.productDao().insertProducts(listOf(product.toEntity()))
                        }
                        val idx = allProducts.indexOfFirst { it.id == product.id }
                        if (idx != -1) {
                            allProducts[idx] = product
                            productsList.value = allProducts
                        }
                        onResult(true)
                    }
                    .addOnFailureListener {
                        onResult(false)
                    }
            } catch (e: Exception) {
                e.printStackTrace()
                onResult(false)
            }
        }
    }

    fun deleteProduct(productId: Int, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                db.collection("products").document(productId.toString()).delete()
                    .addOnSuccessListener {
                        viewModelScope.launch(Dispatchers.IO) {
                            val localDb = AppDatabase.getDatabase(getApplication())
                            localDb.productDao().deleteProductById(productId)
                        }
                        allProducts.removeAll { it.id == productId }
                        productsList.value = allProducts
                        onResult(true)
                    }
                    .addOnFailureListener {
                        onResult(false)
                    }
            } catch (e: Exception) {
                e.printStackTrace()
                onResult(false)
            }
        }
    }

    fun fetchSellerOrders(sellerId: String) {
        viewModelScope.launch {
            try {
                val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                db.collection("orders").get()
                    .addOnSuccessListener { result ->
                        val orders = result.map { doc ->
                            val itemsRaw = doc.get("items") as? List<Map<String, Any>>
                            val checkoutItems = itemsRaw?.map { itemMap ->
                                com.agroSystem.app.data.remote.CheckoutItem(
                                    id = (itemMap["id"] as? Long)?.toInt() ?: 0,
                                    name = itemMap["name"] as? String ?: "",
                                    price = (itemMap["price"] as? Long)?.toInt() ?: 0,
                                    quantity = (itemMap["quantity"] as? Long)?.toInt() ?: 0,
                                    ownerId = itemMap["ownerId"] as? String
                                )
                            }
                            
                            com.agroSystem.app.data.remote.OrderItemResponse(
                                orderId = doc.getString("orderId") ?: doc.id,
                                userId = doc.getString("userId"),
                                amount = doc.getLong("amount")?.toInt() ?: 0,
                                status = doc.getString("status") ?: "pending",
                                createdAt = doc.getString("createdAt") ?: doc.getTimestamp("createdAt")?.toDate()?.toString() ?: "",
                                items = checkoutItems,
                                payment = null
                            )
                        }
                        val filteredOrders = orders.filter { order ->
                            order.items?.any { it.ownerId == sellerId } == true
                        }
                        sellerOrders.value = filteredOrders
                    }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateOrderStatus(orderId: String, status: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                db.collection("orders").document(orderId).update("status", status)
                    .addOnSuccessListener {
                        onResult(true)
                    }
                    .addOnFailureListener {
                        onResult(false)
                    }
            } catch (e: Exception) {
                e.printStackTrace()
                onResult(false)
            }
        }
    }

    private fun saveCartToPrefs() {
        val items = _cartItems.value ?: return
        val idMap = items.mapKeys { it.key.id }
        val json = gson.toJson(idMap)
        sharedPrefs.edit().putString("cart_json", json).apply()
    }

    private fun loadCartFromPrefs() {
        val json = sharedPrefs.getString("cart_json", null)
        if (!json.isNullOrEmpty()) {
            try {
                val type = object : TypeToken<Map<Int, Int>>() {}.type
                val idMap: Map<Int, Int> = gson.fromJson(json, type)
                val cartMap = mutableMapOf<Product, Int>()
                idMap.forEach { (productId, qty) ->
                    val product = allProducts.firstOrNull { it.id == productId }
                    if (product != null) {
                        cartMap[product] = qty
                    }
                }
                _cartItems.value = cartMap
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun addProductToCart(product: Product) {
        val current = _cartItems.value ?: mutableMapOf()
        current[product] = (current[product] ?: 0) + 1
        _cartItems.value = current
        saveCartToPrefs()
    }

    fun removeProductFromCart(product: Product) {
        val current = _cartItems.value ?: mutableMapOf()
        val currentQty = current[product] ?: 0
        if (currentQty > 1) {
            current[product] = currentQty - 1
            _cartItems.value = current
        } else {
            recentlyRemovedProduct.value = product
            recentlyRemovedQty.value = currentQty
            current.remove(product)
            _cartItems.value = current
            isUndoVisible.value = true
        }
        saveCartToPrefs()
    }

    fun restoreRemovedProduct() {
        val product = recentlyRemovedProduct.value
        val qty = recentlyRemovedQty.value ?: 0
        if (product != null && qty > 0) {
            val current = _cartItems.value ?: mutableMapOf()
            current[product] = qty
            _cartItems.value = current
            isUndoVisible.value = false
            saveCartToPrefs()
        }
    }

    fun clearCart() {
        val current = _cartItems.value ?: mutableMapOf()
        current.clear()
        _cartItems.value = current
        saveCartToPrefs()
    }

    fun addRecipeIngredients(recipe: Recipe) {
        val current = _cartItems.value ?: mutableMapOf()
        recipe.ingredients.forEach { prodId ->
            val product = allProducts.firstOrNull { it.id == prodId }
            if (product != null) {
                current[product] = (current[product] ?: 0) + 1
            }
        }
        _cartItems.value = current
        saveCartToPrefs()
    }

    fun toggleFavorite(productId: Int) {
        val current = _favoriteProductIds.value ?: mutableListOf()
        if (current.contains(productId)) {
            current.remove(productId)
        } else {
            current.add(productId)
        }
        _favoriteProductIds.value = current
    }

    fun resetFilters() {
        filterDeliveryDays.value = 0
        filterRegion.value = "Semua"
        filterEcoFriendly.value = false
        filterDiscountedOnly.value = false
        filterSelectedDiets.value = mutableListOf()
        filterSelectedAllergens.value = mutableListOf()
        filterSelectedNutrients.value = mutableListOf()
    }

    // Computed Filtered Products list
    fun getFilteredProducts(): List<Product> {
        val query = searchQuery.value ?: ""
        val category = selectedCategory.value ?: "Semua"
        val delivery = filterDeliveryDays.value ?: 0
        val reg = filterRegion.value ?: "Semua"
        val eco = filterEcoFriendly.value ?: false
        val disc = filterDiscountedOnly.value ?: false
        val diets = filterSelectedDiets.value ?: emptyList()
        val allergens = filterSelectedAllergens.value ?: emptyList()
        val nutrients = filterSelectedNutrients.value ?: emptyList()

        val sourceList = productsList.value ?: allProducts
        return sourceList.filter { product ->
            val matchesSearch = (product.name ?: "").contains(query, ignoreCase = true) ||
                    (product.farmer ?: "").contains(query, ignoreCase = true)
            val matchesCategory = category == "Semua" || product.category == category

            val matchesDelivery = delivery == 0 || product.deliveryDays <= delivery
            val matchesEco = !eco || product.isEcoFriendly
            val matchesDiscount = !disc || product.isDiscounted

            val matchesDiet = diets.isEmpty() || diets.all { diet ->
                when (diet) {
                    "Vegetarian" -> product.category in listOf("Sayuran", "Susu", "Telur", "Bahan Sup")
                    "Vegan" -> product.category in listOf("Sayuran", "Bahan Sup")
                    "Keto" -> product.category in listOf("Daging", "Susu", "Telur")
                    else -> true
                }
            }

            val matchesAllergen = allergens.isEmpty() || allergens.all { allergen ->
                when (allergen) {
                    "Bebas Laktosa" -> product.category != "Susu"
                    "Bebas Gluten" -> product.category != "Telur"
                    else -> true
                }
            }

            val matchesNutrient = nutrients.isEmpty() || nutrients.all { nutrient ->
                when (nutrient) {
                    "Tinggi Kalsium" -> product.category == "Susu"
                    "Tinggi Protein" -> product.category in listOf("Daging", "Telur")
                    "Kaya Serat" -> product.category == "Sayuran"
                    else -> true
                }
            }

            matchesSearch && matchesCategory && matchesDelivery && matchesEco && matchesDiscount &&
                    matchesDiet && matchesAllergen && matchesNutrient
        }
    }
}
