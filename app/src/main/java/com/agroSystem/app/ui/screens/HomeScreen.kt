package com.agroSystem.app.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agroSystem.app.R

// Color Tokens from Note.md
private val OnboardBgWarm = Color(0xFFF5F3EE)
private val OnboardSurfaceWarm = Color(0xFFF0EDE9)
private val OnboardBorderGrey = Color(0xFFD3CEC6)
private val OnboardPrimaryGreen = Color(0xFF475B40)
private val OnboardTextDark = Color(0xFF333333)
private val OnboardTextMuted = Color(0xFF6B7A64)
private val OnboardAccentTerracotta = Color(0xFFBE7C4A)
private val OnboardOliveDark = Color(0xFF828B4C)
private val OnboardOliveLight = Color(0xFFEEEDE3)

data class Product(
    val id: Int,
    val name: String,
    val farmer: String,
    val rating: String,
    val price: Int,
    val unit: String,
    val imageResId: Int,
    val category: String,
    val isDiscounted: Boolean = false,
    val originalPrice: Int = 0,
    val isEcoFriendly: Boolean = false,
    val deliveryDays: Int = 1, // 1 for today, 2 for tomorrow, etc.
    val protein: String = "3g",
    val fat: String = "5g",
    val carbs: String = "4.7g",
    val calories: String = "64 Kcal",
    val ingredients: String = "Susu sapi perah segar pasteurisasi pilihan dari lereng Gunung Panderman. Diproses higienis tanpa zat pengawet tambahan.",
    val shelfLife: String = "5 Hari",
    val storage: String = "Suhu Dingin (+2°C s.d +6°C)",
    val packaging: String = "Botol Kaca steril 1 Liter (Ramah Lingkungan)",
    val diets: List<String> = listOf("Vegetarian", "Keto"),
    val allergens: List<String> = listOf("Bebas Laktosa", "Bebas Gluten"),
    val nutrients: List<String> = listOf("Tinggi Kalsium", "Kaya Protein", "Vitamin B2", "Vitamin B12")
)

data class Farmer(
    val id: Int,
    val name: String,
    val rating: String,
    val distance: String,
    val productsList: String,
    val imageResId: Int,
    val location: String,
    val description: String = "Koperasi susu terpercaya di wilayah Pujon. Kami mengelola ratusan sapi perah lokal secara berkelanjutan dan memproduksi produk susu segar organik harian dengan pengawasan kesehatan yang ketat untuk menjamin cita rasa susu alami asli Indonesia.",
    val certs: List<String> = listOf("Sertifikasi Organik Kementan", "Sertifikasi Halal MUI", "Sertifikasi Uji Lab Dinkes")
)

data class Recipe(
    val id: Int,
    val name: String,
    val rating: String,
    val time: String,
    val difficulty: String,
    val imageResId: Int,
    val ingredients: List<Int> // list of product IDs
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onResetOnboarding: () -> Unit) {
    val context = LocalContext.current

    // Navigation Tab state: "home", "catalog", "favorites", "profile"
    var currentTab by remember { mutableStateOf("home") }

    // Sub-tab selection for Catalog: "Produk" (0), "Petani" (1), "Resep" (2)
    var catalogSubTab by remember { mutableIntStateOf(0) }

    // Search query & focus states
    var searchQuery by remember { mutableStateOf("") }
    var isSearchFocused by remember { mutableStateOf(false) }

    // Cart items state: Product to Quantity (MutableStateMap)
    val cartItems = remember { mutableStateMapOf<Product, Int>() }

    // Favorites list state
    val favoriteProductIds = remember { mutableStateListOf<Int>() }

    // Selected category for filtering products
    var selectedCategory by remember { mutableStateOf("Semua") }

    // --- Navigation stack states for detail views ---
    var selectedProductForDetail by remember { mutableStateOf<Product?>(null) }
    var selectedFarmerForDetail by remember { mutableStateOf<Farmer?>(null) }

    // --- Filter States (Mocking Image 4 filters) ---
    var isFilterSheetOpen by remember { mutableStateOf(false) }
    var filterDeliveryDays by remember { mutableIntStateOf(0) } // 0 = Semua, 1 = Hari Ini, 2 = Besok, etc.
    var filterRegion by remember { mutableStateOf("Semua") } // "Semua", "Malang Raya", "Terdekat"
    var filterEcoFriendly by remember { mutableStateOf(false) }
    var filterDiscountedOnly by remember { mutableStateOf(false) }

    // Multi-select lists for Expandable sections
    val filterSelectedDiets = remember { mutableStateListOf<String>() }
    val filterSelectedAllergens = remember { mutableStateListOf<String>() }
    val filterSelectedNutrients = remember { mutableStateListOf<String>() }

    // Raw mock product list
    val allProducts = remember {
        listOf(
            Product(1, "Telur Ayam Kampung Segar", "Peternakan Tani Jaya, Malang", "5.0", 24000, "10 pcs", R.drawable.padi, "Telur", isEcoFriendly = true, deliveryDays = 1, protein = "13g", fat = "11g", carbs = "1.1g", calories = "155 Kcal", ingredients = "Telur ayam kampung organik segar hasil pakan jagung alami bebas antibiotik."),
            Product(2, "Keju Kambing Organik", "Koperasi Susu Pujon, Batu", "4.9", 45000, "200 g", R.drawable.sapi, "Susu", isDiscounted = true, originalPrice = 50000, isEcoFriendly = false, deliveryDays = 2, protein = "22g", fat = "24g", carbs = "3g", calories = "360 Kcal", ingredients = "Keju artisan semi-hard buatan tangan dari 100% susu kambing murni berkualitas tinggi."),
            Product(3, "Bayam Hidroponik Bersih", "Agro Makmur, Batu", "4.8", 12000, "250 g", R.drawable.sayuran, "Sayuran", isEcoFriendly = true, deliveryDays = 1, protein = "2.9g", fat = "0.4g", carbs = "3.6g", calories = "23 Kcal", ingredients = "Sayur bayam hijau segar hidroponik bebas pestisida kimia. Dikemas steril."),
            Product(4, "Daging Sapi Potong Premium", "Peternakan Singosari, Malang", "5.0", 95000, "500 g", R.drawable.sapi, "Daging", isEcoFriendly = false, deliveryDays = 3, protein = "26g", fat = "15g", carbs = "0g", calories = "250 Kcal", ingredients = "Daging sapi bagian tenderloin segar lokal berkualitas, tanpa hormon pertumbuhan."),
            Product(5, "Beras Merah Organik Cianjur", "Mitra Tani Sejahtera", "4.9", 35000, "1 kg", R.drawable.padi, "Bahan Sup", isEcoFriendly = true, deliveryDays = 5, protein = "7g", fat = "2.5g", carbs = "76g", calories = "350 Kcal", ingredients = "Beras merah pecah kulit organik bermutu tinggi dengan serat pangan alami yang kaya."),
            Product(6, "Tomat Beef Hidroponik", "Agro Makmur, Batu", "4.7", 15000, "500 g", R.drawable.sayuran, "Sayuran", isDiscounted = true, originalPrice = 18000, isEcoFriendly = true, deliveryDays = 2, protein = "0.9g", fat = "0.2g", carbs = "3.9g", calories = "18 Kcal", ingredients = "Tomat beef merah ukuran besar berdaging tebal, manis, dan berair tinggi."),
            Product(7, "Susu Sapi Murni Pasteurisasi", "Peternakan Pujon, Batu", "4.9", 18000, "1 L", R.drawable.sapi, "Susu", isEcoFriendly = false, deliveryDays = 1, protein = "3.2g", fat = "3.6g", carbs = "4.7g", calories = "62 Kcal", ingredients = "Susu sapi segar hasil perahan pagi hari yang dipasteurisasi kilat untuk menjaga kealamian rasa."),
            Product(8, "Wortel Manis Organik", "Kaki Gunung Panderman", "4.8", 14000, "500 g", R.drawable.sayuran, "Sayuran", isEcoFriendly = true, deliveryDays = 3, protein = "0.9g", fat = "0.2g", carbs = "9.6g", calories = "41 Kcal", ingredients = "Wortel lokal manis organik ditanam langsung di lahan tinggi bebas pencemaran udara.")
        )
    }

    // Mock farmers list
    val allFarmers = remember {
        listOf(
            Farmer(1, "Koperasi Susu & Keju Pujon", "5.0", "12 km", "Keju, Susu, Mentega", R.drawable.sapi, "Pujon, Malang", description = "Koperasi susu terpercaya di wilayah Pujon. Kami mengelola ratusan sapi perah lokal secara berkelanjutan dan memproduksi produk susu segar organik harian."),
            Farmer(2, "Madu Hutan Batu & Herbal", "4.9", "24 km", "Madu, Jamu, Manisan", R.drawable.sayuran, "Bumiaji, Batu", description = "Komunitas peternak lebah madu liar yang berfokus melestarikan hutan lindung Batu. Menghasilkan madu hutan liar murni berkualitas tinggi."),
            Farmer(3, "Agro Makmur Sayur & Buah", "4.8", "15 km", "Sayur, Tomat, Wortel", R.drawable.sayuran, "Batu, Malang", description = "Pertanian hidroponik modern yang menyuplai berbagai sayur dan buah dataran tinggi segar organik bebas pestisida kimia."),
            Farmer(4, "Mitra Tani Padi Organik", "5.0", "32 km", "Beras Merah, Beras Putih", R.drawable.padi, "Cianjur, Jabar", description = "Kelompok tani tradisional yang melestarikan penanaman padi organik khas Cianjur menggunakan mata air pegunungan murni.")
        )
    }

    // Mock recipes list
    val allRecipes = remember {
        listOf(
            Recipe(1, "Omelet Sayur Segar Hijau", "5.0", "15 min", "Mudah", R.drawable.sayuran, listOf(1, 3)),
            Recipe(2, "Smoothie Susu Alpukat", "4.9", "10 min", "Mudah", R.drawable.sapi, listOf(7)),
            Recipe(3, "Casserole Kentang Keju", "5.0", "35 min", "Sedang", R.drawable.sapi, listOf(2)),
            Recipe(4, "Sup Bayam Kaldu Organik", "4.8", "20 min", "Mudah", R.drawable.padi, listOf(3, 5))
        )
    }

    val categories = listOf("Semua", "Telur", "Susu", "Sayuran", "Daging", "Bahan Sup")

    // Filtered products list applying categories, search queries, and filter parameters
    val filteredProducts = remember(
        searchQuery, selectedCategory, currentTab, favoriteProductIds,
        filterDeliveryDays, filterRegion, filterEcoFriendly, filterDiscountedOnly,
        filterSelectedDiets.size, filterSelectedAllergens.size, filterSelectedNutrients.size
    ) {
        allProducts.filter { product ->
            val matchesSearch = product.name.contains(searchQuery, ignoreCase = true) ||
                    product.farmer.contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedCategory == "Semua" || product.category == selectedCategory
            val matchesFavorites = currentTab != "favorites" || favoriteProductIds.contains(product.id)

            val matchesDelivery = filterDeliveryDays == 0 || product.deliveryDays <= filterDeliveryDays
            val matchesEco = !filterEcoFriendly || product.isEcoFriendly
            val matchesDiscount = !filterDiscountedOnly || product.isDiscounted

            val matchesDiet = filterSelectedDiets.isEmpty() || filterSelectedDiets.all { diet ->
                when (diet) {
                    "Vegetarian" -> product.category in listOf("Sayuran", "Susu", "Telur", "Bahan Sup")
                    "Vegan" -> product.category in listOf("Sayuran", "Bahan Sup")
                    "Keto" -> product.category in listOf("Daging", "Susu", "Telur")
                    else -> true
                }
            }

            val matchesAllergen = filterSelectedAllergens.isEmpty() || filterSelectedAllergens.all { allergen ->
                when (allergen) {
                    "Bebas Laktosa" -> product.category != "Susu"
                    "Bebas Gluten" -> product.category != "Telur"
                    else -> true
                }
            }

            val matchesNutrients = filterSelectedNutrients.isEmpty() || filterSelectedNutrients.all { nutrient ->
                when (nutrient) {
                    "Tinggi Kalsium" -> product.category == "Susu"
                    "Tinggi Protein" -> product.category in listOf("Daging", "Telur")
                    "Kaya Serat" -> product.category == "Sayuran"
                    else -> true
                }
            }

            matchesSearch && matchesCategory && matchesFavorites &&
                    matchesDelivery && matchesEco && matchesDiscount &&
                    matchesDiet && matchesAllergen && matchesNutrients
        }
    }

    Scaffold(
        bottomBar = {
            if (selectedProductForDetail == null && selectedFarmerForDetail == null) {
                BottomNavigationBar(
                    currentTab = currentTab,
                    onTabSelected = {
                        currentTab = it
                        isSearchFocused = false
                    }
                )
            }
        },
        containerColor = OnboardBgWarm
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header (Address, Notifications)
                HomeHeader(
                    address = "Jalan Sawah Indah No. 12",
                    onNotificationClick = {
                        Toast.makeText(context, "Tidak ada notifikasi baru", Toast.LENGTH_SHORT).show()
                    }
                )

                // Search Bar component with filter click action
                SearchBarComponent(
                    query = searchQuery,
                    onQueryChanged = { searchQuery = it },
                    isFocused = isSearchFocused,
                    onFocusChanged = { isSearchFocused = it },
                    onClearQuery = { searchQuery = "" },
                    onFilterClick = { isFilterSheetOpen = true }
                )

                // If search is focused or query is not empty, show the search results list
                if (isSearchFocused || searchQuery.isNotEmpty()) {
                    SearchFlowView(
                        categories = categories,
                        selectedCategory = selectedCategory,
                        onCategorySelected = { selectedCategory = it },
                        products = filteredProducts,
                        cartItems = cartItems,
                        favoriteProductIds = favoriteProductIds,
                        onAddToCart = { product ->
                            cartItems[product] = (cartItems[product] ?: 0) + 1
                        },
                        onRemoveFromCart = { product ->
                            val currentQty = cartItems[product] ?: 0
                            if (currentQty > 1) {
                                cartItems[product] = currentQty - 1
                            } else {
                                cartItems.remove(product)
                            }
                        },
                        onToggleFavorite = { id ->
                            if (favoriteProductIds.contains(id)) {
                                favoriteProductIds.remove(id)
                            } else {
                                favoriteProductIds.add(id)
                            }
                        },
                        onProductClick = { product ->
                            selectedProductForDetail = product
                            isSearchFocused = false
                        }
                    )
                } else {
                    // Normal tab switching layout
                    when (currentTab) {
                        "home" -> {
                            HomeMainDashboard(
                                products = allProducts.take(4),
                                cartItems = cartItems,
                                favoriteProductIds = favoriteProductIds,
                                onAddToCart = { product ->
                                    cartItems[product] = (cartItems[product] ?: 0) + 1
                                },
                                onRemoveFromCart = { product ->
                                    val currentQty = cartItems[product] ?: 0
                                    if (currentQty > 1) {
                                        cartItems[product] = currentQty - 1
                                    } else {
                                        cartItems.remove(product)
                                    }
                                },
                                onToggleFavorite = { id ->
                                    if (favoriteProductIds.contains(id)) {
                                        favoriteProductIds.remove(id)
                                    } else {
                                        favoriteProductIds.add(id)
                                    }
                                },
                                onNavigateToCatalog = {
                                    currentTab = "catalog"
                                    catalogSubTab = 0
                                    selectedCategory = "Semua"
                                },
                                onProductClick = { product ->
                                    selectedProductForDetail = product
                                }
                            )
                        }
                        "catalog" -> {
                            CatalogMainView(
                                subTab = catalogSubTab,
                                onSubTabChanged = { catalogSubTab = it },
                                categories = categories,
                                selectedCategory = selectedCategory,
                                onCategorySelected = { selectedCategory = it },
                                products = filteredProducts,
                                farmers = allFarmers,
                                recipes = allRecipes,
                                cartItems = cartItems,
                                favoriteProductIds = favoriteProductIds,
                                onAddToCart = { product ->
                                    cartItems[product] = (cartItems[product] ?: 0) + 1
                                },
                                onRemoveFromCart = { product ->
                                    val currentQty = cartItems[product] ?: 0
                                    if (currentQty > 1) {
                                        cartItems[product] = currentQty - 1
                                    } else {
                                        cartItems.remove(product)
                                    }
                                },
                                onToggleFavorite = { id ->
                                    if (favoriteProductIds.contains(id)) {
                                        favoriteProductIds.remove(id)
                                    } else {
                                        favoriteProductIds.add(id)
                                    }
                                },
                                onAddRecipeIngredients = { recipe ->
                                    recipe.ingredients.forEach { prodId ->
                                        val product = allProducts.firstOrNull { it.id == prodId }
                                        if (product != null) {
                                            cartItems[product] = (cartItems[product] ?: 0) + 1
                                        }
                                    }
                                    Toast.makeText(context, "Bahan-bahan resep ${recipe.name} ditambahkan!", Toast.LENGTH_SHORT).show()
                                },
                                onProductClick = { product ->
                                    selectedProductForDetail = product
                                },
                                onFarmerClick = { farmer ->
                                    selectedFarmerForDetail = farmer
                                }
                            )
                        }
                        "favorites" -> {
                            FavoritesView(
                                products = filteredProducts,
                                cartItems = cartItems,
                                favoriteProductIds = favoriteProductIds,
                                onAddToCart = { product ->
                                    cartItems[product] = (cartItems[product] ?: 0) + 1
                                },
                                onRemoveFromCart = { product ->
                                    val currentQty = cartItems[product] ?: 0
                                    if (currentQty > 1) {
                                        cartItems[product] = currentQty - 1
                                    } else {
                                        cartItems.remove(product)
                                    }
                                },
                                onToggleFavorite = { id ->
                                    favoriteProductIds.remove(id)
                                },
                                onProductClick = { product ->
                                    selectedProductForDetail = product
                                }
                            )
                        }
                        "profile" -> {
                            ProfileView(
                                onResetOnboarding = onResetOnboarding
                            )
                        }
                    }
                }
            }

            // Checkout Overlay Card (Cart Drawer at Bottom)
            val totalItemCount = cartItems.values.sum()
            val totalPrice = cartItems.entries.sumOf { it.key.price * it.value }

            AnimatedVisibility(
                visible = totalItemCount > 0 && selectedProductForDetail == null && selectedFarmerForDetail == null,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                CartBottomOverlay(
                    itemCount = totalItemCount,
                    totalPrice = totalPrice,
                    onCheckout = {
                        cartItems.clear()
                        Toast.makeText(context, "Pesanan Anda berhasil dikirim ke mitra tani!", Toast.LENGTH_LONG).show()
                    }
                )
            }

            // Fullscreen Filter Sheet Overlay (Mocking Image 4 layouts)
            AnimatedVisibility(
                visible = isFilterSheetOpen,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                FilterSheetView(
                    deliveryDays = filterDeliveryDays,
                    onDeliveryDaysChanged = { filterDeliveryDays = it },
                    region = filterRegion,
                    onRegionChanged = { filterRegion = it },
                    ecoFriendly = filterEcoFriendly,
                    onEcoFriendlyChanged = { filterEcoFriendly = it },
                    discountedOnly = filterDiscountedOnly,
                    onDiscountedOnlyChanged = { filterDiscountedOnly = it },
                    selectedDiets = filterSelectedDiets,
                    selectedAllergens = filterSelectedAllergens,
                    selectedNutrients = filterSelectedNutrients,
                    matchingProductsCount = filteredProducts.size,
                    onClose = { isFilterSheetOpen = false },
                    onReset = {
                        filterDeliveryDays = 0
                        filterRegion = "Semua"
                        filterEcoFriendly = false
                        filterDiscountedOnly = false
                        filterSelectedDiets.clear()
                        filterSelectedAllergens.clear()
                        filterSelectedNutrients.clear()
                    }
                )
            }

            // --- 14. Fullscreen Product Detail View (Mocking Image 5) ---
            AnimatedVisibility(
                visible = selectedProductForDetail != null,
                enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
                exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                selectedProductForDetail?.let { product ->
                    ProductDetailView(
                        product = product,
                        cartQuantity = cartItems[product] ?: 0,
                        isFavorite = favoriteProductIds.contains(product.id),
                        farmersList = allFarmers,
                        recipesList = allRecipes,
                        onBack = { selectedProductForDetail = null },
                        onToggleFavorite = {
                            if (favoriteProductIds.contains(product.id)) {
                                favoriteProductIds.remove(product.id)
                            } else {
                                favoriteProductIds.add(product.id)
                            }
                        },
                        onAddToCart = { cartItems[product] = (cartItems[product] ?: 0) + 1 },
                        onRemoveFromCart = {
                            val qty = cartItems[product] ?: 0
                            if (qty > 1) {
                                cartItems[product] = qty - 1
                            } else {
                                cartItems.remove(product)
                            }
                        },
                        onFarmerClick = { farmer ->
                            selectedFarmerForDetail = farmer
                        },
                        onAddRecipeIngredients = { recipe ->
                            recipe.ingredients.forEach { prodId ->
                                val p = allProducts.firstOrNull { it.id == prodId }
                                if (p != null) {
                                    cartItems[p] = (cartItems[p] ?: 0) + 1
                                }
                            }
                            Toast.makeText(context, "Bahan-bahan resep ${recipe.name} ditambahkan!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }

            // --- 15. Fullscreen Farmer Detail View (Mocking Image 5 Left) ---
            AnimatedVisibility(
                visible = selectedFarmerForDetail != null,
                enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
                exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                selectedFarmerForDetail?.let { farmer ->
                    FarmerDetailView(
                        farmer = farmer,
                        productsList = allProducts.filter { it.farmer.contains(farmer.name.take(15)) || it.farmer.contains("Pujon") },
                        cartItems = cartItems,
                        favoriteProductIds = favoriteProductIds,
                        onBack = { selectedFarmerForDetail = null },
                        onAddToCart = { prod -> cartItems[prod] = (cartItems[prod] ?: 0) + 1 },
                        onRemoveFromCart = { prod ->
                            val qty = cartItems[prod] ?: 0
                            if (qty > 1) {
                                cartItems[prod] = qty - 1
                            } else {
                                cartItems.remove(prod)
                            }
                        },
                        onToggleFavorite = { id ->
                            if (favoriteProductIds.contains(id)) favoriteProductIds.remove(id) else favoriteProductIds.add(id)
                        },
                        onProductClick = { prod ->
                            selectedProductForDetail = prod
                        }
                    )
                }
            }
        }
    }
}

// 1. Header (Address Selector and Notifications)
@Composable
fun HomeHeader(
    address: String,
    onNotificationClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = "Pin lokasi",
            tint = OnboardAccentTerracotta,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Alamat Pengiriman",
                color = OnboardTextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = address,
                color = OnboardTextDark,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
        IconButton(
            onClick = onNotificationClick,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(OnboardSurfaceWarm)
                .border(1.dp, OnboardBorderGrey, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = "Notifikasi",
                tint = OnboardTextDark,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

// 2. Search Bar Component with Filter Icon
@Composable
fun SearchBarComponent(
    query: String,
    onQueryChanged: (String) -> Unit,
    isFocused: Boolean,
    onFocusChanged: (Boolean) -> Unit,
    onClearQuery: () -> Unit,
    onFilterClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(50.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(OnboardSurfaceWarm)
                .border(1.dp, if (isFocused) OnboardPrimaryGreen else OnboardBorderGrey, RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = "Search",
                    tint = OnboardTextMuted,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (query.isEmpty()) {
                        Text(
                            text = "Cari sayur, buah, beras, susu...",
                            color = OnboardTextMuted.copy(alpha = 0.5f),
                            fontSize = 14.sp
                        )
                    }

                    BasicTextField(
                        value = query,
                        onValueChange = {
                            onQueryChanged(it)
                            onFocusChanged(true)
                        },
                        textStyle = TextStyle(
                            color = OnboardTextDark,
                            fontSize = 14.sp
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (query.isNotEmpty() || isFocused) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear",
                        tint = OnboardTextMuted,
                        modifier = Modifier
                            .size(18.dp)
                            .clickable {
                                onClearQuery()
                                onFocusChanged(false)
                            }
                    )
                }
            }
        }

        IconButton(
            onClick = onFilterClick,
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(OnboardSurfaceWarm)
                .border(1.dp, OnboardBorderGrey, RoundedCornerShape(12.dp))
        ) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Filter",
                tint = OnboardPrimaryGreen,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

// 3. Home Dashboard Main View with Product click support
@Composable
fun HomeMainDashboard(
    products: List<Product>,
    cartItems: Map<Product, Int>,
    favoriteProductIds: List<Int>,
    onAddToCart: (Product) -> Unit,
    onRemoveFromCart: (Product) -> Unit,
    onToggleFavorite: (Int) -> Unit,
    onNavigateToCatalog: () -> Unit,
    onProductClick: (Product) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Hero Grid Categories
        item {
            Spacer(modifier = Modifier.height(8.dp))
            GridCategoryCards(onNavigateToCatalog = onNavigateToCatalog)
        }

        // Section 1: Today's choice products grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pilihan Hari Ini",
                    color = OnboardTextDark,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Lihat Semua",
                    color = OnboardAccentTerracotta,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onNavigateToCatalog() }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2 Column product display
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                for (chunk in products.chunked(2)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        for (product in chunk) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onProductClick(product) }
                            ) {
                                ProductGridCard(
                                    product = product,
                                    isFavorite = favoriteProductIds.contains(product.id),
                                    cartQuantity = cartItems[product] ?: 0,
                                    onToggleFavorite = { onToggleFavorite(product.id) },
                                    onAddToCart = { onAddToCart(product) },
                                    onRemoveFromCart = { onRemoveFromCart(product) }
                                )
                            }
                        }
                        if (chunk.size < 2) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // Section 2: Banner Selection Recommendations
        item {
            Text(
                text = "Pilihan Hidangan Pangan",
                color = OnboardTextDark,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            RecommendedCategoryScroll()
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

// 4. Hero Grid cards component (Image 2 style)
@Composable
fun GridCategoryCards(onNavigateToCatalog: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(0.45f)
                    .height(140.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(OnboardAccentTerracotta)
                    .clickable { onNavigateToCatalog() }
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Beli\nLagi",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 20.sp
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Arrow",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Text(
                    text = "Bahan pangan terakhir dibeli",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 11.sp,
                    lineHeight = 14.sp
                )
            }

            Column(
                modifier = Modifier
                    .weight(0.55f)
                    .height(140.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(OnboardPrimaryGreen)
                    .clickable { onNavigateToCatalog() }
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Langsung dari\nMitra Tani",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 20.sp
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Verified",
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "100% Segar & Organik",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 11.sp
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(90.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(OnboardOliveDark)
                    .clickable { onNavigateToCatalog() }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(OnboardAccentTerracotta)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Promo Spesial",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Diskon Hari Ini s.d 30%",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Star",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

// 5. Product Grid Card Layout
@Composable
fun ProductGridCard(
    product: Product,
    isFavorite: Boolean,
    cartQuantity: Int,
    onToggleFavorite: () -> Unit,
    onAddToCart: () -> Unit,
    onRemoveFromCart: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = OnboardSurfaceWarm),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, OnboardBorderGrey),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(OnboardBgWarm),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = product.imageResId),
                    contentDescription = product.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.8f))
                        .clickable { onToggleFavorite() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) Color.Red else OnboardTextDark,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.White.copy(alpha = 0.8f))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Rating",
                        tint = OnboardAccentTerracotta,
                        modifier = Modifier.size(10.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = product.rating,
                        color = OnboardTextDark,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = product.name,
                color = OnboardTextDark,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )

            Text(
                text = product.farmer,
                color = OnboardTextMuted,
                fontSize = 10.sp,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    if (product.isDiscounted) {
                        Text(
                            text = "Rp ${product.originalPrice}",
                            color = OnboardTextMuted,
                            fontSize = 10.sp,
                            textDecoration = TextDecoration.LineThrough
                        )
                    }
                    Text(
                        text = "Rp ${product.price}",
                        color = OnboardAccentTerracotta,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "/ ${product.unit}",
                        color = OnboardTextMuted,
                        fontSize = 9.sp
                    )
                }

                if (cartQuantity > 0) {
                    Row(
                        modifier = Modifier
                            .height(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(OnboardBorderGrey.copy(alpha = 0.5f))
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .clickable { onRemoveFromCart() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Kurang",
                                tint = OnboardTextDark,
                                modifier = Modifier.size(12.dp)
                            )
                        }

                        Text(
                            text = "$cartQuantity pcs",
                            color = OnboardTextDark,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .clickable { onAddToCart() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Tambah",
                                tint = OnboardTextDark,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(OnboardPrimaryGreen)
                            .clickable { onAddToCart() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Add to cart",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

// 6. Recommended categories Horizontal Scroll List
@Composable
fun RecommendedCategoryScroll() {
    val items = listOf(
        Pair("Untuk Sarapan", R.drawable.sapi),
        Pair("Makan Siang", R.drawable.sayuran),
        Pair("Bahan Sup Hangat", R.drawable.padi),
        Pair("Camilan Sehat", R.drawable.sayuran)
    )

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items) { item ->
            Box(
                modifier = Modifier
                    .size(width = 120.dp, height = 75.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(OnboardSurfaceWarm)
                    .border(1.dp, OnboardBorderGrey, RoundedCornerShape(12.dp))
            ) {
                Image(
                    painter = painterResource(id = item.second),
                    contentDescription = item.first,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    alpha = 0.5f
                )
                Text(
                    text = item.first,
                    color = OnboardTextDark,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp),
                    textAlign = TextAlign.Start
                )
            }
        }
    }
}

// 7. Search View Flow supporting product click
@Composable
fun SearchFlowView(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    products: List<Product>,
    cartItems: Map<Product, Int>,
    favoriteProductIds: List<Int>,
    onAddToCart: (Product) -> Unit,
    onRemoveFromCart: (Product) -> Unit,
    onToggleFavorite: (Int) -> Unit,
    onProductClick: (Product) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        LazyRow(
            modifier = Modifier.padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                val isSelected = category == selectedCategory
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) OnboardPrimaryGreen else OnboardSurfaceWarm)
                        .border(1.dp, OnboardBorderGrey, RoundedCornerShape(20.dp))
                        .clickable { onCategorySelected(category) }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = category,
                        color = if (isSelected) Color.White else OnboardTextDark,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (products.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Tidak ada produk",
                    tint = OnboardTextMuted,
                    modifier = Modifier.size(72.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Sayang sekali, produk tidak ditemukan",
                    color = OnboardTextDark,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Coba gunakan kata kunci pencarian yang lain.",
                    color = OnboardTextMuted,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(products) { product ->
                    Box(modifier = Modifier.clickable { onProductClick(product) }) {
                        ProductGridCard(
                            product = product,
                            isFavorite = favoriteProductIds.contains(product.id),
                            cartQuantity = cartItems[product] ?: 0,
                            onToggleFavorite = { onToggleFavorite(product.id) },
                            onAddToCart = { onAddToCart(product) },
                            onRemoveFromCart = { onRemoveFromCart(product) }
                        )
                    }
                }
            }
        }
    }
}

// 8. Catalog Main View supporting clicks on products & farmers
@Composable
fun CatalogMainView(
    subTab: Int,
    onSubTabChanged: (Int) -> Unit,
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    products: List<Product>,
    farmers: List<Farmer>,
    recipes: List<Recipe>,
    cartItems: Map<Product, Int>,
    favoriteProductIds: List<Int>,
    onAddToCart: (Product) -> Unit,
    onRemoveFromCart: (Product) -> Unit,
    onToggleFavorite: (Int) -> Unit,
    onAddRecipeIngredients: (Recipe) -> Unit,
    onProductClick: (Product) -> Unit,
    onFarmerClick: (Farmer) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = subTab,
            containerColor = OnboardBgWarm,
            contentColor = OnboardPrimaryGreen,
            divider = { HorizontalDivider(color = OnboardBorderGrey) }
        ) {
            val headers = listOf("Produk", "Petani", "Resep")
            headers.forEachIndexed { index, title ->
                Tab(
                    selected = subTab == index,
                    onClick = { onSubTabChanged(index) },
                    text = {
                        Text(
                            text = title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (subTab == index) OnboardPrimaryGreen else OnboardTextMuted
                        )
                    }
                )
            }
        }

        when (subTab) {
            0 -> {
                SearchFlowView(
                    categories = categories,
                    selectedCategory = selectedCategory,
                    onCategorySelected = onCategorySelected,
                    products = products,
                    cartItems = cartItems,
                    favoriteProductIds = favoriteProductIds,
                    onAddToCart = onAddToCart,
                    onRemoveFromCart = onRemoveFromCart,
                    onToggleFavorite = onToggleFavorite,
                    onProductClick = onProductClick
                )
            }
            1 -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(farmers) { farmer ->
                        Box(modifier = Modifier.clickable { onFarmerClick(farmer) }) {
                            FarmerListCard(farmer = farmer)
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
            2 -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    items(recipes) { recipe ->
                        RecipeGridCard(
                            recipe = recipe,
                            onAddIngredients = { onAddRecipeIngredients(recipe) }
                        )
                    }
                }
            }
        }
    }
}

// 8b. Farmer List Card component
@Composable
fun FarmerListCard(farmer: Farmer) {
    Card(
        colors = CardDefaults.cardColors(containerColor = OnboardSurfaceWarm),
        border = BorderStroke(1.dp, OnboardBorderGrey),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(OnboardBgWarm),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = farmer.imageResId),
                    contentDescription = farmer.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = farmer.name,
                        color = OnboardTextDark,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = OnboardAccentTerracotta,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = farmer.rating,
                            color = OnboardTextDark,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = farmer.location,
                    color = OnboardTextMuted,
                    fontSize = 11.sp
                )

                Text(
                    text = "${farmer.distance} dari lokasi Anda",
                    color = OnboardAccentTerracotta,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    farmer.productsList.split(",").take(3).forEach { tag ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(OnboardOliveLight)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = tag.trim(),
                                color = OnboardTextMuted,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

// 8c. Recipe Grid Card component
@Composable
fun RecipeGridCard(
    recipe: Recipe,
    onAddIngredients: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = OnboardSurfaceWarm),
        border = BorderStroke(1.dp, OnboardBorderGrey),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(OnboardBgWarm)
            ) {
                Image(
                    painter = painterResource(id = recipe.imageResId),
                    contentDescription = recipe.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.White.copy(alpha = 0.8f))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Time",
                        tint = OnboardTextMuted,
                        modifier = Modifier.size(10.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = recipe.time,
                        color = OnboardTextDark,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = recipe.name,
                color = OnboardTextDark,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${recipe.difficulty} • ★ ${recipe.rating}",
                    color = OnboardTextMuted,
                    fontSize = 10.sp
                )

                IconButton(
                    onClick = onAddIngredients,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(OnboardPrimaryGreen)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Tambah Bahan",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

// 9. Favorites View Tab supporting click
@Composable
fun FavoritesView(
    products: List<Product>,
    cartItems: Map<Product, Int>,
    favoriteProductIds: List<Int>,
    onAddToCart: (Product) -> Unit,
    onRemoveFromCart: (Product) -> Unit,
    onToggleFavorite: (Int) -> Unit,
    onProductClick: (Product) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Text(
            text = "Produk Favorit Anda",
            color = OnboardTextDark,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 12.dp)
        )

        if (products.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Kosong",
                    tint = OnboardBorderGrey,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Belum ada produk favorit",
                    color = OnboardTextDark,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Klik ikon hati pada produk untuk menyimpannya di sini.",
                    color = OnboardTextMuted,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(products) { product ->
                    Box(modifier = Modifier.clickable { onProductClick(product) }) {
                        ProductGridCard(
                            product = product,
                            isFavorite = true,
                            cartQuantity = cartItems[product] ?: 0,
                            onToggleFavorite = { onToggleFavorite(product.id) },
                            onAddToCart = { onAddToCart(product) },
                            onRemoveFromCart = { onRemoveFromCart(product) }
                        )
                    }
                }
            }
        }
    }
}

// 10. Profile View Tab
@Composable
fun ProfileView(
    onResetOnboarding: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(OnboardSurfaceWarm)
                .border(2.dp, OnboardPrimaryGreen, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile Avatar",
                tint = OnboardPrimaryGreen,
                modifier = Modifier.size(56.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Ricky Prakusa",
            color = OnboardTextDark,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "ricky@gmail.com | +62 812-3456-7890",
            color = OnboardTextMuted,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(40.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = OnboardSurfaceWarm),
            border = BorderStroke(1.dp, OnboardBorderGrey),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Pengaturan Akun",
                    color = OnboardTextDark,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Bahasa", color = OnboardTextMuted, fontSize = 13.sp)
                    Text(text = "Indonesia", color = OnboardTextDark, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Mode Aplikasi", color = OnboardTextMuted, fontSize = 13.sp)
                    Text(text = "Pembeli", color = OnboardPrimaryGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onResetOnboarding,
            colors = ButtonDefaults.buttonColors(containerColor = OnboardAccentTerracotta),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Reset Slide Onboarding", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

// 11. Bottom Overlay Card: "Keranjang" Drawer (Image 2 style)
@Composable
fun CartBottomOverlay(
    itemCount: Int,
    totalPrice: Int,
    onCheckout: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = OnboardPrimaryGreen),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Free Shipping",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Gratis ongkir untuk transaksi hari ini!",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Keranjang Belanja",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 11.sp
                    )
                    Text(
                        text = "Rp $totalPrice ($itemCount Item)",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = onCheckout,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "Bayar Sekarang",
                        color = OnboardPrimaryGreen,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// 12. Bottom Navigation Bar Component
@Composable
fun BottomNavigationBar(
    currentTab: String,
    onTabSelected: (String) -> Unit
) {
    NavigationBar(
        containerColor = OnboardSurfaceWarm,
        tonalElevation = 8.dp
    ) {
        val tabs = listOf<Triple<String, String, ImageVector>>(
            Triple("home", "Beranda", Icons.Default.Home),
            Triple("catalog", "Katalog", Icons.Default.List),
            Triple("favorites", "Favorit", Icons.Default.Favorite),
            Triple("profile", "Profil", Icons.Default.Person)
        )

        for (tab in tabs) {
            val isSelected = currentTab == tab.first
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab.first) },
                icon = {
                    Icon(
                        imageVector = tab.third,
                        contentDescription = tab.second,
                        tint = if (isSelected) OnboardPrimaryGreen else OnboardTextMuted
                    )
                },
                label = {
                    Text(
                        text = tab.second,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) OnboardPrimaryGreen else OnboardTextMuted
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = OnboardOliveLight
                )
            )
        }
    }
}

// --- 13. Fullscreen Filter Sheet View ---
@Composable
fun FilterSheetView(
    deliveryDays: Int,
    onDeliveryDaysChanged: (Int) -> Unit,
    region: String,
    onRegionChanged: (String) -> Unit,
    ecoFriendly: Boolean,
    onEcoFriendlyChanged: (Boolean) -> Unit,
    discountedOnly: Boolean,
    onDiscountedOnlyChanged: (Boolean) -> Unit,
    selectedDiets: MutableList<String>,
    selectedAllergens: MutableList<String>,
    selectedNutrients: MutableList<String>,
    matchingProductsCount: Int,
    onClose: () -> Unit,
    onReset: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OnboardBgWarm)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Kembali",
                        tint = OnboardTextDark
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Filter",
                    color = OnboardTextDark,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "Bersihkan",
                color = OnboardTextMuted,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onReset() }
            )
        }

        val hasParameters = deliveryDays > 0 || region != "Semua" || ecoFriendly || discountedOnly ||
                selectedDiets.isNotEmpty() || selectedAllergens.isNotEmpty() || selectedNutrients.isNotEmpty()

        if (hasParameters) {
            Text(
                text = "Parameter Terpilih",
                color = OnboardTextDark,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )

            LazyRow(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (deliveryDays > 0) {
                    item {
                        ParameterChip(
                            text = when (deliveryDays) {
                                1 -> "Hari Ini"
                                2 -> "Besok"
                                else -> "s.d $deliveryDays Hari"
                            },
                            onDismiss = { onDeliveryDaysChanged(0) }
                        )
                    }
                }
                if (region != "Semua") {
                    item { ParameterChip(text = region, onDismiss = { onRegionChanged("Semua") }) }
                }
                if (ecoFriendly) {
                    item { ParameterChip(text = "Eco-friendly", onDismiss = { onEcoFriendlyChanged(false) }) }
                }
                if (discountedOnly) {
                    item { ParameterChip(text = "Diskon", onDismiss = { onDiscountedOnlyChanged(false) }) }
                }
                items(selectedDiets) { diet ->
                    ParameterChip(text = diet, onDismiss = { selectedDiets.remove(diet) })
                }
                items(selectedAllergens) { allergen ->
                    ParameterChip(text = allergen, onDismiss = { selectedAllergens.remove(allergen) })
                }
                items(selectedNutrients) { nutrient ->
                    ParameterChip(text = nutrient, onDismiss = { selectedNutrients.remove(nutrient) })
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Column {
                Text(
                    text = "Waktu Pengiriman",
                    color = OnboardTextDark,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf(
                        Pair(1, "Hari Ini"),
                        Pair(2, "Besok"),
                        Pair(3, "s.d 3 Hari"),
                        Pair(5, "s.d 5 Hari")
                    ).forEach { (days, label) ->
                        val isSelected = deliveryDays == days
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) OnboardPrimaryGreen else OnboardSurfaceWarm)
                                .border(1.dp, OnboardBorderGrey, RoundedCornerShape(20.dp))
                                .clickable { onDeliveryDaysChanged(if (isSelected) 0 else days) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) Color.White else OnboardTextDark,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Column {
                Text(
                    text = "Wilayah Produksi",
                    color = OnboardTextDark,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Malang Raya", "Terdekat").forEach { label ->
                        val isSelected = region == label
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) OnboardPrimaryGreen else OnboardSurfaceWarm)
                                .border(1.dp, OnboardBorderGrey, RoundedCornerShape(20.dp))
                                .clickable { onRegionChanged(if (isSelected) "Semua" else label) }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) Color.White else OnboardTextDark,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Kemasan Ramah Lingkungan",
                            color = OnboardTextDark,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Gunakan paperbag / bio-degradable bag",
                            color = OnboardTextMuted,
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = ecoFriendly,
                        onCheckedChange = onEcoFriendlyChanged,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = OnboardPrimaryGreen,
                            uncheckedThumbColor = OnboardTextMuted,
                            uncheckedTrackColor = OnboardSurfaceWarm
                        )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Hanya Produk Diskon",
                            color = OnboardTextDark,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tampilkan komoditas berlabel coret saja",
                            color = OnboardTextMuted,
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = discountedOnly,
                        onCheckedChange = onDiscountedOnlyChanged,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = OnboardPrimaryGreen,
                            uncheckedThumbColor = OnboardTextMuted,
                            uncheckedTrackColor = OnboardSurfaceWarm
                        )
                    )
                }
            }

            Column {
                Text(
                    text = "Nutrisi & Kesehatan",
                    color = OnboardTextDark,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                Text(
                    text = "Program Diet",
                    color = OnboardTextMuted,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    listOf("Vegetarian", "Vegan", "Keto").forEach { diet ->
                        val isSelected = selectedDiets.contains(diet)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) OnboardPrimaryGreen else OnboardSurfaceWarm)
                                .border(1.dp, OnboardBorderGrey, RoundedCornerShape(20.dp))
                                .clickable {
                                    if (isSelected) selectedDiets.remove(diet) else selectedDiets.add(diet)
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = diet,
                                color = if (isSelected) Color.White else OnboardTextDark,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Text(
                    text = "Kandungan / Bebas Alergen",
                    color = OnboardTextMuted,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    listOf("Bebas Laktosa", "Bebas Gluten").forEach { allergen ->
                        val isSelected = selectedAllergens.contains(allergen)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) OnboardPrimaryGreen else OnboardSurfaceWarm)
                                .border(1.dp, OnboardBorderGrey, RoundedCornerShape(20.dp))
                                .clickable {
                                    if (isSelected) selectedAllergens.remove(allergen) else selectedAllergens.add(allergen)
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = allergen,
                                color = if (isSelected) Color.White else OnboardTextDark,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Text(
                    text = "Mikroelemen & Nutrisi",
                    color = OnboardTextMuted,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Tinggi Kalsium", "Tinggi Protein", "Kaya Serat").forEach { nutrient ->
                        val isSelected = selectedNutrients.contains(nutrient)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) OnboardPrimaryGreen else OnboardSurfaceWarm)
                                .border(1.dp, OnboardBorderGrey, RoundedCornerShape(20.dp))
                                .clickable {
                                    if (isSelected) selectedNutrients.remove(nutrient) else selectedNutrients.add(nutrient)
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = nutrient,
                                color = if (isSelected) Color.White else OnboardTextDark,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Button(
                onClick = onClose,
                colors = ButtonDefaults.buttonColors(containerColor = OnboardPrimaryGreen),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Text(
                    text = "Tampilkan $matchingProductsCount Produk",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ParameterChip(
    text: String,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(OnboardOliveLight)
            .border(1.dp, OnboardBorderGrey, RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = text,
                color = OnboardTextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Icon(
                imageVector = Icons.Default.Clear,
                contentDescription = "Dismiss",
                tint = OnboardTextMuted,
                modifier = Modifier
                    .size(12.dp)
                    .clickable { onDismiss() }
            )
        }
    }
}

// --- 14. Fullscreen Product Detail View (Mocking Image 5) ---
@Composable
fun ProductDetailView(
    product: Product,
    cartQuantity: Int,
    isFavorite: Boolean,
    farmersList: List<Farmer>,
    recipesList: List<Recipe>,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
    onAddToCart: () -> Unit,
    onRemoveFromCart: () -> Unit,
    onFarmerClick: (Farmer) -> Unit,
    onAddRecipeIngredients: (Recipe) -> Unit
) {
    val scrollState = rememberScrollState()
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Tentang Produk, 1 = Ulasan, 2 = Resep Terkait

    val matchingFarmer = remember(product) {
        farmersList.firstOrNull { it.name.contains(product.farmer.split(",").first().trim()) || it.name.contains("Pujon") } ?: farmersList.first()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OnboardBgWarm)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Custom Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(OnboardSurfaceWarm)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = OnboardTextDark)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                IconButton(
                    onClick = { /* share mock */ },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(OnboardSurfaceWarm)
                ) {
                    Icon(Icons.Outlined.Share, contentDescription = "Bagikan", tint = OnboardTextDark)
                }

                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(OnboardSurfaceWarm)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorit",
                        tint = if (isFavorite) Color.Red else OnboardTextDark
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .background(OnboardSurfaceWarm),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = product.imageResId),
                    contentDescription = product.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (product.isDiscounted) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(OnboardAccentTerracotta)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text("Diskon", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(OnboardPrimaryGreen)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text("Baru", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Star, contentDescription = "Star", tint = OnboardAccentTerracotta, modifier = Modifier.size(16.dp))
                        Text(text = product.rating, color = OnboardTextDark, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = product.name,
                    color = OnboardTextDark,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 26.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "Rp ${product.price}",
                            color = OnboardAccentTerracotta,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (product.isDiscounted) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Rp ${product.originalPrice}",
                                color = OnboardTextMuted,
                                fontSize = 14.sp,
                                textDecoration = TextDecoration.LineThrough
                            )
                        }
                        Text(
                            text = " / ${product.unit}",
                            color = OnboardTextMuted,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 3.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(OnboardOliveLight)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Estimasi Kirim: ${if (product.deliveryDays == 1) "Hari Ini" else "Besok"}",
                            color = OnboardPrimaryGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = product.ingredients,
                    color = OnboardTextMuted,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = OnboardBgWarm,
                contentColor = OnboardPrimaryGreen,
                divider = { HorizontalDivider(color = OnboardBorderGrey) }
            ) {
                val tabs = listOf("Detail Produk", "Ulasan (15)", "Resep")
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedTab == index) OnboardPrimaryGreen else OnboardTextMuted
                            )
                        }
                    )
                }
            }

            when (selectedTab) {
                0 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("Kandungan Nutrisi (per 100g):", color = OnboardTextDark, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            listOf(
                                Pair(product.protein, "Protein"),
                                Pair(product.fat, "Lemak"),
                                Pair(product.carbs, "Karbohidrat"),
                                Pair(product.calories, "Energi")
                            ).forEach { (value, label) ->
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(OnboardSurfaceWarm)
                                        .padding(vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(text = value, color = OnboardTextDark, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text(text = label, color = OnboardTextMuted, fontSize = 9.sp)
                                }
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            SpecRow("Masa Simpan", product.shelfLife)
                            SpecRow("Penyimpanan", product.storage)
                            SpecRow("Kemasan", product.packaging)
                        }

                        Column {
                            Text("Karakteristik & Nutrisi:", color = OnboardTextDark, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            FlowRow(horizontalGap = 8.dp, verticalGap = 8.dp) {
                                (product.diets + product.allergens + product.nutrients).forEach { tag ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(OnboardOliveLight)
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(text = tag, color = OnboardTextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        Text("Produsen Mitra Tani:", color = OnboardTextDark, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Box(modifier = Modifier.clickable { onFarmerClick(matchingFarmer) }) {
                            FarmerListCard(farmer = matchingFarmer)
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = OnboardSurfaceWarm),
                            border = BorderStroke(1.dp, OnboardBorderGrey),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { /* certs detail */ }
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = "Cert", tint = OnboardPrimaryGreen, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text("Sertifikat Kualitas Produk", color = OnboardTextDark, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                                Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Next", tint = OnboardTextMuted)
                            }
                        }
                    }
                }
                1 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = "4.9", color = OnboardTextDark, fontSize = 32.sp, fontWeight = FontWeight.Black)
                            Column {
                                Row {
                                    repeat(5) { Icon(Icons.Default.Star, contentDescription = "Star", tint = OnboardAccentTerracotta, modifier = Modifier.size(14.dp)) }
                                }
                                Text("Berdasarkan 15 Ulasan Pembeli", color = OnboardTextMuted, fontSize = 11.sp)
                            }
                        }

                        HorizontalDivider(color = OnboardBorderGrey)

                        val reviews = listOf(
                            Triple("Amir Santoso", "Susu kambing segar ini mantap sekali rasanya, gurih dan kemasannya botol kaca tebal steril. Recomended!", R.drawable.sapi),
                            Triple("Desi Ratnasari", "Pengirimannya super cepat, masih dingin saat sampai. Sangat higienis bagi kesehatan keluarga.", R.drawable.sapi),
                            Triple("Edi Wijaya", "Biasa buat langganan sarapan pagi anak-anak. Bagus kualitas Pujon.", R.drawable.sapi)
                        )

                        reviews.forEach { (name, review, imgId) ->
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = name, color = OnboardTextDark, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Row {
                                        repeat(5) { Icon(Icons.Default.Star, contentDescription = "*", tint = OnboardAccentTerracotta, modifier = Modifier.size(10.dp)) }
                                    }
                                }
                                Text(text = review, color = OnboardTextMuted, fontSize = 12.sp, lineHeight = 16.sp)

                                Image(
                                    painter = painterResource(id = imgId),
                                    contentDescription = "Review attachment",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                )

                                Spacer(modifier = Modifier.height(8.dp))
                                HorizontalDivider(color = OnboardBorderGrey.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
                2 -> {
                    val matchingRecipes = recipesList.filter { it.ingredients.contains(product.id) }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("Daftar Resep Berbahan Ini:", color = OnboardTextDark, fontSize = 14.sp, fontWeight = FontWeight.Bold)

                        if (matchingRecipes.isEmpty()) {
                            Text("Belum ada resep khusus untuk produk ini.", color = OnboardTextMuted, fontSize = 12.sp)
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier
                                    .height(280.dp)
                                    .fillMaxWidth()
                            ) {
                                items(matchingRecipes) { recipe ->
                                    RecipeGridCard(
                                        recipe = recipe,
                                        onAddIngredients = { onAddRecipeIngredients(recipe) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = OnboardSurfaceWarm),
            elevation = CardDefaults.cardElevation(8.dp),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Total Harga:", color = OnboardTextMuted, fontSize = 11.sp)
                    Text(
                        text = "Rp ${product.price * if (cartQuantity > 0) cartQuantity else 1}",
                        color = OnboardAccentTerracotta,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (cartQuantity > 0) {
                    Row(
                        modifier = Modifier
                            .height(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(OnboardPrimaryGreen)
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        IconButton(onClick = onRemoveFromCart, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Remove, contentDescription = "Kurang", tint = Color.White)
                        }
                        Text(
                            text = "$cartQuantity Pcs",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = onAddToCart, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Add, contentDescription = "Tambah", tint = Color.White)
                        }
                    }
                } else {
                    Button(
                        onClick = onAddToCart,
                        colors = ButtonDefaults.buttonColors(containerColor = OnboardPrimaryGreen),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Tambah ke Keranjang", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// Spec row helper
@Composable
fun SpecRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = OnboardTextMuted, fontSize = 12.sp)
        Text(
            text = value,
            color = OnboardTextDark,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.widthIn(max = 220.dp),
            textAlign = TextAlign.End
        )
    }
}

// FlowRow layout helper for tags wrapping
@Composable
fun FlowRow(
    horizontalGap: androidx.compose.ui.unit.Dp,
    verticalGap: androidx.compose.ui.unit.Dp,
    content: @Composable () -> Unit
) {
    androidx.compose.ui.layout.Layout(content = content) { measurables, constraints ->
        val placeables = measurables.map { it.measure(constraints) }
        val layoutWidth = constraints.maxWidth
        var currentX = 0
        var currentY = 0
        var maxHeightInLine = 0
        val positions = mutableListOf<Pair<Int, Int>>()

        placeables.forEach { placeable ->
            if (currentX + placeable.width > layoutWidth) {
                currentX = 0
                currentY += maxHeightInLine + verticalGap.roundToPx()
                maxHeightInLine = 0
            }
            positions.add(Pair(currentX, currentY))
            maxHeightInLine = maxOf(maxHeightInLine, placeable.height)
            currentX += placeable.width + horizontalGap.roundToPx()
        }

        layout(
            width = layoutWidth,
            height = if (positions.isEmpty()) 0 else (positions.last().second + maxHeightInLine)
        ) {
            placeables.forEachIndexed { index, placeable ->
                val (x, y) = positions[index]
                placeable.placeRelative(x, y)
            }
        }
    }
}

// --- 15. Fullscreen Farmer Detail View (Mocking Image 5 Left) ---
@Composable
fun FarmerDetailView(
    farmer: Farmer,
    productsList: List<Product>,
    cartItems: Map<Product, Int>,
    favoriteProductIds: List<Int>,
    onBack: () -> Unit,
    onAddToCart: (Product) -> Unit,
    onRemoveFromCart: (Product) -> Unit,
    onToggleFavorite: (Int) -> Unit,
    onProductClick: (Product) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Produk, 1 = Sertifikasi, 2 = Ulasan

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OnboardBgWarm)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(OnboardSurfaceWarm)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = OnboardTextDark)
            }

            Text(
                text = "Profil Produsen",
                color = OnboardTextDark,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            IconButton(
                onClick = { /* share */ },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(OnboardSurfaceWarm)
            ) {
                Icon(Icons.Outlined.Share, contentDescription = "Bagikan", tint = OnboardTextDark)
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(OnboardSurfaceWarm),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = farmer.imageResId),
                        contentDescription = farmer.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            item {
                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(OnboardPrimaryGreen)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text("Mitra Utama", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(OnboardOliveDark)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text("Organik", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = "★", tint = OnboardAccentTerracotta, modifier = Modifier.size(16.dp))
                            Text(text = farmer.rating, color = OnboardTextDark, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = farmer.name,
                        color = OnboardTextDark,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = farmer.description,
                        color = OnboardTextMuted,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = "Loc", tint = OnboardAccentTerracotta, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${farmer.location} (${farmer.distance} dari lokasi Anda)",
                            color = OnboardTextDark,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            item {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = OnboardBgWarm,
                    contentColor = OnboardPrimaryGreen,
                    divider = { HorizontalDivider(color = OnboardBorderGrey) }
                ) {
                    val tabs = listOf("Produk", "Sertifikasi", "Ulasan")
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedTab == index) OnboardPrimaryGreen else OnboardTextMuted
                                )
                            }
                        )
                    }
                }
            }

            item {
                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    when (selectedTab) {
                        0 -> {
                            if (productsList.isEmpty()) {
                                Text("Belum ada produk terdaftar untuk mitra ini.", color = OnboardTextMuted, fontSize = 12.sp)
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                    for (chunk in productsList.chunked(2)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                            for (product in chunk) {
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clickable { onProductClick(product) }
                                                ) {
                                                    ProductGridCard(
                                                        product = product,
                                                        isFavorite = favoriteProductIds.contains(product.id),
                                                        cartQuantity = cartItems[product] ?: 0,
                                                        onToggleFavorite = { onToggleFavorite(product.id) },
                                                        onAddToCart = { onAddToCart(product) },
                                                        onRemoveFromCart = { onRemoveFromCart(product) }
                                                    )
                                                }
                                            }
                                            if (chunk.size < 2) {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        1 -> {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                farmer.certs.forEach { cert ->
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = OnboardSurfaceWarm),
                                        border = BorderStroke(1.dp, OnboardBorderGrey),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = "Cert", tint = OnboardPrimaryGreen, modifier = Modifier.size(20.dp))
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(text = cert, color = OnboardTextDark, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                        2 -> {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                val farmReviews = listOf(
                                    Pair("Budi Santoso", "Susu pasteurisasi dari Pujon ini selalu konsisten mutunya. Segar sekali!"),
                                    Pair("Hartono Malang", "Mitra tani terpercaya, hasil buminya memuaskan untuk konsumsi harian keluarga.")
                                )
                                farmReviews.forEach { (reviewer, text) ->
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(text = reviewer, color = OnboardTextDark, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                            Row {
                                                repeat(5) { Icon(Icons.Default.Star, contentDescription = "*", tint = OnboardAccentTerracotta, modifier = Modifier.size(10.dp)) }
                                            }
                                        }
                                        Text(text = text, color = OnboardTextMuted, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                                        Spacer(modifier = Modifier.height(8.dp))
                                        HorizontalDivider(color = OnboardBorderGrey.copy(alpha = 0.5f))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}
