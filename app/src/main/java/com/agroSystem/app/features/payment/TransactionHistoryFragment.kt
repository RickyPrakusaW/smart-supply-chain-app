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
import com.agroSystem.app.data.remote.ApiClient
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

    private fun loadTransactionHistory() {
        val currentUser = authViewModel.currentUser.value
        if (currentUser == null) {
            Toast.makeText(requireContext(), "Silakan login terlebih dahulu!", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
            return
        }

        progressLoader.visibility = View.VISIBLE
        rvTransactions.visibility = View.GONE
        layoutEmptyHistory.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val response = ApiClient.authApiService.getUserOrders(currentUser.id)
                progressLoader.visibility = View.GONE

                if (response.success && response.data != null) {
                    val ordersList = response.data
                    if (ordersList.isEmpty()) {
                        layoutEmptyHistory.visibility = View.VISIBLE
                        rvTransactions.visibility = View.GONE
                    } else {
                        transactionAdapter.updateOrders(ordersList)
                        rvTransactions.visibility = View.VISIBLE
                        layoutEmptyHistory.visibility = View.GONE
                    }
                } else {
                    layoutEmptyHistory.visibility = View.VISIBLE
                    Toast.makeText(requireContext(), "Gagal memuat riwayat transaksi.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                progressLoader.visibility = View.GONE
                layoutEmptyHistory.visibility = View.VISIBLE
                Log.e("TransactionHistory", "Error fetching orders list", e)
                Toast.makeText(requireContext(), "Gagal menghubungkan ke server: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
