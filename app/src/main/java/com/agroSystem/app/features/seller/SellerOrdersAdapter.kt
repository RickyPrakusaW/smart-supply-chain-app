package com.agroSystem.app.features.seller

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.agroSystem.app.R
import com.agroSystem.app.data.remote.OrderItemResponse
import com.google.android.material.card.MaterialCardView
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class SellerOrdersAdapter(
    private var orders: List<OrderItemResponse>,
    private val onItemClick: (OrderItemResponse) -> Unit,
    private val onShipClick: (OrderItemResponse) -> Unit
) : RecyclerView.Adapter<SellerOrdersAdapter.ViewHolder>() {

    fun updateOrders(newOrders: List<OrderItemResponse>) {
        orders = newOrders
        notifyDataSetChanged()
    }

    fun getOrdersList(): List<OrderItemResponse> = orders

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_seller_order, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val order = orders[position]
        holder.bind(order, onShipClick)
        holder.itemView.setOnClickListener {
            onItemClick(order)
        }
    }

    override fun getItemCount(): Int = orders.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textOrderId: TextView = itemView.findViewById(R.id.text_order_id)
        private val textOrderDate: TextView = itemView.findViewById(R.id.text_order_date)
        private val textOrderItems: TextView = itemView.findViewById(R.id.text_order_items)
        private val textTotalAmount: TextView = itemView.findViewById(R.id.text_total_amount)
        private val cardStatusBadge: MaterialCardView = itemView.findViewById(R.id.card_status_badge)
        private val textStatus: TextView = itemView.findViewById(R.id.text_status)
        private val btnShipOrder: Button = itemView.findViewById(R.id.btn_ship_order)

        fun bind(order: OrderItemResponse, onShipClick: (OrderItemResponse) -> Unit) {
            textOrderId.text = order.orderId
            textOrderDate.text = formatDate(order.createdAt)
            
            // Build items list string
            val itemsBuilder = StringBuilder()
            order.items?.forEach { item ->
                itemsBuilder.append("• ").append(item.name).append(" x ").append(item.quantity).append("\n")
            }
            textOrderItems.text = itemsBuilder.toString().trim()

            textTotalAmount.text = "Rp " + NumberFormat.getNumberInstance(Locale("id", "ID")).format(order.amount)

            // Setup Status & Actions
            val context = itemView.context
            when (order.status.lowercase()) {
                "pending" -> {
                    textStatus.text = "Menunggu Pembayaran"
                    textStatus.setTextColor(ContextCompat.getColor(context, R.color.color_text_muted))
                    cardStatusBadge.setCardBackgroundColor(ContextCompat.getColor(context, R.color.color_surface_warm))
                    btnShipOrder.visibility = View.GONE
                }
                "success" -> {
                    textStatus.text = "Perlu Dikirim"
                    textStatus.setTextColor(ContextCompat.getColor(context, R.color.color_primary_green))
                    cardStatusBadge.setCardBackgroundColor(ContextCompat.getColor(context, R.color.color_olive_light))
                    btnShipOrder.visibility = View.VISIBLE
                    btnShipOrder.setOnClickListener { onShipClick(order) }
                }
                "shipped" -> {
                    textStatus.text = "Sedang Dikirim"
                    textStatus.setTextColor(ContextCompat.getColor(context, R.color.white))
                    cardStatusBadge.setCardBackgroundColor(ContextCompat.getColor(context, R.color.color_primary_green))
                    btnShipOrder.visibility = View.GONE
                }
                "completed" -> {
                    textStatus.text = "Selesai"
                    textStatus.setTextColor(ContextCompat.getColor(context, R.color.color_primary_green))
                    cardStatusBadge.setCardBackgroundColor(ContextCompat.getColor(context, R.color.color_olive_light))
                    btnShipOrder.visibility = View.GONE
                }
                else -> {
                    textStatus.text = order.status.uppercase()
                    textStatus.setTextColor(ContextCompat.getColor(context, R.color.color_red_error))
                    cardStatusBadge.setCardBackgroundColor(ContextCompat.getColor(context, R.color.color_surface_warm))
                    btnShipOrder.visibility = View.GONE
                }
            }
        }

        private fun formatDate(dateStr: String): String {
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
                inputFormat.timeZone = TimeZone.getTimeZone("UTC")
                val date = inputFormat.parse(dateStr)
                val outputFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
                outputFormat.format(date!!)
            } catch (e: Exception) {
                dateStr
            }
        }
    }
}
