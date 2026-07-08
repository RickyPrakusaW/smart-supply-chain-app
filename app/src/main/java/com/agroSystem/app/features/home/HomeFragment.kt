package com.agroSystem.app.features.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.agroSystem.app.R
import com.agroSystem.app.features.catalog.CatalogFragment
import com.agroSystem.app.features.catalog.FavoritesFragment
import com.agroSystem.app.features.shared.MainSharedViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class HomeFragment : Fragment() {

    private val sharedViewModel: MainSharedViewModel by activityViewModels()

    private lateinit var bottomNav: BottomNavigationView
    private lateinit var layoutCartOverlay: MaterialCardView
    private lateinit var textCartInfo: TextView
    private lateinit var btnCheckout: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        bottomNav = view.findViewById(R.id.bottom_navigation)
        layoutCartOverlay = view.findViewById(R.id.layout_cart_overlay)
        textCartInfo = view.findViewById(R.id.text_cart_info)
        btnCheckout = view.findViewById(R.id.btn_checkout)

        // Set default fragment
        if (savedInstanceState == null) {
            loadFragment(DashboardFragment())
        }

        bottomNav.setOnItemSelectedListener { menuItem ->
            val fragment = when (menuItem.itemId) {
                R.id.menu_home -> DashboardFragment()
                R.id.menu_catalog -> CatalogFragment()
                R.id.menu_favorites -> FavoritesFragment()
                R.id.menu_profile -> ProfileFragment()
                else -> DashboardFragment()
            }
            loadFragment(fragment)
            true
        }

        // Observe cart items to toggle overlay visibility
        sharedViewModel.cartItems.observe(viewLifecycleOwner) { cartMap ->
            val totalItemCount = cartMap.values.sum()
            val totalPrice = cartMap.entries.sumOf { it.key.price * it.value }

            if (totalItemCount > 0) {
                layoutCartOverlay.visibility = View.VISIBLE
                textCartInfo.text = "Rp $totalPrice ($totalItemCount Item)"
            } else {
                layoutCartOverlay.visibility = View.GONE
            }
        }

        btnCheckout.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_cartFragment)
        }

        return view
    }

    private fun loadFragment(fragment: Fragment) {
        childFragmentManager.beginTransaction()
            .replace(R.id.container_swap, fragment)
            .commit()
    }
}
