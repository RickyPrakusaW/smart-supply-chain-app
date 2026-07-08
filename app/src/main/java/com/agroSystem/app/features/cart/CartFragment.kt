package com.agroSystem.app.features.cart

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.agroSystem.app.R
import com.agroSystem.app.data.models.Product
import com.agroSystem.app.features.shared.MainSharedViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial

class CartFragment : Fragment() {

    private val sharedViewModel: MainSharedViewModel by activityViewModels()

    private lateinit var layoutActiveCart: View
    private lateinit var layoutEmptyCart: View

    private lateinit var btnBack: View
    private lateinit var btnShare: View
    private lateinit var btnClear: View

    private lateinit var rvCartGroups: RecyclerView
    private lateinit var cartGroupAdapter: CartGroupAdapter

    // Packaging controls
    private lateinit var btnOptionPaperbag: View
    private lateinit var radioPaperbag: RadioButton
    private lateinit var btnOptionPlastic: View
    private lateinit var radioPlastic: RadioButton
    private lateinit var layoutPlasticSwitch: View
    private lateinit var switchPlasticReturn: SwitchMaterial

    // Pricing labels
    private lateinit var textSubtotalLabel: TextView
    private lateinit var textSubtotalValue: TextView
    private lateinit var layoutDiscount: View
    private lateinit var textDiscountValue: TextView
    private lateinit var textPackagingValue: TextView
    private lateinit var textTotalPayment: TextView
    private lateinit var btnCheckoutFinal: MaterialButton

    // Empty Cart controls
    private lateinit var btnEmptyBack: View
    private lateinit var btnStartShopping: MaterialButton

    // Undo elements
    private lateinit var layoutUndoCard: View
    private lateinit var textUndoMessage: TextView
    private lateinit var btnUndoAction: TextView

    private var selectedPackaging = 0 // 0 = Paperbag (Rp 2.000), 1 = Plastic (Rp 1.000)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_cart, container, false)

        bindViews(view)
        setupActions()
        setupRecyclerView()
        observeCartState()

        return view
    }

    private fun bindViews(view: View) {
        layoutActiveCart = view.findViewById(R.id.layout_active_cart)
        layoutEmptyCart = view.findViewById(R.id.layout_empty_cart)

        btnBack = view.findViewById(R.id.btn_back)
        btnShare = view.findViewById(R.id.btn_share)
        btnClear = view.findViewById(R.id.btn_clear)

        rvCartGroups = view.findViewById(R.id.rv_cart_groups)

        btnOptionPaperbag = view.findViewById(R.id.btn_option_paperbag)
        radioPaperbag = view.findViewById(R.id.radio_paperbag)
        btnOptionPlastic = view.findViewById(R.id.btn_option_plastic)
        radioPlastic = view.findViewById(R.id.radio_plastic)
        layoutPlasticSwitch = view.findViewById(R.id.layout_plastic_switch)
        switchPlasticReturn = view.findViewById(R.id.switch_plastic_return)

        textSubtotalLabel = view.findViewById(R.id.text_subtotal_label)
        textSubtotalValue = view.findViewById(R.id.text_subtotal_value)
        layoutDiscount = view.findViewById(R.id.layout_discount)
        textDiscountValue = view.findViewById(R.id.text_discount_value)
        textPackagingValue = view.findViewById(R.id.text_packaging_value)
        textTotalPayment = view.findViewById(R.id.text_total_payment)
        btnCheckoutFinal = view.findViewById(R.id.btn_checkout_final)

        btnEmptyBack = view.findViewById(R.id.btn_empty_back)
        btnStartShopping = view.findViewById(R.id.btn_start_shopping)

        layoutUndoCard = view.findViewById(R.id.layout_undo_card)
        textUndoMessage = view.findViewById(R.id.text_undo_message)
        btnUndoAction = view.findViewById(R.id.btn_undo_action)
    }

    private fun setupActions() {
        btnBack.setOnClickListener { findNavController().navigateUp() }
        btnEmptyBack.setOnClickListener { findNavController().navigateUp() }
        btnShare.setOnClickListener { Toast.makeText(requireContext(), "Tautan belanja disalin!", Toast.LENGTH_SHORT).show() }
        btnClear.setOnClickListener { sharedViewModel.clearCart() }

        btnStartShopping.setOnClickListener {
            // Back out to main catalog
            findNavController().navigateUp()
        }

        // Packaging option selections
        btnOptionPaperbag.setOnClickListener { setPackagingSelection(0) }
        radioPaperbag.setOnClickListener { setPackagingSelection(0) }
        btnOptionPlastic.setOnClickListener { setPackagingSelection(1) }
        radioPlastic.setOnClickListener { setPackagingSelection(1) }

        btnCheckoutFinal.setOnClickListener {
            sharedViewModel.clearCart()
            Toast.makeText(requireContext(), "Pesanan Anda berhasil dikirim ke mitra tani!", Toast.LENGTH_LONG).show()
            findNavController().navigateUp()
        }

        btnUndoAction.setOnClickListener {
            sharedViewModel.restoreRemovedProduct()
        }

        // Default state
        setPackagingSelection(0)
    }

    private fun setPackagingSelection(option: Int) {
        selectedPackaging = option
        if (option == 0) {
            radioPaperbag.isChecked = true
            radioPlastic.isChecked = false
            layoutPlasticSwitch.visibility = View.GONE
        } else {
            radioPaperbag.isChecked = false
            radioPlastic.isChecked = true
            layoutPlasticSwitch.visibility = View.VISIBLE
        }
        recalculatePricing()
    }

    private fun setupRecyclerView() {
        rvCartGroups.layoutManager = LinearLayoutManager(requireContext())
        cartGroupAdapter = CartGroupAdapter(
            groupedItems = emptyMap(),
            onAddClick = { product -> sharedViewModel.addProductToCart(product) },
            onRemoveClick = { product -> sharedViewModel.removeProductFromCart(product) }
        )
        rvCartGroups.adapter = cartGroupAdapter
    }

    private fun observeCartState() {
        sharedViewModel.cartItems.observe(viewLifecycleOwner) { cartMap ->
            val totalItemCount = cartMap.values.sum()

            if (totalItemCount == 0) {
                layoutActiveCart.visibility = View.GONE
                layoutEmptyCart.visibility = View.VISIBLE
            } else {
                layoutActiveCart.visibility = View.VISIBLE
                layoutEmptyCart.visibility = View.GONE

                // Group entries by farmer name
                val grouped = cartMap.entries.groupBy { it.key.farmer.split(",").first().trim() }
                val mapped = grouped.mapValues { entry ->
                    entry.value.map { Pair(it.key, it.value) }
                }

                cartGroupAdapter.updateData(mapped)
                recalculatePricing()
            }
        }

        // Undo states
        sharedViewModel.isUndoVisible.observe(viewLifecycleOwner) { visible ->
            val product = sharedViewModel.recentlyRemovedProduct.value
            if (visible && product != null) {
                layoutUndoCard.visibility = View.VISIBLE
                textUndoMessage.text = "Menghapus ${product.name.take(16)}..."
            } else {
                layoutUndoCard.visibility = View.GONE
            }
        }
    }

    private fun recalculatePricing() {
        val cartMap = sharedViewModel.cartItems.value ?: emptyMap()
        val totalItemCount = cartMap.values.sum()
        val rawSubtotal = cartMap.entries.sumOf { it.key.price * it.value }

        val packagingCost = if (selectedPackaging == 0) 2000 else 1000
        val discount = if (rawSubtotal > 30000) 5000 else 0
        val finalTotal = rawSubtotal + packagingCost - discount

        textSubtotalLabel.text = "Subtotal ($totalItemCount Barang)"
        textSubtotalValue.text = "Rp $rawSubtotal"
        textPackagingValue.text = "Rp $packagingCost"

        if (discount > 0) {
            layoutDiscount.visibility = View.VISIBLE
            textDiscountValue.text = "-Rp $discount"
        } else {
            layoutDiscount.visibility = View.GONE
        }

        textTotalPayment.text = "Rp $finalTotal"
        btnCheckoutFinal.text = "Pesan Sekarang - Rp $finalTotal"
    }
}
