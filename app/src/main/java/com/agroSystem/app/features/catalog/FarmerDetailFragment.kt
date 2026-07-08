package com.agroSystem.app.features.catalog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.agroSystem.app.R
import com.agroSystem.app.data.models.Farmer
import com.agroSystem.app.data.models.Product
import com.agroSystem.app.features.shared.MainSharedViewModel
import com.google.android.material.card.MaterialCardView
import com.google.android.material.tabs.TabLayout

class FarmerDetailFragment : Fragment() {

    private val sharedViewModel: MainSharedViewModel by activityViewModels()

    private var farmerId: Int = 1
    private lateinit var farmer: Farmer

    private lateinit var btnBack: View
    private lateinit var btnShare: View

    private lateinit var imageFarmer: ImageView
    private lateinit var textRating: TextView
    private lateinit var textName: TextView
    private lateinit var textDescription: TextView
    private lateinit var textLocationDetails: TextView

    private lateinit var tabLayout: TabLayout
    private lateinit var layoutProducts: View
    private lateinit var layoutCerts: LinearLayout
    private lateinit var layoutReviews: LinearLayout

    private lateinit var rvFarmerProducts: RecyclerView
    private lateinit var productsAdapter: ProductGridAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        farmerId = arguments?.getInt("farmerId") ?: 1
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_farmer_detail, container, false)

        farmer = sharedViewModel.allFarmers.firstOrNull { it.id == farmerId } ?: sharedViewModel.allFarmers.first()

        bindViews(view)
        setupActions()
        setupTabSelection()
        populateFarmerInfo()
        populateProductsTab()
        populateCertsTab()
        populateReviewsTab()

        return view
    }

    private fun bindViews(view: View) {
        btnBack = view.findViewById(R.id.btn_back)
        btnShare = view.findViewById(R.id.btn_share)

        imageFarmer = view.findViewById(R.id.image_farmer)
        textRating = view.findViewById(R.id.text_rating)
        textName = view.findViewById(R.id.text_name)
        textDescription = view.findViewById(R.id.text_description)
        textLocationDetails = view.findViewById(R.id.text_location_details)

        tabLayout = view.findViewById(R.id.tab_layout_farmer)
        layoutProducts = view.findViewById(R.id.layout_farmer_products)
        layoutCerts = view.findViewById(R.id.layout_farmer_certs)
        layoutReviews = view.findViewById(R.id.layout_farmer_reviews)

        rvFarmerProducts = view.findViewById(R.id.rv_farmer_products)
    }

    private fun setupActions() {
        btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
        btnShare.setOnClickListener {
            Toast.makeText(requireContext(), "Tautan profil dibagikan!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupTabSelection() {
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        layoutProducts.visibility = View.VISIBLE
                        layoutCerts.visibility = View.GONE
                        layoutReviews.visibility = View.GONE
                    }
                    1 -> {
                        layoutProducts.visibility = View.GONE
                        layoutCerts.visibility = View.VISIBLE
                        layoutReviews.visibility = View.GONE
                    }
                    2 -> {
                        layoutProducts.visibility = View.GONE
                        layoutCerts.visibility = View.GONE
                        layoutReviews.visibility = View.VISIBLE
                    }
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun populateFarmerInfo() {
        imageFarmer.setImageResource(farmer.imageResId)
        textRating.text = farmer.rating
        textName.text = farmer.name
        textDescription.text = farmer.description
        textLocationDetails.text = "${farmer.location} (${farmer.distance} dari lokasi Anda)"
    }

    private fun populateProductsTab() {
        val farmerProducts = sharedViewModel.allProducts.filter {
            it.farmer.contains(farmer.name.take(15)) || it.farmer.contains("Pujon")
        }

        rvFarmerProducts.layoutManager = GridLayoutManager(requireContext(), 2)
        productsAdapter = ProductGridAdapter(
            products = farmerProducts,
            favoriteIds = sharedViewModel.favoriteProductIds.value ?: emptyList(),
            cartQuantities = sharedViewModel.cartItems.value ?: emptyMap(),
            onProductClick = { product -> navigateToProductDetail(product) },
            onFavoriteClick = { product -> sharedViewModel.toggleFavorite(product.id) },
            onAddClick = { product -> sharedViewModel.addProductToCart(product) },
            onRemoveClick = { product -> sharedViewModel.removeProductFromCart(product) }
        )
        rvFarmerProducts.adapter = productsAdapter

        sharedViewModel.favoriteProductIds.observe(viewLifecycleOwner) { favs ->
            productsAdapter.updateData(farmerProducts, favs, sharedViewModel.cartItems.value ?: emptyMap())
        }
        sharedViewModel.cartItems.observe(viewLifecycleOwner) { cart ->
            productsAdapter.updateData(farmerProducts, sharedViewModel.favoriteProductIds.value ?: emptyList(), cart)
        }
    }

    private fun populateCertsTab() {
        layoutCerts.removeAllViews()
        farmer.certs.forEach { cert ->
            val certCard = LayoutInflater.from(requireContext()).inflate(R.layout.item_cart_product, layoutCerts, false)
            val imgView: ImageView = certCard.findViewById(R.id.image_illustration)
            val titleView: TextView = certCard.findViewById(R.id.text_title)
            val descView: TextView = certCard.findViewById(R.id.text_subtitle)
            val checkView: TextView = certCard.findViewById(R.id.text_item_total_price)
            val stepper: View = certCard.findViewById(R.id.layout_stepper)

            imgView.setImageResource(android.R.drawable.checkbox_on_background)
            imgView.setColorFilter(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.color_primary_green))
            titleView.text = cert
            descView.text = "Terverifikasi Resmi"
            checkView.text = "VALID"
            checkView.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.color_primary_green))
            stepper.visibility = View.GONE

            layoutCerts.addView(certCard)
        }
    }

    private fun populateReviewsTab() {
        val farmReviews = listOf(
            Pair("Budi Santoso", "Susu pasteurisasi dari Pujon ini selalu konsisten mutunya. Segar sekali!"),
            Pair("Hartono Malang", "Mitra tani terpercaya, hasil buminya memuaskan untuk konsumsi harian keluarga.")
        )

        layoutReviews.removeAllViews()
        farmReviews.forEach { (reviewer, review) ->
            val reviewView = LayoutInflater.from(requireContext()).inflate(R.layout.item_cart_product, layoutReviews, false)
            val imgView: ImageView = reviewView.findViewById(R.id.image_illustration)
            val titleView: TextView = reviewView.findViewById(R.id.text_title)
            val descView: TextView = reviewView.findViewById(R.id.text_subtitle)
            val scoreView: TextView = reviewView.findViewById(R.id.text_item_total_price)
            val stepper: View = reviewView.findViewById(R.id.layout_stepper)

            imgView.setImageResource(android.R.drawable.ic_menu_myplaces)
            titleView.text = reviewer
            descView.text = review
            scoreView.text = "★ 5.0"
            stepper.visibility = View.GONE

            layoutReviews.addView(reviewView)
        }
    }

    private fun navigateToProductDetail(product: Product) {
        findNavController().navigate(R.id.action_farmerDetailFragment_to_productDetailFragment, bundleOf("productId" to product.id))
    }
}
