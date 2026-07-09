package com.agroSystem.app.features.catalog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.agroSystem.app.R
import com.agroSystem.app.data.models.Product
import com.agroSystem.app.data.models.bindImageTo
import com.google.android.material.card.MaterialCardView

class ProductGridAdapter(
    private var products: List<Product>,
    private var favoriteIds: List<Int>,
    private var cartQuantities: Map<Product, Int>,
    private val onProductClick: (Product) -> Unit,
    private val onFavoriteClick: (Product) -> Unit,
    private val onAddClick: (Product) -> Unit,
    private val onRemoveClick: (Product) -> Unit
) : RecyclerView.Adapter<ProductGridAdapter.ProductViewHolder>() {

    fun updateData(newProducts: List<Product>, newFavoriteIds: List<Int>, newCartQuantities: Map<Product, Int>) {
        this.products = newProducts
        this.favoriteIds = newFavoriteIds
        this.cartQuantities = newCartQuantities
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product_card, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(products[position])
    }

    override fun getItemCount(): Int = products.size

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardProduct: MaterialCardView = itemView.findViewById(R.id.card_product)
        private val imageIllustration: ImageView = itemView.findViewById(R.id.image_illustration)
        private val btnFavorite: ImageView = itemView.findViewById(R.id.btn_favorite)
        private val textRating: TextView = itemView.findViewById(R.id.text_rating)
        private val textTitle: TextView = itemView.findViewById(R.id.text_title)
        private val textFarmer: TextView = itemView.findViewById(R.id.text_farmer)
        private val textPrice: TextView = itemView.findViewById(R.id.text_price)
        private val textOriginalPrice: TextView = itemView.findViewById(R.id.text_original_price)
        private val textUnit: TextView = itemView.findViewById(R.id.text_unit)
        
        private val btnAddToCart: MaterialCardView = itemView.findViewById(R.id.btn_add_to_cart)
        private val layoutStepper: LinearLayout = itemView.findViewById(R.id.layout_stepper)
        private val btnStepperMinus: ImageView = itemView.findViewById(R.id.btn_stepper_minus)
        private val textStepperCount: TextView = itemView.findViewById(R.id.text_stepper_count)
        private val btnStepperPlus: ImageView = itemView.findViewById(R.id.btn_stepper_plus)

        fun bind(product: Product) {
            product.bindImageTo(imageIllustration)
            textTitle.text = product.name
            textFarmer.text = product.farmer
            textRating.text = product.rating
            textPrice.text = "Rp ${product.price}"
            textUnit.text = "/ ${product.unit}"

            if (product.isDiscounted) {
                textOriginalPrice.visibility = View.VISIBLE
                textOriginalPrice.text = "Rp ${product.originalPrice}"
                textOriginalPrice.paintFlags = textOriginalPrice.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                textOriginalPrice.visibility = View.GONE
            }

            val isFavorite = favoriteIds.contains(product.id)
            btnFavorite.setImageResource(
                if (isFavorite) android.R.drawable.btn_star_big_on else android.R.drawable.btn_star_big_off
            )

            // Favoriting color filter
            if (isFavorite) {
                btnFavorite.setColorFilter(android.graphics.Color.RED)
            } else {
                btnFavorite.clearColorFilter()
            }

            // Stepper state
            val cartQty = cartQuantities.entries.firstOrNull { it.key.id == product.id }?.value ?: 0
            if (cartQty > 0) {
                btnAddToCart.visibility = View.GONE
                layoutStepper.visibility = View.VISIBLE
                textStepperCount.text = "$cartQty pcs"
            } else {
                btnAddToCart.visibility = View.VISIBLE
                layoutStepper.visibility = View.GONE
            }

            cardProduct.setOnClickListener { onProductClick(product) }
            btnFavorite.setOnClickListener { onFavoriteClick(product) }
            btnAddToCart.setOnClickListener { onAddClick(product) }
            btnStepperPlus.setOnClickListener { onAddClick(product) }
            btnStepperMinus.setOnClickListener { onRemoveClick(product) }
        }
    }
}
