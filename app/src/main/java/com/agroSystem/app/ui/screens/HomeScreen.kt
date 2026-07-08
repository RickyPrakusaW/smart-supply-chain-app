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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Search
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
    val originalPrice: Int = 0
)

data class Farmer(
    val id: Int,
    val name: String,
    val rating: String,
    val distance: String,
    val productsList: String,
    val imageResId: Int,
    val location: String
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

    // Raw mock product list using existing drawable resources
    val allProducts = remember {
        listOf(
            Product(1, "Telur Ayam Kampung Segar", "Peternakan Tani Jaya, Malang", "5.0", 24000, "10 pcs", R.drawable.padi, "Telur"),
            Product(2, "Keju Kambing Organik", "Koperasi Susu Pujon, Batu", "4.9", 45000, "200 g", R.drawable.sapi, "Susu", isDiscounted = true, originalPrice = 50000),
            Product(3, "Bayam Hidroponik Bersih", "Agro Makmur, Batu", "4.8", 12000, "250 g", R.drawable.sayuran, "Sayuran"),
            Product(4, "Daging Sapi Potong Premium", "Peternakan Singosari, Malang", "5.0", 95000, "500 g", R.drawable.sapi, "Daging"),
            Product(5, "Beras Merah Organik Cianjur", "Mitra Tani Sejahtera", "4.9", 35000, "1 kg", R.drawable.padi, "Bahan Sup"),
            Product(6, "Tomat Beef Hidroponik", "Agro Makmur, Batu", "4.7", 15000, "500 g", R.drawable.sayuran, "Sayuran", isDiscounted = true, originalPrice = 18000),
            Product(7, "Susu Sapi Murni Pasteurisasi", "Peternakan Pujon, Batu", "4.9", 18000, "1 L", R.drawable.sapi, "Susu"),
            Product(8, "Wortel Manis Organik", "Kaki Gunung Panderman", "4.8", 14000, "500 g", R.drawable.sayuran, "Sayuran")
        )
    }

    // Mock farmers list
    val allFarmers = remember {
        listOf(
            Farmer(1, "Koperasi Susu & Keju Pujon", "5.0", "12 km", "Keju, Susu, Mentega", R.drawable.sapi, "Pujon, Malang"),
            Farmer(2, "Madu Hutan Batu & Herbal", "4.9", "24 km", "Madu, Jamu, Manisan", R.drawable.sayuran, "Bumiaji, Batu"),
            Farmer(3, "Agro Makmur Sayur & Buah", "4.8", "15 km", "Sayur, Tomat, Wortel", R.drawable.sayuran, "Batu, Malang"),
            Farmer(4, "Mitra Tani Padi Organik", "5.0", "32 km", "Beras Merah, Beras Putih", R.drawable.padi, "Cianjur, Jabar")
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

    // Filtered products list
    val filteredProducts = remember(searchQuery, selectedCategory, currentTab, favoriteProductIds) {
        allProducts.filter { product ->
            val matchesSearch = product.name.contains(searchQuery, ignoreCase = true) ||
                    product.farmer.contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedCategory == "Semua" || product.category == selectedCategory
            val matchesFavorites = currentTab != "favorites" || favoriteProductIds.contains(product.id)
            matchesSearch && matchesCategory && matchesFavorites
        }
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                currentTab = currentTab,
                onTabSelected = {
                    currentTab = it
                    isSearchFocused = false
                }
            )
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

                // Search Bar component
                SearchBarComponent(
                    query = searchQuery,
                    onQueryChanged = { searchQuery = it },
                    isFocused = isSearchFocused,
                    onFocusChanged = { isSearchFocused = it },
                    onClearQuery = { searchQuery = "" }
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
                visible = totalItemCount > 0,
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

// 2. Search Bar Component
@Composable
fun SearchBarComponent(
    query: String,
    onQueryChanged: (String) -> Unit,
    isFocused: Boolean,
    onFocusChanged: (Boolean) -> Unit,
    onClearQuery: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
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
}

// 3. Home Dashboard Main View
@Composable
fun HomeMainDashboard(
    products: List<Product>,
    cartItems: Map<Product, Int>,
    favoriteProductIds: List<Int>,
    onAddToCart: (Product) -> Unit,
    onRemoveFromCart: (Product) -> Unit,
    onToggleFavorite: (Int) -> Unit,
    onNavigateToCatalog: () -> Unit
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
                            Box(modifier = Modifier.weight(1f)) {
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
            Spacer(modifier = Modifier.height(80.dp)) // space for cart overlay
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
            // Card 1: Beli Lagi (Terracotta)
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

            // Card 2: Langsung dari Mitra Tani (Dark Green)
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
            // Card 3: Promo Hari Ini (Olive Green)
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

// 5. Product Grid Card Layout (Includes Add/Subtract quantity selector on card)
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
            // Image frame container
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

                // Favorite (heart) button at top-right
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

                // Star rating at bottom-left
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

            // Title
            Text(
                text = product.name,
                color = OnboardTextDark,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )

            // Farmer provider label
            Text(
                text = product.farmer,
                color = OnboardTextMuted,
                fontSize = 10.sp,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom row: price and selector or shopping bag button
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

                // Quantity selector: if quantity > 0 show [ - 2 pcs + ], otherwise show shopping bag icon button
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
                        // Minus button
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

                        // Quantity count
                        Text(
                            text = "$cartQuantity pcs",
                            color = OnboardTextDark,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )

                        // Plus button
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
                    // Small Shopping Bag Button to add first item
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

// 7. Search View Flow
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
    onToggleFavorite: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        // Category Chips Row
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

// 8. Catalog Main View (Supports Top Tabs for Products, Farmers, and Recipes)
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
    onAddRecipeIngredients: (Recipe) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Top 3-tabs Selector: Produk (0), Petani (1), Resep (2)
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
                // Products list with categories
                SearchFlowView(
                    categories = categories,
                    selectedCategory = selectedCategory,
                    onCategorySelected = onCategorySelected,
                    products = products,
                    cartItems = cartItems,
                    favoriteProductIds = favoriteProductIds,
                    onAddToCart = onAddToCart,
                    onRemoveFromCart = onRemoveFromCart,
                    onToggleFavorite = onToggleFavorite
                )
            }
            1 -> {
                // Farmers list (Screen C style)
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(farmers) { farmer ->
                        FarmerListCard(farmer = farmer)
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
            2 -> {
                // Recipes list grid (Screen D style)
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
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = farmer.name,
                        color = OnboardTextDark,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Rating",
                        tint = OnboardAccentTerracotta,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = farmer.rating,
                        color = OnboardTextDark,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = farmer.location,
                    color = OnboardTextMuted,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Farmer specialties tags
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

            Text(
                text = farmer.distance,
                color = OnboardAccentTerracotta,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

// 8c. Recipe Grid Card component (Allows adding ingredients direct to cart)
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

                // Time Badge
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
                        imageVector = Icons.Default.PlayArrow, // clock representation
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

                // Add Recipe ingredients button
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

// 9. Favorites View Tab
@Composable
fun FavoritesView(
    products: List<Product>,
    cartItems: Map<Product, Int>,
    favoriteProductIds: List<Int>,
    onAddToCart: (Product) -> Unit,
    onRemoveFromCart: (Product) -> Unit,
    onToggleFavorite: (Int) -> Unit
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

        // Profile Cards Info
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

        // Reset Onboarding Button
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
            // Free Shipping status
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

            // Cart Summary and checkout button
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
