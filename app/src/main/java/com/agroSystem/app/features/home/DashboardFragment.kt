package com.agroSystem.app.features.home

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
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
import com.agroSystem.app.data.models.Product
import com.agroSystem.app.features.catalog.FilterBottomSheetFragment
import com.agroSystem.app.features.catalog.ProductGridAdapter
import com.agroSystem.app.features.shared.MainSharedViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView

class DashboardFragment : Fragment() {

    private val sharedViewModel: MainSharedViewModel by activityViewModels()

    private lateinit var textAddress: TextView
    private lateinit var btnCart: MaterialCardView
    private lateinit var textCartBadge: TextView
    private lateinit var editSearch: EditText
    private lateinit var btnClearSearch: ImageView
    private lateinit var btnFilter: MaterialCardView

    private lateinit var scrollDashboard: View
    private lateinit var layoutSearchResults: View

    private lateinit var rvProductsHome: RecyclerView
    private lateinit var rvRecommendedCategories: RecyclerView
    private lateinit var rvSearchCategories: RecyclerView
    private lateinit var rvSearchGrid: RecyclerView
    private lateinit var layoutEmptySearch: View

    private lateinit var productsAdapter: ProductGridAdapter
    private lateinit var searchGridAdapter: ProductGridAdapter
    private lateinit var categoriesAdapter: CategoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)

        textAddress = view.findViewById(R.id.text_address)
        btnCart = view.findViewById(R.id.btn_cart)
        textCartBadge = view.findViewById(R.id.text_cart_badge)
        editSearch = view.findViewById(R.id.edit_search)
        btnClearSearch = view.findViewById(R.id.btn_clear_search)
        btnFilter = view.findViewById(R.id.btn_filter)

        scrollDashboard = view.findViewById(R.id.scroll_dashboard)
        layoutSearchResults = view.findViewById(R.id.layout_search_results)

        rvProductsHome = view.findViewById(R.id.rv_products_home)
        rvRecommendedCategories = view.findViewById(R.id.rv_recommended_categories)
        rvSearchCategories = view.findViewById(R.id.rv_search_categories)
        rvSearchGrid = view.findViewById(R.id.rv_search_grid)
        layoutEmptySearch = view.findViewById(R.id.layout_empty_search)

        setupButtons()
        setupNormalDashboard()
        setupSearchDashboard()

        return view
    }

    private fun setupButtons() {
        btnCart.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_cartFragment)
        }

        // Observe cart items to update top-right cart badge count
        sharedViewModel.cartItems.observe(viewLifecycleOwner) { cartMap ->
            val totalItemCount = cartMap.values.sum()
            if (totalItemCount > 0) {
                textCartBadge.visibility = View.VISIBLE
                textCartBadge.text = totalItemCount.toString()
            } else {
                textCartBadge.visibility = View.GONE
            }
        }

        view?.findViewById<TextView>(R.id.btn_see_all)?.setOnClickListener {
            // Trigger home activity's bottom nav switch to catalog
            val parent = parentFragment as? HomeFragment
            parent?.view?.findViewById<BottomNavigationView>(R.id.bottom_navigation)?.selectedItemId = R.id.menu_catalog
        }

        btnFilter.setOnClickListener {
            FilterBottomSheetFragment().show(childFragmentManager, "FilterSheet")
        }

        view?.findViewById<MaterialCardView>(R.id.btn_buy_again)?.setOnClickListener {
            navigateToCatalog()
        }

        view?.findViewById<MaterialCardView>(R.id.btn_direct_farmers)?.setOnClickListener {
            navigateToCatalog()
        }

        view?.findViewById<MaterialCardView>(R.id.btn_promo)?.setOnClickListener {
            navigateToCatalog()
        }
    }

    private fun navigateToCatalog() {
        val parent = parentFragment as? HomeFragment
        parent?.view?.findViewById<BottomNavigationView>(R.id.bottom_navigation)?.selectedItemId = R.id.menu_catalog
    }

    private fun setupNormalDashboard() {
        rvProductsHome.layoutManager = GridLayoutManager(requireContext(), 2)
        productsAdapter = ProductGridAdapter(
            products = sharedViewModel.allProducts,
            favoriteIds = sharedViewModel.favoriteProductIds.value ?: emptyList(),
            cartQuantities = sharedViewModel.cartItems.value ?: emptyMap(),
            onProductClick = { product -> navigateToProductDetail(product) },
            onFavoriteClick = { product -> sharedViewModel.toggleFavorite(product.id) },
            onAddClick = { product -> sharedViewModel.addProductToCart(product) },
            onRemoveClick = { product -> sharedViewModel.removeProductFromCart(product) }
        )
        rvProductsHome.adapter = productsAdapter

        // Populate recommended recipes row
        rvRecommendedCategories.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvRecommendedCategories.adapter = RecommendedCategoriesAdapter(
            listOf(
                Pair("Untuk Sarapan", R.drawable.sapi),
                Pair("Makan Siang", R.drawable.sayuran),
                Pair("Bahan Sup Hangat", R.drawable.padi),
                Pair("Camilan Sehat", R.drawable.sayuran)
            )
        )

        // Observe main states
        sharedViewModel.productsList.observe(viewLifecycleOwner) { prods ->
            productsAdapter.updateData(
                prods,
                sharedViewModel.favoriteProductIds.value ?: emptyList(),
                sharedViewModel.cartItems.value ?: emptyMap()
            )
        }
        sharedViewModel.favoriteProductIds.observe(viewLifecycleOwner) { favs ->
            productsAdapter.updateData(
                sharedViewModel.productsList.value ?: sharedViewModel.allProducts,
                favs,
                sharedViewModel.cartItems.value ?: emptyMap()
            )
        }
        sharedViewModel.cartItems.observe(viewLifecycleOwner) { cart ->
            productsAdapter.updateData(
                sharedViewModel.productsList.value ?: sharedViewModel.allProducts,
                sharedViewModel.favoriteProductIds.value ?: emptyList(),
                cart
            )
        }
    }

    private fun setupSearchDashboard() {
        rvSearchGrid.layoutManager = GridLayoutManager(requireContext(), 2)
        searchGridAdapter = ProductGridAdapter(
            products = emptyList(),
            favoriteIds = sharedViewModel.favoriteProductIds.value ?: emptyList(),
            cartQuantities = sharedViewModel.cartItems.value ?: emptyMap(),
            onProductClick = { product -> navigateToProductDetail(product) },
            onFavoriteClick = { product -> sharedViewModel.toggleFavorite(product.id) },
            onAddClick = { product -> sharedViewModel.addProductToCart(product) },
            onRemoveClick = { product -> sharedViewModel.removeProductFromCart(product) }
        )
        rvSearchGrid.adapter = searchGridAdapter

        // Categories selector row inside search
        rvSearchCategories.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        categoriesAdapter = CategoryAdapter(
            sharedViewModel.categories,
            sharedViewModel.selectedCategory.value ?: "Semua"
        ) { category ->
            sharedViewModel.selectedCategory.value = category
        }
        rvSearchCategories.adapter = categoriesAdapter

        // Search text watcher
        editSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString() ?: ""
                sharedViewModel.searchQuery.value = query
                btnClearSearch.visibility = if (query.isNotEmpty()) View.VISIBLE else View.GONE

                if (query.isNotEmpty()) {
                    scrollDashboard.visibility = View.GONE
                    layoutSearchResults.visibility = View.VISIBLE
                } else {
                    scrollDashboard.visibility = View.VISIBLE
                    layoutSearchResults.visibility = View.GONE
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        btnClearSearch.setOnClickListener {
            editSearch.setText("")
            sharedViewModel.searchQuery.value = ""
        }

        // Observe search criteria
        sharedViewModel.searchQuery.observe(viewLifecycleOwner) { runSearch() }
        sharedViewModel.selectedCategory.observe(viewLifecycleOwner) { cat ->
            categoriesAdapter.setSelectedCategory(cat)
            runSearch()
        }
        sharedViewModel.favoriteProductIds.observe(viewLifecycleOwner) { runSearch() }
        sharedViewModel.cartItems.observe(viewLifecycleOwner) { runSearch() }

    }

    private fun runSearch() {
        val filtered = sharedViewModel.getFilteredProducts()
        searchGridAdapter.updateData(
            filtered,
            sharedViewModel.favoriteProductIds.value ?: emptyList(),
            sharedViewModel.cartItems.value ?: emptyMap()
        )
        layoutEmptySearch.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun navigateToProductDetail(product: Product) {
        // Since we are inside a sub-fragment swapped in HomeFragment, we navigate using the parent fragment's NavController!
        val navController = parentFragment?.findNavController()
        navController?.navigate(R.id.action_homeFragment_to_productDetailFragment, bundleOf("productId" to product.id))
    }

    // Horizontal category banner adapter
    class RecommendedCategoriesAdapter(
        private val items: List<Pair<String, Int>>
    ) : RecyclerView.Adapter<RecommendedCategoriesAdapter.CategoryViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recipe_card, parent, false)
            // Modify width safely on the existing RecyclerView.LayoutParams
            val lp = view.layoutParams as RecyclerView.LayoutParams
            lp.width = 160.dpToPx(parent.context)
            view.layoutParams = lp
            return CategoryViewHolder(view)
        }

        override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
            holder.bind(items[position])
        }

        override fun getItemCount(): Int = items.size

        inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val image: ImageView = itemView.findViewById(R.id.image_illustration)
            private val textTime: TextView = itemView.findViewById(R.id.text_time)
            private val textName: TextView = itemView.findViewById(R.id.text_name)
            private val textInfo: TextView = itemView.findViewById(R.id.text_info)
            private val btnAdd: View = itemView.findViewById(R.id.btn_add_ingredients)

            fun bind(item: Pair<String, Int>) {
                image.setImageResource(item.second)
                image.alpha = 0.6f
                textName.text = item.first
                textTime.visibility = View.GONE
                textInfo.visibility = View.GONE
                btnAdd.visibility = View.GONE
            }
        }

        private fun Int.dpToPx(context: android.content.Context): Int {
            return (this * context.resources.displayMetrics.density + 0.5f).toInt()
        }
    }

    // Category adapter for row filter chips
    class CategoryAdapter(
        private val categories: List<String>,
        private var selectedCategory: String,
        private val onCategoryClick: (String) -> Unit
    ) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

        fun setSelectedCategory(cat: String) {
            this.selectedCategory = cat
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
            val view = TextView(parent.context).apply {
                layoutParams = RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 8.dpToPx(parent.context), 0)
                }
                setPadding(14.dpToPx(parent.context), 6.dpToPx(parent.context), 14.dpToPx(parent.context), 6.dpToPx(parent.context))
                textSize = 12f
                textStyle = android.graphics.Typeface.BOLD
                gravity = android.view.Gravity.CENTER
            }
            return CategoryViewHolder(view)
        }

        override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
            holder.bind(categories[position])
        }

        override fun getItemCount(): Int = categories.size

        inner class CategoryViewHolder(private val textView: TextView) : RecyclerView.ViewHolder(textView) {
            fun bind(category: String) {
                textView.text = category
                val context = textView.context
                val isSelected = category == selectedCategory

                if (isSelected) {
                    textView.setBackgroundResource(R.drawable.bg_tag_primary)
                    textView.setTextColor(androidx.core.content.ContextCompat.getColor(context, R.color.white))
                } else {
                    textView.setBackgroundResource(R.drawable.bg_tag_olive)
                    textView.setTextColor(androidx.core.content.ContextCompat.getColor(context, R.color.color_text_dark))
                }

                textView.setOnClickListener { onCategoryClick(category) }
            }
        }

        private fun Int.dpToPx(context: android.content.Context): Int {
            return (this * context.resources.displayMetrics.density + 0.5f).toInt()
        }
        
        private var TextView.textSize: Float
            get() = this.paint.textSize
            set(value) { this.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, value) }
            
        private var TextView.textStyle: Int
            get() = this.typeface.style
            set(value) { this.setTypeface(null, value) }
    }
}
