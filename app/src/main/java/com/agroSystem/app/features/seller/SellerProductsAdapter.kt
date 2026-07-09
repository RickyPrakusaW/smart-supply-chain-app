package com.agroSystem.app.features.seller

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.agroSystem.app.R
import com.agroSystem.app.data.models.Product
import com.agroSystem.app.data.models.bindImageTo
import com.google.android.material.card.MaterialCardView
import java.util.Locale

class SellerProductsAdapter(
    private var products: List<Product>,
    private val onEditClick: (Product) -> Unit,
    private val onDeleteClick: (Product) -> Unit
) : RecyclerView.Adapter<SellerProductsAdapter.ViewHolder>() {

    fun updateProducts(newProducts: List<Product>) {
        products = newProducts
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_seller_product, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = products[position]
        holder.bind(product, onEditClick, onDeleteClick)
    }

    override fun getItemCount(): Int = products.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageProduct: ImageView = itemView.findViewById(R.id.image_product)
        private val textTitle: TextView = itemView.findViewById(R.id.text_title)
        private val textCategory: TextView = itemView.findViewById(R.id.text_category)
        private val textPrice: TextView = itemView.findViewById(R.id.text_price)
        private val btnEdit: View = itemView.findViewById(R.id.btn_edit)
        private val btnDelete: View = itemView.findViewById(R.id.btn_delete)

        fun bind(
            product: Product,
            onEditClick: (Product) -> Unit,
            onDeleteClick: (Product) -> Unit
        ) {
            product.bindImageTo(imageProduct)
            textTitle.text = product.name
            textCategory.text = product.category
            textPrice.text = "Rp ${formatPrice(product.price)} / ${product.unit}"

            btnEdit.setOnClickListener { onEditClick(product) }
            btnDelete.setOnClickListener { onDeleteClick(product) }
        }

        private fun formatPrice(price: Int): String {
            return java.text.NumberFormat.getNumberInstance(Locale("id", "ID")).format(price)
        }
    }
}
