package com.agroSystem.app.features.seller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.agroSystem.app.R
import com.agroSystem.app.data.models.Product
import com.agroSystem.app.features.auth.AuthViewModel
import com.agroSystem.app.features.shared.MainSharedViewModel
import com.agroSystem.app.features.payment.OrderDetailBottomSheetFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

import com.google.android.material.tabs.TabLayout

class SellerProductsFragment : Fragment() {

    private val authViewModel: AuthViewModel by activityViewModels()
    private val sharedViewModel: MainSharedViewModel by activityViewModels()

    private lateinit var tabSeller: TabLayout
    private lateinit var btnBack: MaterialCardView
    private lateinit var progressLoader: ProgressBar
    private lateinit var rvSellerProducts: RecyclerView
    private lateinit var layoutEmptySeller: View
    private lateinit var btnAddProduct: MaterialButton

    private lateinit var sellerAdapter: SellerProductsAdapter
    private lateinit var ordersAdapter: SellerOrdersAdapter
    private var currentTab = 0 // 0 for Products, 1 for Incoming Orders

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_seller_products, container, false)

        tabSeller = view.findViewById(R.id.tab_seller)
        btnBack = view.findViewById(R.id.btn_back)
        progressLoader = view.findViewById(R.id.progress_loader)
        rvSellerProducts = view.findViewById(R.id.rv_seller_products)
        layoutEmptySeller = view.findViewById(R.id.layout_empty_seller)
        btnAddProduct = view.findViewById(R.id.btn_add_product)

        setupRecyclerViews()
        setupTabListener()
        observeData()

        btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        btnAddProduct.setOnClickListener {
            navigateToAddEdit(-1)
        }

        return view
    }

    private fun setupRecyclerViews() {
        sellerAdapter = SellerProductsAdapter(
            products = emptyList(),
            onEditClick = { product -> navigateToAddEdit(product.id) },
            onDeleteClick = { product -> confirmDeleteProduct(product) }
        )
        ordersAdapter = SellerOrdersAdapter(
            orders = emptyList(),
            onItemClick = { order -> showOrderDetail(order) },
            onShipClick = { order -> confirmShipOrder(order) }
        )
        rvSellerProducts.layoutManager = LinearLayoutManager(requireContext())
        rvSellerProducts.adapter = sellerAdapter
    }

    private fun setupTabListener() {
        tabSeller.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentTab = tab?.position ?: 0
                updateUIForCurrentTab()
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun updateUIForCurrentTab() {
        val currentUser = authViewModel.currentUser.value ?: return
        if (currentTab == 0) {
            btnAddProduct.visibility = View.VISIBLE
            rvSellerProducts.adapter = sellerAdapter
            // Force refresh products list mapping
            val prodsList = sharedViewModel.productsList.value ?: emptyList()
            val sellerProducts = prodsList.filter { it.ownerId == currentUser.id }
            toggleEmptyState(sellerProducts.isEmpty())
        } else {
            btnAddProduct.visibility = View.GONE
            rvSellerProducts.adapter = ordersAdapter
            // Fetch seller orders
            progressLoader.visibility = View.VISIBLE
            sharedViewModel.fetchSellerOrders(currentUser.id)
        }
    }

    private fun observeData() {
        val currentUser = authViewModel.currentUser.value
        if (currentUser == null) {
            Toast.makeText(requireContext(), "Silakan login terlebih dahulu!", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
            return
        }

        // Observe Products List
        sharedViewModel.productsList.observe(viewLifecycleOwner) { prodsList ->
            if (currentTab == 0) {
                val sellerProducts = prodsList.filter { it.ownerId == currentUser.id }
                sellerAdapter.updateProducts(sellerProducts)
                toggleEmptyState(sellerProducts.isEmpty())
            }
        }

        // Observe Incoming Seller Orders
        sharedViewModel.sellerOrders.observe(viewLifecycleOwner) { orders ->
            progressLoader.visibility = View.GONE
            if (currentTab == 1) {
                ordersAdapter.updateOrders(orders)
                toggleEmptyState(orders.isEmpty())
            }
        }
    }

    private fun toggleEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            layoutEmptySeller.visibility = View.VISIBLE
            rvSellerProducts.visibility = View.GONE
            if (currentTab == 1) {
                layoutEmptySeller.findViewById<TextView>(android.R.id.text1)?.text = "Belum Ada Pesanan Masuk"
            }
        } else {
            rvSellerProducts.visibility = View.VISIBLE
            layoutEmptySeller.visibility = View.GONE
        }
    }

    private fun navigateToAddEdit(productId: Int) {
        val bundle = bundleOf("productId" to productId)
        findNavController().navigate(R.id.action_sellerProductsFragment_to_addEditProductFragment, bundle)
    }

    private fun confirmDeleteProduct(product: Product) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Produk")
            .setMessage("Apakah Anda yakin ingin menghapus produk '${product.name}'?")
            .setPositiveButton("Hapus") { _, _ ->
                progressLoader.visibility = View.VISIBLE
                sharedViewModel.deleteProduct(product.id) { success ->
                    progressLoader.visibility = View.GONE
                    if (success) {
                        Toast.makeText(requireContext(), "Produk berhasil dihapus!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Gagal menghapus produk.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun confirmShipOrder(order: com.agroSystem.app.data.remote.OrderItemResponse) {
        AlertDialog.Builder(requireContext())
            .setTitle("Kirim Barang")
            .setMessage("Konfirmasi bahwa Anda telah menyerahkan pesanan '${order.orderId}' ke kurir pengiriman?")
            .setPositiveButton("Kirim") { _, _ ->
                progressLoader.visibility = View.VISIBLE
                sharedViewModel.updateOrderStatus(order.orderId, "shipped") { success ->
                    if (success) {
                        Toast.makeText(requireContext(), "Status pengiriman dikonfirmasi!", Toast.LENGTH_SHORT).show()
                        val currentUser = authViewModel.currentUser.value
                        if (currentUser != null) {
                            sharedViewModel.fetchSellerOrders(currentUser.id)
                        }
                    } else {
                        progressLoader.visibility = View.GONE
                        Toast.makeText(requireContext(), "Gagal memperbarui status pengiriman.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showOrderDetail(order: com.agroSystem.app.data.remote.OrderItemResponse) {
        val bottomSheet = OrderDetailBottomSheetFragment.newInstance(order)
        bottomSheet.show(childFragmentManager, "OrderDetailSheet")
    }
}
