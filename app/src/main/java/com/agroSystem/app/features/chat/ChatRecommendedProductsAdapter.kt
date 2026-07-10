package com.agroSystem.app.features.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.agroSystem.app.R
import com.agroSystem.app.data.models.Product
import com.agroSystem.app.data.models.bindImageTo
import java.text.NumberFormat
import java.util.Locale

class ChatRecommendedProductsAdapter(
    private val products: List<Product>,
    private val onProductClick: (Product) -> Unit
) : RecyclerView.Adapter<ChatRecommendedProductsAdapter.ProductViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_recommended_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(products[position], onProductClick)
    }

    override fun getItemCount(): Int = products.size

    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgProduct: ImageView = itemView.findViewById(R.id.img_product)
        private val txtProductName: TextView = itemView.findViewById(R.id.txt_product_name)
        private val txtProductPrice: TextView = itemView.findViewById(R.id.txt_product_price)

        fun bind(product: Product, onProductClick: (Product) -> Unit) {
            product.bindImageTo(imgProduct)
            txtProductName.text = product.name
            
            val formattedPrice = NumberFormat.getNumberInstance(Locale("id", "ID")).format(product.price)
            txtProductPrice.text = "Rp $formattedPrice / ${product.unit}"

            itemView.setOnClickListener {
                onProductClick(product)
            }
        }
    }
}
