package com.agroSystem.app.features.seller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
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
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class SellerProductsFragment : Fragment() {

    private val authViewModel: AuthViewModel by activityViewModels()
    private val sharedViewModel: MainSharedViewModel by activityViewModels()

    private lateinit var btnBack: MaterialCardView
    private lateinit var progressLoader: ProgressBar
    private lateinit var rvSellerProducts: RecyclerView
    private lateinit var layoutEmptySeller: View
    private lateinit var btnAddProduct: MaterialButton

    private lateinit var sellerAdapter: SellerProductsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_seller_products, container, false)

        btnBack = view.findViewById(R.id.btn_back)
        progressLoader = view.findViewById(R.id.progress_loader)
        rvSellerProducts = view.findViewById(R.id.rv_seller_products)
        layoutEmptySeller = view.findViewById(R.id.layout_empty_seller)
        btnAddProduct = view.findViewById(R.id.btn_add_product)

        setupRecyclerView()
        observeProducts()

        btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        btnAddProduct.setOnClickListener {
            // Navigate to AddEditProductFragment with ID -1 (signifying creation)
            navigateToAddEdit(-1)
        }

        return view
    }

    private fun setupRecyclerView() {
        sellerAdapter = SellerProductsAdapter(
            products = emptyList(),
            onEditClick = { product -> navigateToAddEdit(product.id) },
            onDeleteClick = { product -> confirmDeleteProduct(product) }
        )
        rvSellerProducts.layoutManager = LinearLayoutManager(requireContext())
        rvSellerProducts.adapter = sellerAdapter
    }

    private fun observeProducts() {
        val currentUser = authViewModel.currentUser.value
        if (currentUser == null) {
            Toast.makeText(requireContext(), "Silakan login terlebih dahulu!", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
            return
        }

        sharedViewModel.productsList.observe(viewLifecycleOwner) { prodsList ->
            // Filter products matching current seller ownerId
            val sellerProducts = prodsList.filter { it.ownerId == currentUser.id }
            
            if (sellerProducts.isEmpty()) {
                layoutEmptySeller.visibility = View.VISIBLE
                rvSellerProducts.visibility = View.GONE
            } else {
                sellerAdapter.updateProducts(sellerProducts)
                rvSellerProducts.visibility = View.VISIBLE
                layoutEmptySeller.visibility = View.GONE
            }
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
}
