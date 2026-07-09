package com.agroSystem.app.features.payment

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.agroSystem.app.R
import com.agroSystem.app.data.remote.OrderItemResponse
import com.google.android.material.card.MaterialCardView
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class TransactionHistoryAdapter(
    private var orders: List<OrderItemResponse>,
    private val onItemClick: (OrderItemResponse) -> Unit
) : RecyclerView.Adapter<TransactionHistoryAdapter.ViewHolder>() {

    fun updateOrders(newOrders: List<OrderItemResponse>) {
        orders = newOrders
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_transaction, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val order = orders[position]
        holder.bind(order)
        holder.itemView.setOnClickListener {
            onItemClick(order)
        }
    }

    override fun getItemCount(): Int = orders.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textOrderId: TextView = itemView.findViewById(R.id.text_order_id)
        private val textDate: TextView = itemView.findViewById(R.id.text_date)
        private val textItemsSummary: TextView = itemView.findViewById(R.id.text_items_summary)
        private val textTotalPrice: TextView = itemView.findViewById(R.id.text_total_price)
        private val textStatus: TextView = itemView.findViewById(R.id.text_status)
        private val cardStatusBadge: MaterialCardView = itemView.findViewById(R.id.card_status_badge)

        fun bind(order: OrderItemResponse) {
            textOrderId.text = order.orderId
            textTotalPrice.text = "Rp ${formatPrice(order.amount)}"
            textDate.text = formatTimestamp(order.createdAt)

            // Compile item names summary
            val summary = order.items?.joinToString { "${it.name} (${it.quantity}x)" } ?: "Belanja AgriMitra"
            textItemsSummary.text = summary

            // Color status badge dynamically
            when (order.status.lowercase(Locale.getDefault())) {
                "success", "settlement" -> {
                    textStatus.text = "SUKSES"
                    textStatus.setTextColor(Color.parseColor("#2E7D32")) // Dark green text
                    cardStatusBadge.setCardBackgroundColor(ColorStateList.valueOf(Color.parseColor("#E8F5E9"))) // Light green card
                }
                "failed", "deny", "cancel", "expire" -> {
                    textStatus.text = "GAGAL"
                    textStatus.setTextColor(Color.parseColor("#C62828")) // Dark red text
                    cardStatusBadge.setCardBackgroundColor(ColorStateList.valueOf(Color.parseColor("#FFEBEE"))) // Light red card
                }
                else -> {
                    // Pending status
                    textStatus.text = "PENDING"
                    textStatus.setTextColor(Color.parseColor("#E65100")) // Dark orange text
                    cardStatusBadge.setCardBackgroundColor(ColorStateList.valueOf(Color.parseColor("#FFF3E0"))) // Light orange card
                }
            }
        }

        private fun formatPrice(price: Int): String {
            return java.text.NumberFormat.getNumberInstance(Locale("id", "ID")).format(price)
        }

        private fun formatTimestamp(timestampStr: String): String {
            return try {
                // Try parsing standard ISO-8601 from Node.js
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                inputFormat.timeZone = TimeZone.getTimeZone("UTC")
                val date = inputFormat.parse(timestampStr) ?: return timestampStr
                
                val outputFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
                outputFormat.timeZone = TimeZone.getDefault()
                outputFormat.format(date)
            } catch (e: Exception) {
                timestampStr
            }
        }
    }
}
