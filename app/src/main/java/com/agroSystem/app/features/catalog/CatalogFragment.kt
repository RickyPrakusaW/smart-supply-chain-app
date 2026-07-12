package com.agroSystem.app.features.catalog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
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
import com.agroSystem.app.features.home.DashboardFragment
import com.agroSystem.app.features.shared.MainSharedViewModel
import com.google.android.material.tabs.TabLayout
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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

    private fun setupEdamamTab() {
        rvEdamam.layoutManager = LinearLayoutManager(requireContext())
        edamamAdapter = EdamamFoodAdapter(emptyList())
        rvEdamam.adapter = edamamAdapter

        btnSearchEdamam.setOnClickListener {
            val query = inputSearchEdamam.text.toString().trim()
            if (query.isEmpty()) {
                Toast.makeText(requireContext(), "Silakan masukkan nama makanan!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            searchEdamamFood(query)
        }
    }

    private fun searchEdamamFood(query: String) {
        progressEdamam.visibility = View.VISIBLE
        textEmptyEdamam.visibility = View.GONE
        edamamAdapter.updateItems(emptyList())

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val client = okhttp3.OkHttpClient()
                val url = "https://edamam-food-and-grocery-database.p.rapidapi.com/api/food-database/v2/parser?ingr=" + java.net.URLEncoder.encode(query, "UTF-8")
                val request = okhttp3.Request.Builder()
                    .url(url)
                    .get()
                    .addHeader("x-rapidapi-host", "edamam-food-and-grocery-database.p.rapidapi.com")
                    .addHeader("x-rapidapi-key", "d0cb222ccfmshdb370c52ef78688p171408jsn420b6036d587")
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                withContext(Dispatchers.Main) {
                    progressEdamam.visibility = View.GONE
                    if (response.isSuccessful && responseBody != null) {
                        val edamamResponse = com.google.gson.Gson().fromJson(responseBody, EdamamResponse::class.java)
                        val hints = edamamResponse.hints ?: emptyList()
                        edamamAdapter.updateItems(hints)
                        if (hints.isEmpty()) {
                            textEmptyEdamam.visibility = View.VISIBLE
                            textEmptyEdamam.text = "Makanan '$query' tidak ditemukan di database Edamam."
                        } else {
                            textEmptyEdamam.visibility = View.GONE
                        }
                    } else {
                        textEmptyEdamam.visibility = View.VISIBLE
                        textEmptyEdamam.text = "Gagal memuat data dari Edamam API (Error: ${response.code})"
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    progressEdamam.visibility = View.GONE
                    textEmptyEdamam.visibility = View.VISIBLE
                    textEmptyEdamam.text = "Terjadi kesalahan koneksi ke Edamam API."
                }
            }
        }
    }
}
