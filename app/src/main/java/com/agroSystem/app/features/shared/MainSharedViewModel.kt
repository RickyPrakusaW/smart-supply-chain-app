package com.agroSystem.app.features.shared

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.AndroidViewModel
import com.agroSystem.app.R
import com.agroSystem.app.data.models.Product
import com.agroSystem.app.data.models.Farmer
import com.agroSystem.app.data.models.Recipe
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.agroSystem.app.data.remote.ApiClient
import com.agroSystem.app.data.remote.OrderItemResponse
import com.agroSystem.app.data.remote.UpdateStatusRequest

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
                val list = ApiClient.apiService.getProducts()
                if (list.isNotEmpty()) {
                    allProducts.clear()
                    allProducts.addAll(list)
                    productsList.value = allProducts
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun createProduct(product: Product, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val res = ApiClient.authApiService.createProduct(product)
                if (res.success && res.data != null) {
                    allProducts.add(res.data)
                    productsList.value = allProducts
                    onResult(true)
                } else {
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
                val res = ApiClient.authApiService.updateProduct(product.id, product)
                if (res.success && res.data != null) {
                    val idx = allProducts.indexOfFirst { it.id == product.id }
                    if (idx != -1) {
                        allProducts[idx] = res.data
                        productsList.value = allProducts
                    }
                    onResult(true)
                } else {
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
                val res = ApiClient.authApiService.deleteProduct(productId)
                if (res.success) {
                    allProducts.removeAll { it.id == productId }
                    productsList.value = allProducts
                    onResult(true)
                } else {
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
                val response = ApiClient.authApiService.getSellerOrders(sellerId)
                if (response.success && response.data != null) {
                    sellerOrders.value = response.data
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateOrderStatus(orderId: String, status: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val response = ApiClient.authApiService.updateOrderStatus(orderId, UpdateStatusRequest(status))
                if (response.success) {
                    onResult(true)
                } else {
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
