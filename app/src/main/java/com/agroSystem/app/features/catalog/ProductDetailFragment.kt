package com.agroSystem.app.features.catalog

import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.agroSystem.app.R
import com.agroSystem.app.data.models.Farmer
import com.agroSystem.app.data.models.Product
import com.agroSystem.app.data.models.bindImageTo
import com.agroSystem.app.data.models.Recipe
import com.agroSystem.app.features.shared.MainSharedViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.tabs.TabLayout

class ProductDetailFragment : Fragment() {

    private val sharedViewModel: MainSharedViewModel by activityViewModels()

    private var productId: Int = 1
    private lateinit var product: Product
    private lateinit var farmer: Farmer

    // Header views
    private lateinit var btnBack: View
    private lateinit var btnShare: View
    private lateinit var btnFavorite: MaterialCardView
    private lateinit var imageFavorite: ImageView

    // Product Main info
    private lateinit var imageProduct: ImageView
    private lateinit var tagDiscount: TextView
    private lateinit var textRating: TextView
    private lateinit var textTitle: TextView
    private lateinit var textPrice: TextView
    private lateinit var textOriginalPrice: TextView
    private lateinit var textUnit: TextView
    private lateinit var textDeliveryBadge: TextView
    private lateinit var textDescription: TextView

    // TabLayout
    private lateinit var tabLayout: TabLayout
    private lateinit var layoutDetailSpec: View
    private lateinit var layoutDetailReviews: View
    private lateinit var layoutDetailRecipes: View

    // Detail spec tab sub-views
    private lateinit var textNutrProtein: TextView
    private lateinit var textNutrFat: TextView
    private lateinit var textNutrCarbs: TextView
    private lateinit var textNutrCalories: TextView
    private lateinit var textSpecShelfLife: TextView
    private lateinit var textSpecStorage: TextView
    private lateinit var textSpecPackaging: TextView
    private lateinit var layoutCharacteristicsTags: LinearLayout
    private lateinit var layoutFarmerCard: View

    // Review tab sub-views
    private lateinit var layoutReviewsList: LinearLayout

    // Recipe tab sub-views
    private lateinit var rvDetailRecipes: RecyclerView

    // Bottom Action Bar views
    private lateinit var textBottomPrice: TextView
    private lateinit var btnBottomAdd: MaterialButton
    private lateinit var layoutBottomStepper: LinearLayout
    private lateinit var btnBottomMinus: ImageView
    private lateinit var btnBottomPlus: ImageView
    private lateinit var textBottomStepperCount: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        productId = arguments?.getInt("productId") ?: 1
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_product_detail, container, false)

        // Find product
        product = sharedViewModel.allProducts.firstOrNull { it.id == productId } ?: sharedViewModel.allProducts.first()
        // Find matching farmer
        val farmerName = product.farmer ?: "Peternakan Kemitraan Mandiri, Batu"
        farmer = sharedViewModel.allFarmers.firstOrNull { it.name.contains(farmerName.split(",").first().trim()) || it.name.contains("Pujon") } ?: sharedViewModel.allFarmers.first()

        bindViews(view)
        setupHeaderActions()
        setupTabSelection()
        populateProductInfo()
        populateSpecTab()
        populateReviewsTab()
        populateRecipesTab()
        setupBottomBar()

        return view
    }

    private fun bindViews(view: View) {
        btnBack = view.findViewById(R.id.btn_back)
        btnShare = view.findViewById(R.id.btn_share)
        btnFavorite = view.findViewById(R.id.btn_favorite)
        imageFavorite = view.findViewById(R.id.image_favorite)

        imageProduct = view.findViewById(R.id.image_product)
        tagDiscount = view.findViewById(R.id.tag_discount)
        textRating = view.findViewById(R.id.text_rating)
        textTitle = view.findViewById(R.id.text_title)
        textPrice = view.findViewById(R.id.text_price)
        textOriginalPrice = view.findViewById(R.id.text_original_price)
        textUnit = view.findViewById(R.id.text_unit)
        textDeliveryBadge = view.findViewById(R.id.text_delivery_badge)
        textDescription = view.findViewById(R.id.text_description)

        tabLayout = view.findViewById(R.id.tab_layout_detail)
        layoutDetailSpec = view.findViewById(R.id.layout_detail_spec)
        layoutDetailReviews = view.findViewById(R.id.layout_detail_reviews)
        layoutDetailRecipes = view.findViewById(R.id.layout_detail_recipes)

        textNutrProtein = view.findViewById(R.id.text_nutr_protein)
        textNutrFat = view.findViewById(R.id.text_nutr_fat)
        textNutrCarbs = view.findViewById(R.id.text_nutr_carbs)
        textNutrCalories = view.findViewById(R.id.text_nutr_calories)
        textSpecShelfLife = view.findViewById(R.id.text_spec_shelf_life)
        textSpecStorage = view.findViewById(R.id.text_spec_storage)
        textSpecPackaging = view.findViewById(R.id.text_spec_packaging)
        layoutCharacteristicsTags = view.findViewById(R.id.layout_characteristics_tags)
        layoutFarmerCard = view.findViewById(R.id.layout_farmer_card)

        layoutReviewsList = view.findViewById(R.id.layout_reviews_list)
        rvDetailRecipes = view.findViewById(R.id.rv_detail_recipes)

        textBottomPrice = view.findViewById(R.id.text_bottom_price)
        btnBottomAdd = view.findViewById(R.id.btn_bottom_add)
        layoutBottomStepper = view.findViewById(R.id.layout_bottom_stepper)
        btnBottomMinus = view.findViewById(R.id.btn_bottom_minus)
        btnBottomPlus = view.findViewById(R.id.btn_bottom_plus)
        textBottomStepperCount = view.findViewById(R.id.text_bottom_stepper_count)
    }

    private fun setupHeaderActions() {
        btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
        btnShare.setOnClickListener {
            Toast.makeText(requireContext(), "Tautan produk disalin!", Toast.LENGTH_SHORT).show()
        }
        btnFavorite.setOnClickListener {
            sharedViewModel.toggleFavorite(product.id)
        }

        sharedViewModel.favoriteProductIds.observe(viewLifecycleOwner) { favs ->
            val isFavorite = favs.contains(product.id)
            imageFavorite.setImageResource(
                if (isFavorite) android.R.drawable.btn_star_big_on else android.R.drawable.btn_star_big_off
            )
            if (isFavorite) {
                imageFavorite.setColorFilter(android.graphics.Color.RED)
            } else {
                imageFavorite.clearColorFilter()
            }
        }
    }

    private fun setupTabSelection() {
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        layoutDetailSpec.visibility = View.VISIBLE
                        layoutDetailReviews.visibility = View.GONE
                        layoutDetailRecipes.visibility = View.GONE
                    }
                    1 -> {
                        layoutDetailSpec.visibility = View.GONE
                        layoutDetailReviews.visibility = View.VISIBLE
                        layoutDetailRecipes.visibility = View.GONE
                    }
                    2 -> {
                        layoutDetailSpec.visibility = View.GONE
                        layoutDetailReviews.visibility = View.GONE
                        layoutDetailRecipes.visibility = View.VISIBLE
                    }
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun populateProductInfo() {
        product.bindImageTo(imageProduct)
        textRating.text = product.rating
        textTitle.text = product.name
        textPrice.text = "Rp ${product.price}"
        textUnit.text = " / ${product.unit}"
        textDescription.text = product.ingredients

        if (product.isDiscounted) {
            tagDiscount.visibility = View.VISIBLE
            textOriginalPrice.visibility = View.VISIBLE
            textOriginalPrice.text = "Rp ${product.originalPrice}"
            textOriginalPrice.paintFlags = textOriginalPrice.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            tagDiscount.visibility = View.GONE
            textOriginalPrice.visibility = View.GONE
        }

        textDeliveryBadge.text = "Kirim: ${if (product.deliveryDays == 1) "Hari Ini" else "Besok"}"
    }

    private fun populateSpecTab() {
        textNutrProtein.text = product.protein
        textNutrFat.text = product.fat
        textNutrCarbs.text = product.carbs
        textNutrCalories.text = product.calories

        textSpecShelfLife.text = product.shelfLife
        textSpecStorage.text = product.storage
        textSpecPackaging.text = product.packaging

        // Populate Characteristics tag container
        val tags = listOf("Vegetarian", "Keto", "Bebas Laktosa", "Bebas Gluten", "Tinggi Kalsium", "Kaya Protein")
        layoutCharacteristicsTags.removeAllViews()
        tags.forEach { label ->
            val textView = TextView(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 8.dpToPx(requireContext()), 0)
                }
                setPadding(10.dpToPx(requireContext()), 4.dpToPx(requireContext()), 10.dpToPx(requireContext()), 4.dpToPx(requireContext()))
                background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_tag_olive)
                text = label
                textColor = ContextCompat.getColor(requireContext(), R.color.color_text_muted)
                textSize = 10f
                textStyle = android.graphics.Typeface.BOLD
                gravity = Gravity.CENTER
            }
            layoutCharacteristicsTags.addView(textView)
        }

        // Setup Farmer Card inside include layout
        val imgFarmer: ImageView = layoutFarmerCard.findViewById(R.id.image_illustration)
        val textFarmerName: TextView = layoutFarmerCard.findViewById(R.id.text_name)
        val textFarmerRating: TextView = layoutFarmerCard.findViewById(R.id.text_rating)
        val textFarmerLocation: TextView = layoutFarmerCard.findViewById(R.id.text_location)
        val textFarmerDistance: TextView = layoutFarmerCard.findViewById(R.id.text_distance)

        imgFarmer.setImageResource(farmer.imageResId)
        textFarmerName.text = farmer.name
        textFarmerRating.text = farmer.rating
        textFarmerLocation.text = farmer.location
        textFarmerDistance.text = "${farmer.distance} dari lokasi Anda"

        // Setup farmer tags
        val farmTags = farmer.productsList.split(",")
        val tag1: TextView = layoutFarmerCard.findViewById(R.id.tag1)
        val tag2: TextView = layoutFarmerCard.findViewById(R.id.tag2)
        val tag3: TextView = layoutFarmerCard.findViewById(R.id.tag3)
        tag1.visibility = View.GONE
        tag2.visibility = View.GONE
        tag3.visibility = View.GONE

        if (farmTags.isNotEmpty()) {
            tag1.text = farmTags[0].trim()
            tag1.visibility = View.VISIBLE
        }
        if (farmTags.size > 1) {
            tag2.text = farmTags[1].trim()
            tag2.visibility = View.VISIBLE
        }
        if (farmTags.size > 2) {
            tag3.text = farmTags[2].trim()
            tag3.visibility = View.VISIBLE
        }

        layoutFarmerCard.setOnClickListener {
            findNavController().navigate(R.id.action_productDetailFragment_to_farmerDetailFragment, bundleOf("farmerId" to farmer.id))
        }
    }

    private fun populateReviewsTab() {
        val reviews = listOf(
            Triple("Amir Santoso", "Susu kambing segar ini mantap sekali rasanya, gurih dan kemasannya botol kaca tebal steril. Recomended!", R.drawable.sapi),
            Triple("Desi Ratnasari", "Pengirimannya super cepat, masih dingin saat sampai. Sangat higienis bagi kesehatan keluarga.", R.drawable.sapi),
            Triple("Edi Wijaya", "Biasa buat langganan sarapan pagi anak-anak. Bagus kualitas Pujon.", R.drawable.sapi)
        )

        layoutReviewsList.removeAllViews()
        reviews.forEach { (name, text, imgId) ->
            val reviewView = LayoutInflater.from(requireContext()).inflate(R.layout.item_cart_product, layoutReviewsList, false)
            // Reuse cart product item to show a review layout style:
            val imgView: ImageView = reviewView.findViewById(R.id.image_illustration)
            val titleView: TextView = reviewView.findViewById(R.id.text_title)
            val descView: TextView = reviewView.findViewById(R.id.text_subtitle)
            val ratingView: TextView = reviewView.findViewById(R.id.text_item_total_price)
            val stepper: View = reviewView.findViewById(R.id.layout_stepper)

            imgView.setImageResource(imgId)
            titleView.text = name
            descView.text = text
            ratingView.text = "★ 5.0"
            stepper.visibility = View.GONE

            layoutReviewsList.addView(reviewView)
        }
    }

    private fun populateRecipesTab() {
        val matchingRecipes = sharedViewModel.allRecipes.filter { it.ingredients.contains(product.id) }
        rvDetailRecipes.layoutManager = GridLayoutManager(requireContext(), 2)
        val adapter = RecipeListAdapter(matchingRecipes) { recipe ->
            sharedViewModel.addRecipeIngredients(recipe)
            Toast.makeText(requireContext(), "Bahan resep ${recipe.name} ditambahkan!", Toast.LENGTH_SHORT).show()
        }
        rvDetailRecipes.adapter = adapter
    }

    private fun setupBottomBar() {
        // Observe cart items to update total price & bottom bar actions
        sharedViewModel.cartItems.observe(viewLifecycleOwner) { cartMap ->
            val qty = cartMap.entries.firstOrNull { it.key.id == product.id }?.value ?: 0
            val currentPrice = product.price * if (qty > 0) qty else 1
            textBottomPrice.text = "Rp $currentPrice"

            if (qty > 0) {
                btnBottomAdd.visibility = View.GONE
                layoutBottomStepper.visibility = View.VISIBLE
                textBottomStepperCount.text = "$qty Pcs"
            } else {
                btnBottomAdd.visibility = View.VISIBLE
                layoutBottomStepper.visibility = View.GONE
            }
        }

        btnBottomAdd.setOnClickListener {
            sharedViewModel.addProductToCart(product)
        }
        btnBottomPlus.setOnClickListener {
            sharedViewModel.addProductToCart(product)
        }
        btnBottomMinus.setOnClickListener {
            sharedViewModel.removeProductFromCart(product)
        }
    }

    private fun Int.dpToPx(context: Context): Int {
        return (this * context.resources.displayMetrics.density + 0.5f).toInt()
    }
    
    private var TextView.textColor: Int
        get() = this.currentTextColor
        set(value) { this.setTextColor(value) }

    private var TextView.textSize: Float
        get() = this.paint.textSize
        set(value) { this.setTextSize(TypedValue.COMPLEX_UNIT_SP, value) }
        
    private var TextView.textStyle: Int
        get() = this.typeface.style
        set(value) { this.setTypeface(null, value) }
}
