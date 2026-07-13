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
import androidx.fragment.app.activityViewModels
import com.agroSystem.app.features.shared.MainSharedViewModel
import com.agroSystem.app.features.auth.AuthViewModel
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import android.content.DialogInterface
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class OrderDetailBottomSheetFragment : BottomSheetDialogFragment() {

    private val sharedViewModel: MainSharedViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by activityViewModels()

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
        val btnConfirmReceive: MaterialButton = view.findViewById(R.id.btn_confirm_receive)

        // Dispute views
        val cardDisputeInfo: View = view.findViewById(R.id.card_dispute_info)
        val textDisputeReason: TextView = view.findViewById(R.id.text_dispute_reason)
        val layoutSellerDisputeActions: View = view.findViewById(R.id.layout_seller_dispute_actions)
        val btnRejectDispute: MaterialButton = view.findViewById(R.id.btn_reject_dispute)
        val btnAcceptDispute: MaterialButton = view.findViewById(R.id.btn_accept_dispute)

        // Bind basic details
        textOrderId.text = order.orderId
        textOrderDate.text = formatTimestamp(order.createdAt)

        // Bind status and badge styling
        val normalizedStatus = order.status.lowercase(Locale.getDefault())
        when (normalizedStatus) {
            "success", "settlement" -> {
                textStatus.text = "PERLU DIKIRIM"
                textStatus.setTextColor(Color.parseColor("#2E7D32"))
                cardStatusBadge.setCardBackgroundColor(ColorStateList.valueOf(Color.parseColor("#E8F5E9")))
            }
            "shipped" -> {
                textStatus.text = "SEDANG DIKIRIM"
                textStatus.setTextColor(Color.parseColor("#E65100"))
                cardStatusBadge.setCardBackgroundColor(ColorStateList.valueOf(Color.parseColor("#FFF3E0")))
            }
            "completed" -> {
                textStatus.text = "SELESAI"
                textStatus.setTextColor(Color.parseColor("#2E7D32"))
                cardStatusBadge.setCardBackgroundColor(ColorStateList.valueOf(Color.parseColor("#E8F5E9")))
            }
            "dispute" -> {
                textStatus.text = "KOMPLAIN"
                textStatus.setTextColor(Color.parseColor("#E65100"))
                cardStatusBadge.setCardBackgroundColor(ColorStateList.valueOf(Color.parseColor("#FFF3E0")))
            }
            "refunded" -> {
                textStatus.text = "KOMPLAIN DISETUJUI"
                textStatus.setTextColor(Color.parseColor("#C62828"))
                cardStatusBadge.setCardBackgroundColor(ColorStateList.valueOf(Color.parseColor("#FFEBEE")))
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

        // Check if the current user is the buyer of this order
        val currentUser = authViewModel.currentUser.value
        val isBuyer = currentUser != null && currentUser.id == order.userId
        val isSeller = currentUser != null && currentUser.role == "Petani"

        // Render Dispute text if exists
        if (!order.disputeReason.isNullOrEmpty()) {
            cardDisputeInfo.visibility = View.VISIBLE
            textDisputeReason.text = order.disputeReason
        } else {
            cardDisputeInfo.visibility = View.GONE
        }

        // Payment continuation (if status is pending and redirect URL is provided)
        val redirectUrl = order.payment?.redirect_url
        if (normalizedStatus == "pending" && !redirectUrl.isNullOrEmpty() && isBuyer) {
            btnPayNow.visibility = View.VISIBLE
            btnPayNow.setOnClickListener {
                dismiss()
                val bundle = bundleOf("redirectUrl" to redirectUrl)
                findNavController().navigate(R.id.action_transactionHistoryFragment_to_paymentWebViewFragment, bundle)
            }
        } else {
            btnPayNow.visibility = View.GONE
        }

        // Receipt confirmation action (if status is shipped)
        if (normalizedStatus == "shipped" && isBuyer) {
            btnConfirmReceive.visibility = View.VISIBLE
            btnConfirmReceive.setOnClickListener {
                AlertDialog.Builder(requireContext())
                    .setTitle("Konfirmasi Terima Barang")
                    .setMessage("Apakah Anda yakin pesanan sudah sampai dan ingin menyelesaikan transaksi?")
                    .setPositiveButton("Ya, Selesai") { _, _ ->
                        sharedViewModel.updateOrderStatus(order.orderId, "completed") { success ->
                            if (success) {
                                Toast.makeText(requireContext(), "Transaksi selesai!", Toast.LENGTH_SHORT).show()
                                dismiss()
                            } else {
                                Toast.makeText(requireContext(), "Gagal menyelesaikan transaksi.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    .setNegativeButton("Batal", null)
                    .show()
            }
        } else {
            btnConfirmReceive.visibility = View.GONE
        }

        // Seller Dispute Actions Handling
        if (normalizedStatus == "dispute" && isSeller) {
            layoutSellerDisputeActions.visibility = View.VISIBLE
            
            btnAcceptDispute.setOnClickListener {
                AlertDialog.Builder(requireContext())
                    .setTitle("Terima Komplain")
                    .setMessage("Apakah Anda yakin ingin menyetujui komplain ini dan memproses pengembalian dana?")
                    .setPositiveButton("Terima") { _, _ ->
                        sharedViewModel.updateOrderStatus(order.orderId, "refunded") { success ->
                            if (success) {
                                Toast.makeText(requireContext(), "Komplain diterima, status dana dikembalikan.", Toast.LENGTH_SHORT).show()
                                dismiss()
                            } else {
                                Toast.makeText(requireContext(), "Gagal memproses persetujuan.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    .setNegativeButton("Batal", null)
                    .show()
            }

            btnRejectDispute.setOnClickListener {
                AlertDialog.Builder(requireContext())
                    .setTitle("Tolak Komplain")
                    .setMessage("Apakah Anda yakin ingin menolak komplain ini dan mengembalikan status ke selesai?")
                    .setPositiveButton("Tolak Komplain") { _, _ ->
                        sharedViewModel.updateOrderStatus(order.orderId, "completed") { success ->
                            if (success) {
                                Toast.makeText(requireContext(), "Komplain ditolak, pesanan diselesaikan kembali.", Toast.LENGTH_SHORT).show()
                                dismiss()
                            } else {
                                Toast.makeText(requireContext(), "Gagal menolak komplain.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    .setNegativeButton("Batal", null)
                    .show()
            }
        } else {
            layoutSellerDisputeActions.visibility = View.GONE
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

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        val parent = parentFragment
        if (parent is TransactionHistoryFragment) {
            parent.loadTransactionHistory()
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
