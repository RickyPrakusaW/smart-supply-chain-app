package com.agroSystem.app.features.cart

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.agroSystem.app.R
import com.agroSystem.app.data.models.Product

class CartGroupAdapter(
    private var groupedItems: Map<String, List<Pair<Product, Int>>>,
    private val onAddClick: (Product) -> Unit,
    private val onRemoveClick: (Product) -> Unit
) : RecyclerView.Adapter<CartGroupAdapter.CartGroupViewHolder>() {

    private val keysList: List<String>
        get() = groupedItems.keys.toList()

    fun updateData(newGroupedItems: Map<String, List<Pair<Product, Int>>>) {
        this.groupedItems = newGroupedItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartGroupViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cart_group, parent, false)
        return CartGroupViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartGroupViewHolder, position: Int) {
        val farmerKey = keysList[position]
        val items = groupedItems[farmerKey] ?: emptyList()
        holder.bind(farmerKey, items)
    }

    override fun getItemCount(): Int = keysList.size

    inner class CartGroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textGroupTitle: TextView = itemView.findViewById(R.id.text_group_title)
        private val rvGroupItems: RecyclerView = itemView.findViewById(R.id.rv_group_items)

        fun bind(farmerName: String, items: List<Pair<Product, Int>>) {
            textGroupTitle.text = farmerName
            
            val context = itemView.context
            rvGroupItems.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            val childAdapter = CartItemAdapter(items, onAddClick, onRemoveClick)
            rvGroupItems.adapter = childAdapter
        }
    }
}
