package com.agroSystem.app.features.cart

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.agroSystem.app.R
import com.agroSystem.app.data.models.Product
import com.agroSystem.app.data.models.bindImageTo

class CartItemAdapter(
    private var items: List<Pair<Product, Int>>,
    private val onAddClick: (Product) -> Unit,
    private val onRemoveClick: (Product) -> Unit
) : RecyclerView.Adapter<CartItemAdapter.CartItemViewHolder>() {

    fun updateData(newItems: List<Pair<Product, Int>>) {
        this.items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cart_product, parent, false)
        return CartItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartItemViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item.first, item.second)
    }

    override fun getItemCount(): Int = items.size

    inner class CartItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageIllustration: ImageView = itemView.findViewById(R.id.image_illustration)
        private val textTitle: TextView = itemView.findViewById(R.id.text_title)
        private val textSubtitle: TextView = itemView.findViewById(R.id.text_subtitle)
        private val textStepperCount: TextView = itemView.findViewById(R.id.text_stepper_count)
        private val btnStepperMinus: View = itemView.findViewById(R.id.btn_stepper_minus)
        private val btnStepperPlus: View = itemView.findViewById(R.id.btn_stepper_plus)
        private val textItemTotalPrice: TextView = itemView.findViewById(R.id.text_item_total_price)

        fun bind(product: Product, quantity: Int) {
            product.bindImageTo(imageIllustration)
            textTitle.text = product.name
            textSubtitle.text = "Rp ${product.price} / ${product.unit}"
            textStepperCount.text = "$quantity pcs"
            textItemTotalPrice.text = "Rp ${product.price * quantity}"

            btnStepperPlus.setOnClickListener { onAddClick(product) }
            btnStepperMinus.setOnClickListener { onRemoveClick(product) }
        }
    }
}
