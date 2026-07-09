package com.agroSystem.app.features.payment

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.agroSystem.app.R
import com.agroSystem.app.data.remote.OrderItemResponse
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.gson.Gson
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class OrderDetailBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var order: OrderItemResponse

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val orderJson = arguments?.getString("order_json")
        if (orderJson != null) {
            order = Gson().fromJson(orderJson, OrderItemResponse::class.java)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.layout_order_detail_bottom_sheet, container, false)

        val textOrderId: TextView = view.findViewById(R.id.text_order_id)
        val textOrderDate: TextView = view.findViewById(R.id.text_order_date)
        val textStatus: TextView = view.findViewById(R.id.text_status)
        val cardStatusBadge: MaterialCardView = view.findViewById(R.id.card_status_badge)
        val textShippingAddress: TextView = view.findViewById(R.id.text_shipping_address)
        val layoutItemsContainer: LinearLayout = view.findViewById(R.id.layout_items_container)
        val textSubtotal: TextView = view.findViewById(R.id.text_subtotal)
        val textGrandTotal: TextView = view.findViewById(R.id.text_grand_total)
        val btnPayNow: MaterialButton = view.findViewById(R.id.btn_pay_now)

        // Bind basic details
        textOrderId.text = order.orderId
        textOrderDate.text = formatTimestamp(order.createdAt)

        // Bind status and badge styling
        when (order.status.lowercase(Locale.getDefault())) {
            "success", "settlement" -> {
                textStatus.text = "SUKSES"
                textStatus.setTextColor(Color.parseColor("#2E7D32"))
                cardStatusBadge.setCardBackgroundColor(ColorStateList.valueOf(Color.parseColor("#E8F5E9")))
            }
            "failed", "deny", "cancel", "expire" -> {
                textStatus.text = "GAGAL"
                textStatus.setTextColor(Color.parseColor("#C62828"))
                cardStatusBadge.setCardBackgroundColor(ColorStateList.valueOf(Color.parseColor("#FFEBEE")))
            }
            else -> {
                textStatus.text = "PENDING"
                textStatus.setTextColor(Color.parseColor("#E65100"))
                cardStatusBadge.setCardBackgroundColor(ColorStateList.valueOf(Color.parseColor("#FFF3E0")))
            }
        }

        // Dynamically inflate order items rows
        layoutItemsContainer.removeAllViews()
        val itemsList = order.items ?: emptyList()
        var calculatedSubtotal = 0
        itemsList.forEach { item ->
            val rowView = inflater.inflate(R.layout.item_order_detail_row, layoutItemsContainer, false)
            rowView.findViewById<TextView>(R.id.text_item_name).text = "${item.name} x${item.quantity}"
            val itemCost = item.price * item.quantity
            calculatedSubtotal += itemCost
            rowView.findViewById<TextView>(R.id.text_item_price).text = "Rp ${formatPrice(itemCost)}"
            layoutItemsContainer.addView(rowView)
        }

        // Calculations
        val packingFee = 2000
        val finalSubtotal = if (calculatedSubtotal > 0) calculatedSubtotal else (order.amount - packingFee)
        textSubtotal.text = "Rp ${formatPrice(finalSubtotal)}"
        textGrandTotal.text = "Rp ${formatPrice(order.amount)}"

        // Payment continuation (if status is pending and redirect URL is provided)
        val redirectUrl = order.payment?.redirect_url
        if (order.status.lowercase(Locale.getDefault()) == "pending" && !redirectUrl.isNullOrEmpty()) {
            btnPayNow.visibility = View.VISIBLE
            btnPayNow.setOnClickListener {
                dismiss()
                val bundle = bundleOf("redirectUrl" to redirectUrl)
                findNavController().navigate(R.id.action_transactionHistoryFragment_to_paymentWebViewFragment, bundle)
            }
        } else {
            btnPayNow.visibility = View.GONE
        }

        return view
    }

    private fun formatPrice(price: Int): String {
        return NumberFormat.getNumberInstance(Locale("id", "ID")).format(price)
    }

    private fun formatTimestamp(timestampStr: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = inputFormat.parse(timestampStr) ?: return timestampStr
            
            val outputFormat = SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm", Locale("id", "ID"))
            outputFormat.timeZone = TimeZone.getDefault()
            outputFormat.format(date)
        } catch (e: Exception) {
            timestampStr
        }
    }

    companion object {
        fun newInstance(order: OrderItemResponse): OrderDetailBottomSheetFragment {
            val fragment = OrderDetailBottomSheetFragment()
            val args = Bundle().apply {
                putString("order_json", Gson().toJson(order))
            }
            fragment.arguments = args
            return fragment
        }
    }
}
