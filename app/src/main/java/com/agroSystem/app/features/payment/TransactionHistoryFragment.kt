package com.agroSystem.app.features.payment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.agroSystem.app.R
import com.agroSystem.app.features.auth.AuthViewModel
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch

class TransactionHistoryFragment : Fragment() {

    private val authViewModel: AuthViewModel by activityViewModels()

    private lateinit var btnBack: MaterialCardView
    private lateinit var progressLoader: ProgressBar
    private lateinit var rvTransactions: RecyclerView
    private lateinit var layoutEmptyHistory: View

    private lateinit var transactionAdapter: TransactionHistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_transaction_history, container, false)

        btnBack = view.findViewById(R.id.btn_back)
        progressLoader = view.findViewById(R.id.progress_loader)
        rvTransactions = view.findViewById(R.id.rv_transactions)
        layoutEmptyHistory = view.findViewById(R.id.layout_empty_history)

        setupRecyclerView()
        loadTransactionHistory()

        btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        return view
    }

    private fun setupRecyclerView() {
        transactionAdapter = TransactionHistoryAdapter(emptyList()) { order ->
            val bottomSheet = OrderDetailBottomSheetFragment.newInstance(order)
            bottomSheet.show(childFragmentManager, "OrderDetailSheet")
        }
        rvTransactions.layoutManager = LinearLayoutManager(requireContext())
        rvTransactions.adapter = transactionAdapter
    }

    fun loadTransactionHistory() {
        val currentUser = authViewModel.currentUser.value
        if (currentUser == null) {
            Toast.makeText(requireContext(), "Silakan login terlebih dahulu!", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
            return
        }

        progressLoader.visibility = View.VISIBLE
        rvTransactions.visibility = View.GONE
        layoutEmptyHistory.visibility = View.GONE

        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        db.collection("orders").whereEqualTo("userId", currentUser.id).get()
            .addOnSuccessListener { result ->
                progressLoader.visibility = View.GONE
                try {
                    val ordersList = result.map { doc ->
                        val itemsRaw = doc.get("items") as? List<Map<String, Any>>
                        val checkoutItems = itemsRaw?.map { itemMap ->
                            com.agroSystem.app.data.remote.CheckoutItem(
                                id = (itemMap["id"] as? Long)?.toInt() ?: 0,
                                name = itemMap["name"] as? String ?: "",
                                price = (itemMap["price"] as? Long)?.toInt() ?: 0,
                                quantity = (itemMap["quantity"] as? Long)?.toInt() ?: 0,
                                ownerId = itemMap["ownerId"] as? String
                            )
                        }
                        
                        com.agroSystem.app.data.remote.OrderItemResponse(
                            orderId = doc.getString("orderId") ?: doc.id,
                            userId = doc.getString("userId"),
                            amount = doc.getLong("amount")?.toInt() ?: 0,
                            status = doc.getString("status") ?: "pending",
                            createdAt = doc.getString("createdAt") ?: doc.getTimestamp("createdAt")?.toDate()?.toString() ?: "",
                            items = checkoutItems,
                            payment = null
                        )
                    }

                    if (ordersList.isEmpty()) {
                        layoutEmptyHistory.visibility = View.VISIBLE
                        rvTransactions.visibility = View.GONE
                    } else {
                        transactionAdapter.updateOrders(ordersList)
                        rvTransactions.visibility = View.VISIBLE
                        layoutEmptyHistory.visibility = View.GONE
                    }
                } catch (e: Exception) {
                    Log.e("TransactionHistory", "Error mapping orders list", e)
                    layoutEmptyHistory.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener { e ->
                progressLoader.visibility = View.GONE
                layoutEmptyHistory.visibility = View.VISIBLE
                Log.e("TransactionHistory", "Error fetching orders list", e)
                Toast.makeText(requireContext(), "Gagal menghubungkan ke database: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
