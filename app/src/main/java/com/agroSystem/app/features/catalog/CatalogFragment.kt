package com.agroSystem.app.features.catalog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import android.widget.ImageView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.agroSystem.app.R
import com.agroSystem.app.data.models.Farmer
import com.agroSystem.app.data.models.Product
import com.agroSystem.app.data.models.Recipe
import com.agroSystem.app.data.local.AppDatabase
import com.agroSystem.app.data.local.entities.EdamamFoodEntity
import com.agroSystem.app.features.home.DashboardFragment
import com.agroSystem.app.features.shared.MainSharedViewModel
import com.google.android.material.tabs.TabLayout
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log

class CatalogFragment : Fragment() {

    private val sharedViewModel: MainSharedViewModel by activityViewModels()

    private lateinit var tabLayout: TabLayout
    private lateinit var layoutProducts: View
    private lateinit var layoutFarmers: View
    private lateinit var layoutRecipes: View
    private lateinit var layoutEdamam: View

    private lateinit var rvCategories: RecyclerView
    private lateinit var rvProductsGrid: RecyclerView
    private lateinit var rvFarmers: RecyclerView
    private lateinit var rvRecipes: RecyclerView
    private lateinit var rvEdamam: RecyclerView

    private lateinit var productsAdapter: ProductGridAdapter
    private lateinit var farmersAdapter: FarmerListAdapter
    private lateinit var recipesAdapter: RecipeListAdapter
    private lateinit var categoriesAdapter: DashboardFragment.CategoryAdapter
    private lateinit var edamamAdapter: EdamamFoodAdapter

    private lateinit var inputSearchEdamam: com.google.android.material.textfield.TextInputEditText
    private lateinit var btnSearchEdamam: com.google.android.material.button.MaterialButton
    private lateinit var progressEdamam: android.widget.ProgressBar
    private lateinit var textEmptyEdamam: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_catalog, container, false)

        tabLayout = view.findViewById(R.id.tab_layout_catalog)
        layoutProducts = view.findViewById(R.id.layout_catalog_products)
        layoutFarmers = view.findViewById(R.id.layout_catalog_farmers)
        layoutRecipes = view.findViewById(R.id.layout_catalog_recipes)
        layoutEdamam = view.findViewById(R.id.layout_catalog_edamam)

        rvCategories = view.findViewById(R.id.rv_catalog_categories)
        rvProductsGrid = view.findViewById(R.id.rv_catalog_products_grid)
        rvFarmers = view.findViewById(R.id.rv_catalog_farmers)
        rvRecipes = view.findViewById(R.id.rv_catalog_recipes)
        rvEdamam = view.findViewById(R.id.rv_catalog_edamam)

        inputSearchEdamam = view.findViewById(R.id.input_search_edamam)
        btnSearchEdamam = view.findViewById(R.id.btn_search_edamam)
        progressEdamam = view.findViewById(R.id.progress_edamam)
        textEmptyEdamam = view.findViewById(R.id.text_empty_edamam)

        setupTabSwapping()
        setupProductsTab()
        setupFarmersTab()
        setupRecipesTab()
        setupEdamamTab()

        return view
    }

    private fun setupTabSwapping() {
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        layoutProducts.visibility = View.VISIBLE
                        layoutFarmers.visibility = View.GONE
                        layoutRecipes.visibility = View.GONE
                        layoutEdamam.visibility = View.GONE
                    }
                    1 -> {
                        layoutProducts.visibility = View.GONE
                        layoutFarmers.visibility = View.VISIBLE
                        layoutRecipes.visibility = View.GONE
                        layoutEdamam.visibility = View.GONE
                    }
                    2 -> {
                        layoutProducts.visibility = View.GONE
                        layoutFarmers.visibility = View.GONE
                        layoutRecipes.visibility = View.VISIBLE
                        layoutEdamam.visibility = View.GONE
                    }
                    3 -> {
                        layoutProducts.visibility = View.GONE
                        layoutFarmers.visibility = View.GONE
                        layoutRecipes.visibility = View.GONE
                        layoutEdamam.visibility = View.VISIBLE
                    }
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupProductsTab() {
        rvProductsGrid.layoutManager = GridLayoutManager(requireContext(), 2)
        productsAdapter = ProductGridAdapter(
            products = sharedViewModel.getFilteredProducts(),
            favoriteIds = sharedViewModel.favoriteProductIds.value ?: emptyList(),
            cartQuantities = sharedViewModel.cartItems.value ?: emptyMap(),
            onProductClick = { product -> navigateToProductDetail(product) },
            onFavoriteClick = { product -> sharedViewModel.toggleFavorite(product.id) },
            onAddClick = { product -> sharedViewModel.addProductToCart(product) },
            onRemoveClick = { product -> sharedViewModel.removeProductFromCart(product) }
        )
        rvProductsGrid.adapter = productsAdapter

        rvCategories.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        categoriesAdapter = DashboardFragment.CategoryAdapter(
            sharedViewModel.categories,
            sharedViewModel.selectedCategory.value ?: "Semua"
        ) { category ->
            sharedViewModel.selectedCategory.value = category
        }
        rvCategories.adapter = categoriesAdapter

        // LiveData observation for products
        sharedViewModel.selectedCategory.observe(viewLifecycleOwner) { cat ->
            categoriesAdapter.setSelectedCategory(cat)
            updateProductsList()
        }
        sharedViewModel.favoriteProductIds.observe(viewLifecycleOwner) { updateProductsList() }
        sharedViewModel.cartItems.observe(viewLifecycleOwner) { updateProductsList() }
        sharedViewModel.productsList.observe(viewLifecycleOwner) { updateProductsList() }
    }

    private fun updateProductsList() {
        productsAdapter.updateData(
            sharedViewModel.getFilteredProducts(),
            sharedViewModel.favoriteProductIds.value ?: emptyList(),
            sharedViewModel.cartItems.value ?: emptyMap()
        )
    }

    private fun setupFarmersTab() {
        rvFarmers.layoutManager = LinearLayoutManager(requireContext())
        farmersAdapter = FarmerListAdapter(sharedViewModel.allFarmers) { farmer ->
            navigateToFarmerDetail(farmer)
        }
        rvFarmers.adapter = farmersAdapter
    }

    private fun setupRecipesTab() {
        rvRecipes.layoutManager = GridLayoutManager(requireContext(), 2)
        recipesAdapter = RecipeListAdapter(sharedViewModel.allRecipes) { recipe ->
            sharedViewModel.addRecipeIngredients(recipe)
            Toast.makeText(requireContext(), "Bahan resep ${recipe.name} ditambahkan!", Toast.LENGTH_SHORT).show()
        }
        rvRecipes.adapter = recipesAdapter
    }

    private fun navigateToProductDetail(product: Product) {
        val navController = parentFragment?.findNavController()
        navController?.navigate(R.id.action_homeFragment_to_productDetailFragment, bundleOf("productId" to product.id))
    }

    private fun navigateToFarmerDetail(farmer: Farmer) {
        val navController = parentFragment?.findNavController()
        navController?.navigate(R.id.action_homeFragment_to_farmerDetailFragment, bundleOf("farmerId" to farmer.id))
    }

    private fun getEnglishEdamamQuery(inputText: String): String {
        val query = inputText.lowercase().trim()
        return when {
            query == "buah" || query == "buah segar" || query == "buah-buahan" -> "fruit"
            query == "sayur" || query == "sayuran" || query == "sayur segar" -> "vegetables"
            query.contains("apel") -> "apple"
            query.contains("pisang") -> "banana"
            query.contains("mangga") -> "mango"
            query.contains("jeruk") -> "orange"
            query.contains("tomat") -> "tomato"
            query.contains("kentang") -> "potato"
            query.contains("alpukat") -> "avocado"
            query.contains("stroberi") || query.contains("strawberry") -> "strawberry"
            query.contains("anggur") -> "grape"
            query.contains("pepaya") -> "papaya"
            query.contains("semangka") -> "watermelon"
            query.contains("melon") -> "melon"
            query.contains("nanas") || query.contains("pineapple") -> "pineapple"
            query.contains("susu") -> "milk"
            query.contains("keju") -> "cheese"
            query.contains("telur") -> "egg"
            query.contains("beras") || query.contains("nasi") || query.contains("padi") -> "rice"
            query.contains("bayam") -> "spinach"
            query.contains("wortel") -> "carrot"
            query.contains("bawang") -> "onion"
            query.contains("cabai") || query.contains("cabe") -> "chili"
            query.contains("daging sapi") || query == "sapi" -> "beef"
            query.contains("daging ayam") || query == "ayam" -> "chicken"
            query.contains("daging kambing") || query == "kambing" -> "goat"
            query.contains("daging") -> "meat"
            query.contains("madu") -> "honey"
            else -> query
        }
    }

    private fun setupEdamamTab() {
        rvEdamam.layoutManager = LinearLayoutManager(requireContext())
        edamamAdapter = EdamamFoodAdapter(emptyList()) { hint ->
            showEdamamDetailBottomSheet(hint)
        }
        rvEdamam.adapter = edamamAdapter

        btnSearchEdamam.setOnClickListener {
            val query = inputSearchEdamam.text.toString().trim()
            if (query.isEmpty()) {
                Toast.makeText(requireContext(), "Silakan masukkan nama makanan!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            searchEdamamFood(query)
        }

        // Trigger default query in Indonesian: sayuran segar (will auto translate to fresh vegetables)
        searchEdamamFood("sayuran segar")
    }

    private fun showEdamamDetailBottomSheet(hint: EdamamHint) {
        val food = hint.food ?: return
        val dialog = com.google.android.material.bottomsheet.BottomSheetDialog(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.dialog_edamam_detail, null)
        dialog.setContentView(dialogView)

        val imgFood: ImageView = dialogView.findViewById(R.id.img_dialog_food)
        val txtTitle: TextView = dialogView.findViewById(R.id.txt_dialog_title)
        val txtCategory: TextView = dialogView.findViewById(R.id.txt_dialog_category)
        val txtCalories: TextView = dialogView.findViewById(R.id.txt_dialog_calories)
        val txtProtein: TextView = dialogView.findViewById(R.id.txt_dialog_protein)
        val txtFat: TextView = dialogView.findViewById(R.id.txt_dialog_fat)
        val txtCarbs: TextView = dialogView.findViewById(R.id.txt_dialog_carbs)
        val btnClose: View = dialogView.findViewById(R.id.btn_close_dialog)
        val btnBuy: View = dialogView.findViewById(R.id.btn_buy_edamam)

        val rawCategoryForMapping = food.category?.lowercase() ?: ""
        val categoryMapped = when {
            rawCategoryForMapping.contains("dairy") || rawCategoryForMapping.contains("susu") -> "Susu"
            rawCategoryForMapping.contains("vegetable") || rawCategoryForMapping.contains("sayuran") || rawCategoryForMapping.contains("bayam") || rawCategoryForMapping.contains("tomat") || rawCategoryForMapping.contains("kentang") || rawCategoryForMapping.contains("fruit") || rawCategoryForMapping.contains("buah") -> "Sayuran"
            rawCategoryForMapping.contains("meat") || rawCategoryForMapping.contains("daging") || rawCategoryForMapping.contains("chicken") || rawCategoryForMapping.contains("poultry") -> "Daging"
            rawCategoryForMapping.contains("egg") || rawCategoryForMapping.contains("telur") -> "Telur"
            else -> "Bahan Sup"
        }

        // Calculate dynamic realistic price based on food category and deterministic name variance
        val basePrice = when (categoryMapped) {
            "Daging" -> 85000
            "Susu" -> 35000
            "Telur" -> 22000
            "Sayuran" -> 12000
            else -> 18000
        }
        val variance = ((food.label ?: "").length * 500) % 8000
        val calculatedPrice = basePrice + variance
        val formattedPrice = java.text.NumberFormat.getNumberInstance(java.util.Locale("id", "ID")).format(calculatedPrice)

        txtTitle.text = food.label ?: "-"
        txtCategory.text = food.category ?: "Umum"

        if (btnBuy is com.google.android.material.button.MaterialButton) {
            btnBuy.text = "Beli Bahan Ini (Rp $formattedPrice)"
        }

        val nutrients = food.nutrients
        val df = java.text.DecimalFormat("#.#")
        txtCalories.text = "${df.format(nutrients?.ENERC_KCAL ?: 0.0)} kcal"
        txtProtein.text = "${df.format(nutrients?.PROCNT ?: 0.0)}g"
        txtFat.text = "${df.format(nutrients?.FAT ?: 0.0)}g"
        txtCarbs.text = "${df.format(nutrients?.CHOCDF ?: 0.0)}g"

        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        btnBuy.setOnClickListener {
            val rawId = (food.foodId ?: "").hashCode()
            val finalId = if (rawId == Int.MIN_VALUE) 99999 else Math.abs(rawId)

            val product = Product(
                id = finalId,
                name = food.label ?: "Makanan Edamam",
                farmer = "Mitra Tani Edamam",
                rating = "4.9",
                price = calculatedPrice,
                unit = "1 kg",
                imageResId = R.drawable.sayuran,
                category = categoryMapped,
                isEcoFriendly = true,
                deliveryDays = 2,
                protein = "${df.format(nutrients?.PROCNT ?: 0.0)}g",
                fat = "${df.format(nutrients?.FAT ?: 0.0)}g",
                carbs = "${df.format(nutrients?.CHOCDF ?: 0.0)}g",
                calories = "${df.format(nutrients?.ENERC_KCAL ?: 0.0)} Kcal",
                ingredients = "Bahan segar bersumber dari Edamam Global Database.",
                ownerId = "google_admingmailcom",
                imageBytes = food.image
            )

            sharedViewModel.createProduct(product) { success ->
                activity?.runOnUiThread {
                    sharedViewModel.addProductToCart(product)
                    Toast.makeText(requireContext(), "${product.name} ditambahkan ke keranjang!", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
            }
        }

        // Load image URL securely
        val urlStr = food.image
        if (!urlStr.isNullOrEmpty()) {
            val secureUrlStr = if (urlStr.startsWith("http://")) {
                urlStr.replace("http://", "https://")
            } else {
                urlStr
            }
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val connection = java.net.URL(secureUrlStr).openConnection() as java.net.HttpURLConnection
                    connection.doInput = true
                    connection.connect()
                    val input = connection.inputStream
                    val bitmap = android.graphics.BitmapFactory.decodeStream(input)
                    withContext(Dispatchers.Main) {
                        if (bitmap != null) {
                            imgFood.setImageBitmap(bitmap)
                        } else {
                            imgFood.setImageResource(R.drawable.sayuran)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        imgFood.setImageResource(R.drawable.sayuran)
                    }
                }
            }
        } else {
            imgFood.setImageResource(R.drawable.sayuran)
        }

        dialog.show()
    }

    private fun searchEdamamFood(query: String) {
        progressEdamam.visibility = View.VISIBLE
        textEmptyEdamam.visibility = View.GONE
        edamamAdapter.updateItems(emptyList())

        val translatedQuery = getEnglishEdamamQuery(query)

        lifecycleScope.launch(Dispatchers.IO) {
            val localDb = AppDatabase.getDatabase(requireContext())
            try {
                val client = okhttp3.OkHttpClient()
                val url = "https://edamam-food-and-grocery-database.p.rapidapi.com/api/food-database/v2/parser?ingr=" + java.net.URLEncoder.encode(translatedQuery, "UTF-8")
                
                Log.d("CatalogFragment", "Searching Edamam food URL: $url")
                
                val request = okhttp3.Request.Builder()
                    .url(url)
                    .get()
                    .addHeader("X-RapidAPI-Host", "edamam-food-and-grocery-database.p.rapidapi.com")
                    .addHeader("X-RapidAPI-Key", "d0cb222ccfmshdb370c52ef78688p171408jsn420b6036d587")
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()
                
                Log.d("CatalogFragment", "Edamam response code: ${response.code}, body: $responseBody")

                if (response.isSuccessful && responseBody != null) {
                    val edamamResponse = com.google.gson.Gson().fromJson(responseBody, EdamamResponse::class.java)
                    val hints = edamamResponse.hints ?: emptyList()
                    
                    // Offline Cache Logic: Save successfully fetched foods into Room Cache using original Indonesian query Term!
                    if (hints.isNotEmpty()) {
                        val entitiesToCache = hints.mapNotNull { hint ->
                            val food = hint.food ?: return@mapNotNull null
                            val foodId = food.foodId ?: return@mapNotNull null
                            val label = food.label ?: ""
                            val category = food.category ?: ""
                            val nutrients = food.nutrients
                            
                            EdamamFoodEntity(
                                foodId = foodId,
                                label = label,
                                category = category,
                                imageUrl = food.image,
                                calories = nutrients?.ENERC_KCAL ?: 0.0,
                                protein = nutrients?.PROCNT ?: 0.0,
                                fat = nutrients?.FAT ?: 0.0,
                                carbs = nutrients?.CHOCDF ?: 0.0,
                                queryTerm = query.lowercase().trim()
                            )
                        }
                        localDb.edamamFoodDao().insertFoods(entitiesToCache)
                    }

                    withContext(Dispatchers.Main) {
                        progressEdamam.visibility = View.GONE
                        val sortedHints = hints.sortedWith(compareByDescending { !it.food?.image.isNullOrEmpty() })
                        edamamAdapter.updateItems(sortedHints)
                        if (hints.isEmpty()) {
                            textEmptyEdamam.visibility = View.VISIBLE
                            textEmptyEdamam.text = "Makanan '$query' tidak ditemukan di database Edamam."
                        } else {
                            textEmptyEdamam.visibility = View.GONE
                        }
                    }
                } else {
                    // Fail fallback: read from Room DB Cache if possible
                    loadCachedDataOrShowError(localDb, query, "Gagal memuat data dari Edamam API (Error: ${response.code})")
                }
            } catch (e: Exception) {
                Log.e("CatalogFragment", "Edamam network exception occurred", e)
                // Network error fallback: read from Room DB Cache
                loadCachedDataOrShowError(localDb, query, "Terjadi kesalahan koneksi ke Edamam API: ${e.message}")
            }
        }
    }

    private suspend fun loadCachedDataOrShowError(localDb: AppDatabase, query: String, networkErrorMessage: String) {
        val cachedEntities = localDb.edamamFoodDao().getCachedFoods(query.lowercase().trim())
        
        withContext(Dispatchers.Main) {
            progressEdamam.visibility = View.GONE
            if (cachedEntities.isNotEmpty()) {
                val hints = cachedEntities.map { entity ->
                    EdamamHint(
                        food = EdamamFood(
                            foodId = entity.foodId,
                            label = entity.label,
                            category = entity.category,
                            image = entity.imageUrl,
                            nutrients = EdamamNutrients(
                                ENERC_KCAL = entity.calories,
                                PROCNT = entity.protein,
                                FAT = entity.fat,
                                CHOCDF = entity.carbs
                            )
                        )
                    )
                }
                val sortedHints = hints.sortedWith(compareByDescending { !it.food?.image.isNullOrEmpty() })
                edamamAdapter.updateItems(sortedHints)
                Toast.makeText(requireContext(), "Menampilkan data dari cache offline lokal.", Toast.LENGTH_SHORT).show()
                textEmptyEdamam.visibility = View.GONE
            } else {
                textEmptyEdamam.visibility = View.VISIBLE
                textEmptyEdamam.text = "$networkErrorMessage (Tidak ada data di cache offline)."
            }
        }
    }
}
