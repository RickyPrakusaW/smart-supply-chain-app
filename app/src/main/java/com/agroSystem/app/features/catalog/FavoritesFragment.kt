package com.agroSystem.app.features.catalog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.agroSystem.app.R
import com.agroSystem.app.data.models.Product
import com.agroSystem.app.features.shared.MainSharedViewModel

class FavoritesFragment : Fragment() {

    private val sharedViewModel: MainSharedViewModel by activityViewModels()

    private lateinit var rvFavorites: RecyclerView
    private lateinit var layoutEmpty: View
    private lateinit var adapter: ProductGridAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_favorites, container, false)

        rvFavorites = view.findViewById(R.id.rv_favorites)
        layoutEmpty = view.findViewById(R.id.layout_empty_favorites)

        setupRecyclerView()
        observeStates()

        return view
    }

    private fun setupRecyclerView() {
        rvFavorites.layoutManager = GridLayoutManager(requireContext(), 2)
        adapter = ProductGridAdapter(
            products = emptyList(),
            favoriteIds = emptyList(),
            cartQuantities = emptyMap(),
            onProductClick = { product -> navigateToProductDetail(product) },
            onFavoriteClick = { product -> sharedViewModel.toggleFavorite(product.id) },
            onAddClick = { product -> sharedViewModel.addProductToCart(product) },
            onRemoveClick = { product -> sharedViewModel.removeProductFromCart(product) }
        )
        rvFavorites.adapter = adapter
    }

    private fun observeStates() {
        sharedViewModel.favoriteProductIds.observe(viewLifecycleOwner) { updateList() }
        sharedViewModel.cartItems.observe(viewLifecycleOwner) { updateList() }
    }

    private fun updateList() {
        val favIds = sharedViewModel.favoriteProductIds.value ?: emptyList()
        val favProducts = sharedViewModel.allProducts.filter { favIds.contains(it.id) }

        if (favProducts.isEmpty()) {
            layoutEmpty.visibility = View.VISIBLE
            rvFavorites.visibility = View.GONE
        } else {
            layoutEmpty.visibility = View.GONE
            rvFavorites.visibility = View.VISIBLE
            adapter.updateData(
                favProducts,
                favIds,
                sharedViewModel.cartItems.value ?: emptyMap()
            )
        }
    }

    private fun navigateToProductDetail(product: Product) {
        val navController = parentFragment?.findNavController()
        navController?.navigate(R.id.action_homeFragment_to_productDetailFragment, bundleOf("productId" to product.id))
    }
}
