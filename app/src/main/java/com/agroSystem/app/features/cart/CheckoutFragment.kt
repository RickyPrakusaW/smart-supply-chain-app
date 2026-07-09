package com.agroSystem.app.features.cart

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.agroSystem.app.R
import com.agroSystem.app.data.models.Product
import com.agroSystem.app.features.shared.MainSharedViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputLayout

class CheckoutFragment : Fragment() {

    private val sharedViewModel: MainSharedViewModel by activityViewModels()

    private lateinit var btnBack: MaterialCardView
    private lateinit var textAddress: TextView

    // Delivery Option Buttons
    private lateinit var btnDeliveryAllOnce: MaterialButton
    private lateinit var btnDeliverySplit: MaterialButton
    private lateinit var layoutDeliverySingle: LinearLayout
    private lateinit var layoutDeliverySplit: LinearLayout

    // Calendars, Time slots, Thumbnails containers
    private lateinit var layoutCalendarSingle: LinearLayout
    private lateinit var layoutTimeslotsSingle: LinearLayout
    private lateinit var layoutThumbnailsSingle: LinearLayout

    private lateinit var layoutCalendarSplit1: LinearLayout
    private lateinit var layoutTimeslotsSplit1: LinearLayout
    private lateinit var layoutThumbnailsSplit1: LinearLayout

    private lateinit var layoutCalendarSplit2: LinearLayout
    private lateinit var layoutTimeslotsSplit2: LinearLayout
    private lateinit var layoutThumbnailsSplit2: LinearLayout

    private lateinit var textItemsCountSingle: TextView
    private lateinit var textItemsCountSplit1: TextView
    private lateinit var textItemsCountSplit2: TextView

    // Bonuses and Promo Code
    private lateinit var btnPromoCode: View
    private lateinit var textPromoCode: TextView
    private lateinit var switchBonuses: SwitchMaterial

    // Payment Cards
    private lateinit var btnPayCash: MaterialCardView
    private lateinit var btnPayCard1: MaterialCardView
    private lateinit var btnPayCard2: MaterialCardView
    private lateinit var btnPayNew: MaterialCardView

    // Order Settings
    private lateinit var switchLeaveDoor: SwitchMaterial
    private lateinit var switchOtherPerson: SwitchMaterial
    private lateinit var btnToggleComment: View
    private lateinit var imageCommentArrow: ImageView
    private lateinit var layoutCheckoutComment: TextInputLayout

    // Pricing Detail
    private lateinit var btnTogglePricingDetail: View
    private lateinit var imagePricingArrow: ImageView
    private lateinit var layoutPricingDetailContent: LinearLayout
    private lateinit var rowPricingSplit1: LinearLayout
    private lateinit var rowPricingSplit2: LinearLayout
    private lateinit var rowCheckoutPromo: LinearLayout
    private lateinit var rowCheckoutBonus: LinearLayout

    private lateinit var textCheckoutSplit1Val: TextView
    private lateinit var textCheckoutSplit2Val: TextView
    private lateinit var textCheckoutPromoVal: TextView
    private lateinit var textCheckoutPackagingVal: TextView
    private lateinit var textCheckoutTotalPrice: TextView

    // Place Order Button
    private lateinit var btnPlaceOrder: MaterialButton

    // State Variables
    private var isSplitDelivery = false
    private var selectedDaySingle = 0
    private var selectedSlotSingle = 0
    private var selectedDaySplit1 = 0
    private var selectedSlotSplit1 = 0
    private var selectedDaySplit2 = 2
    private var selectedSlotSplit2 = 1

    private var selectedPaymentMethod = 0 // 0 = Cash, 1 = Card1, 2 = Card2
    private var isCommentExpanded = false
    private var isPricingExpanded = true
    private var isPromoApplied = false
    private var isBonusApplied = false

    private var packagingCost = 2000

    // Calendar Days Mock (Sab, Ming, Sen, Sel, Rab, Kam, Jum)
    private val calendarDays = listOf(
        Pair("23", "Sab"),
        Pair("24", "Ming"),
        Pair("25", "Sen"),
        Pair("26", "Sel"),
        Pair("27", "Rab"),
        Pair("28", "Kam"),
        Pair("29", "Jum")
    )

    private val timeSlots = listOf(
        "09:00 - 12:00",
        "12:00 - 15:00",
        "15:00 - 18:00",
        "18:00 - 21:00"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_checkout, container, false)
        packagingCost = arguments?.getInt("packagingCost") ?: 2000

        bindViews(view)
        setupActions()
        renderDeliveryData()
        recalculatePricing()

        return view
    }

    private fun bindViews(view: View) {
        btnBack = view.findViewById(R.id.btn_back)
        textAddress = view.findViewById(R.id.text_checkout_address)

        btnDeliveryAllOnce = view.findViewById(R.id.btn_delivery_all_once)
        btnDeliverySplit = view.findViewById(R.id.btn_delivery_split)
        layoutDeliverySingle = view.findViewById(R.id.layout_delivery_single)
        layoutDeliverySplit = view.findViewById(R.id.layout_delivery_split)

        layoutCalendarSingle = view.findViewById(R.id.layout_calendar_single)
        layoutTimeslotsSingle = view.findViewById(R.id.layout_timeslots_single)
        layoutThumbnailsSingle = view.findViewById(R.id.layout_thumbnails_single)

        layoutCalendarSplit1 = view.findViewById(R.id.layout_calendar_split1)
        layoutTimeslotsSplit1 = view.findViewById(R.id.layout_timeslots_split1)
        layoutThumbnailsSplit1 = view.findViewById(R.id.layout_thumbnails_split1)

        layoutCalendarSplit2 = view.findViewById(R.id.layout_calendar_split2)
        layoutTimeslotsSplit2 = view.findViewById(R.id.layout_timeslots_split2)
        layoutThumbnailsSplit2 = view.findViewById(R.id.layout_thumbnails_split2)

        textItemsCountSingle = view.findViewById(R.id.text_items_count_single)
        textItemsCountSplit1 = view.findViewById(R.id.text_items_count_split1)
        textItemsCountSplit2 = view.findViewById(R.id.text_items_count_split2)

        btnPromoCode = view.findViewById(R.id.btn_promo_code)
        textPromoCode = view.findViewById(R.id.text_promo_code)
        switchBonuses = view.findViewById(R.id.switch_bonuses)

        btnPayCash = view.findViewById(R.id.btn_pay_cash)
        btnPayCard1 = view.findViewById(R.id.btn_pay_card1)
        btnPayCard2 = view.findViewById(R.id.btn_pay_card2)
        btnPayNew = view.findViewById(R.id.btn_pay_new)

        switchLeaveDoor = view.findViewById(R.id.switch_leave_door)
        switchOtherPerson = view.findViewById(R.id.switch_other_person)
        btnToggleComment = view.findViewById(R.id.btn_toggle_comment)
        imageCommentArrow = view.findViewById(R.id.image_comment_arrow)
        layoutCheckoutComment = view.findViewById(R.id.layout_checkout_comment)

        btnTogglePricingDetail = view.findViewById(R.id.btn_toggle_pricing_detail)
        imagePricingArrow = view.findViewById(R.id.image_pricing_arrow)
        layoutPricingDetailContent = view.findViewById(R.id.layout_pricing_detail_content)
        rowPricingSplit1 = view.findViewById(R.id.row_pricing_split1)
        rowPricingSplit2 = view.findViewById(R.id.row_pricing_split2)
        rowCheckoutPromo = view.findViewById(R.id.row_checkout_promo)
        rowCheckoutBonus = view.findViewById(R.id.row_checkout_bonus)

        textCheckoutSplit1Val = view.findViewById(R.id.text_checkout_split1_val)
        textCheckoutSplit2Val = view.findViewById(R.id.text_checkout_split2_val)
        textCheckoutPromoVal = view.findViewById(R.id.text_checkout_promo_val)
        textCheckoutPackagingVal = view.findViewById(R.id.text_checkout_packaging_val)
        textCheckoutTotalPrice = view.findViewById(R.id.text_checkout_total_price)

        btnPlaceOrder = view.findViewById(R.id.btn_place_order)
    }

    private fun setupActions() {
        btnBack.setOnClickListener { findNavController().navigateUp() }

        // Delivery option switches
        btnDeliveryAllOnce.setOnClickListener { setSplitDeliveryOption(false) }
        btnDeliverySplit.setOnClickListener { setSplitDeliveryOption(true) }

        // Promo Code Click
        btnPromoCode.setOnClickListener {
            if (!isPromoApplied) {
                isPromoApplied = true
                textPromoCode.text = "Promo aktif: MX597TN (-10%)"
                textPromoCode.setTextColor(resources.getColor(R.color.color_primary_green))
                Toast.makeText(requireContext(), "Kode promo MX597TN berhasil digunakan!", Toast.LENGTH_SHORT).show()
            } else {
                isPromoApplied = false
                textPromoCode.text = "Kode Promo"
                textPromoCode.setTextColor(resources.getColor(R.color.color_text_dark))
            }
            recalculatePricing()
        }

        // Bonus points toggle
        switchBonuses.setOnCheckedChangeListener { _, isChecked ->
            isBonusApplied = isChecked
            recalculatePricing()
        }

        // Payment selections
        btnPayCash.setOnClickListener { selectPayment(0) }
        btnPayCard1.setOnClickListener { selectPayment(1) }
        btnPayCard2.setOnClickListener { selectPayment(2) }
        btnPayNew.setOnClickListener {
            Toast.makeText(requireContext(), "Simulasi penambahan kartu baru.", Toast.LENGTH_SHORT).show()
        }

        // Comments toggle
        btnToggleComment.setOnClickListener {
            isCommentExpanded = !isCommentExpanded
            layoutCheckoutComment.visibility = if (isCommentExpanded) View.VISIBLE else View.GONE
            imageCommentArrow.rotation = if (isCommentExpanded) 270f else 90f
        }

        // Pricing toggle
        btnTogglePricingDetail.setOnClickListener {
            isPricingExpanded = !isPricingExpanded
            layoutPricingDetailContent.visibility = if (isPricingExpanded) View.VISIBLE else View.GONE
            imagePricingArrow.rotation = if (isPricingExpanded) 270f else 90f
        }

        // Place Order final action
        btnPlaceOrder.setOnClickListener {
            sharedViewModel.clearCart()
            Toast.makeText(requireContext(), "Pesanan berhasil dikirim ke Mitra Tani!", Toast.LENGTH_LONG).show()
            findNavController().navigate(R.id.action_checkoutFragment_to_homeFragment)
        }
    }

    private fun setSplitDeliveryOption(split: Boolean) {
        isSplitDelivery = split
        if (split) {
            btnDeliverySplit.setBackgroundColor(resources.getColor(R.color.color_primary_green))
            btnDeliverySplit.setTextColor(resources.getColor(R.color.white))
            btnDeliveryAllOnce.setBackgroundColor(resources.getColor(R.color.transparent))
            btnDeliveryAllOnce.setTextColor(resources.getColor(R.color.color_text_muted))

            layoutDeliverySingle.visibility = View.GONE
            layoutDeliverySplit.visibility = View.VISIBLE
        } else {
            btnDeliveryAllOnce.setBackgroundColor(resources.getColor(R.color.color_primary_green))
            btnDeliveryAllOnce.setTextColor(resources.getColor(R.color.white))
            btnDeliverySplit.setBackgroundColor(resources.getColor(R.color.transparent))
            btnDeliverySplit.setTextColor(resources.getColor(R.color.color_text_muted))

            layoutDeliverySingle.visibility = View.VISIBLE
            layoutDeliverySplit.visibility = View.GONE
        }
        recalculatePricing()
    }

    private fun selectPayment(method: Int) {
        selectedPaymentMethod = method
        btnPayCash.setStrokeColor(resources.getColorStateList(if (method == 0) R.color.color_primary_green else R.color.color_border_grey))
        btnPayCash.setStrokeWidth(if (method == 0) 6 else 3)

        btnPayCard1.setStrokeColor(resources.getColorStateList(if (method == 1) R.color.color_primary_green else R.color.color_border_grey))
        btnPayCard1.setStrokeWidth(if (method == 1) 6 else 3)

        btnPayCard2.setStrokeColor(resources.getColorStateList(if (method == 2) R.color.color_primary_green else R.color.color_border_grey))
        btnPayCard2.setStrokeWidth(if (method == 2) 6 else 3)
    }

    private fun renderDeliveryData() {
        val cartMap = sharedViewModel.cartItems.value ?: emptyMap()
        if (cartMap.isEmpty()) return

        // 1. Populate Single Delivery Items
        layoutThumbnailsSingle.removeAllViews()
        cartMap.forEach { (product, qty) ->
            addThumbnailToContainer(layoutThumbnailsSingle, product, qty)
        }
        val totalCount = cartMap.values.sum()
        val totalWeight = totalCount * 0.5 // mock weight
        textItemsCountSingle.text = "$totalCount Barang ($totalWeight kg)"

        // 2. Populate Split Delivery Items
        layoutThumbnailsSplit1.removeAllViews()
        layoutThumbnailsSplit2.removeAllViews()

        val itemsList = cartMap.entries.toList()
        val splitIndex = (itemsList.size + 1) / 2

        val split1Items = itemsList.take(splitIndex)
        val split2Items = itemsList.drop(splitIndex)

        split1Items.forEach { entry ->
            addThumbnailToContainer(layoutThumbnailsSplit1, entry.key, entry.value)
        }
        split2Items.forEach { entry ->
            addThumbnailToContainer(layoutThumbnailsSplit2, entry.key, entry.value)
        }

        val count1 = split1Items.sumOf { it.value }
        val weight1 = count1 * 0.5
        textItemsCountSplit1.text = "$count1 Barang ($weight1 kg)"

        val count2 = split2Items.sumOf { it.value }
        val weight2 = count2 * 0.5
        textItemsCountSplit2.text = "$count2 Barang ($weight2 kg)"

        // Render Calendar Calendars
        renderCalendar(layoutCalendarSingle, selectedDaySingle) { dayIndex ->
            selectedDaySingle = dayIndex
            renderDeliveryData()
        }
        renderCalendar(layoutCalendarSplit1, selectedDaySplit1) { dayIndex ->
            selectedDaySplit1 = dayIndex
            renderDeliveryData()
        }
        renderCalendar(layoutCalendarSplit2, selectedDaySplit2) { dayIndex ->
            selectedDaySplit2 = dayIndex
            renderDeliveryData()
        }

        // Render Time slots
        renderTimeSlots(layoutTimeslotsSingle, selectedSlotSingle) { slotIndex ->
            selectedSlotSingle = slotIndex
            renderDeliveryData()
        }
        renderTimeSlots(layoutTimeslotsSplit1, selectedSlotSplit1) { slotIndex ->
            selectedSlotSplit1 = slotIndex
            renderDeliveryData()
        }
        renderTimeSlots(layoutTimeslotsSplit2, selectedSlotSplit2) { slotIndex ->
            selectedSlotSplit2 = slotIndex
            renderDeliveryData()
        }
    }

    private fun addThumbnailToContainer(container: LinearLayout, product: Product, qty: Int) {
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.item_checkout_product, container, false)
        val image = view.findViewById<ImageView>(R.id.image_product)
        val textQty = view.findViewById<TextView>(R.id.text_qty)

        image.setImageResource(product.imageResId)
        textQty.text = qty.toString()

        container.addView(view)
    }

    private fun renderCalendar(container: LinearLayout, selectedIndex: Int, onSelected: (Int) -> Unit) {
        container.removeAllViews()
        calendarDays.forEachIndexed { index, (day, name) ->
            val card = MaterialCardView(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    rightMargin = 10
                }
                radius = 24f
                cardElevation = 0f
                strokeWidth = 0
                setCardBackgroundColor(resources.getColor(if (index == selectedIndex) R.color.color_primary_green else R.color.color_surface_warm))
            }

            val inner = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(28, 20, 28, 20)
            }

            val textDay = TextView(requireContext()).apply {
                text = day
                setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 14f)
                typeface = android.graphics.Typeface.DEFAULT_BOLD
                setTextColor(resources.getColor(if (index == selectedIndex) R.color.white else R.color.color_text_dark))
            }

            val textName = TextView(requireContext()).apply {
                text = name
                setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 10f)
                setTextColor(resources.getColor(if (index == selectedIndex) R.color.color_olive_light else R.color.color_text_muted))
            }

            inner.addView(textDay)
            inner.addView(textName)
            card.addView(inner)

            card.setOnClickListener { onSelected(index) }
            container.addView(card)
        }
    }

    private fun renderTimeSlots(container: LinearLayout, selectedIndex: Int, onSelected: (Int) -> Unit) {
        container.removeAllViews()
        timeSlots.forEachIndexed { index, slot ->
            val card = MaterialCardView(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    rightMargin = 10
                }
                radius = 16f
                cardElevation = 0f
                strokeWidth = 0
                setCardBackgroundColor(resources.getColor(if (index == selectedIndex) R.color.color_primary_green else R.color.color_surface_warm))
            }

            val textSlot = TextView(requireContext()).apply {
                text = slot
                setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 11f)
                typeface = android.graphics.Typeface.DEFAULT_BOLD
                setPadding(32, 20, 32, 20)
                setTextColor(resources.getColor(if (index == selectedIndex) R.color.white else R.color.color_text_dark))
            }

            card.addView(textSlot)
            card.setOnClickListener { onSelected(index) }
            container.addView(card)
        }
    }

    private fun recalculatePricing() {
        val cartMap = sharedViewModel.cartItems.value ?: emptyMap()
        if (cartMap.isEmpty()) return

        val itemsList = cartMap.entries.toList()
        val splitIndex = (itemsList.size + 1) / 2
        val split1 = itemsList.take(splitIndex)
        val split2 = itemsList.drop(splitIndex)

        val price1 = split1.sumOf { it.key.price * it.value }
        val price2 = split2.sumOf { it.key.price * it.value }
        val subtotal = price1 + price2

        textCheckoutSplit1Val.text = "Rp $price1"
        if (isSplitDelivery && price2 > 0) {
            rowPricingSplit2.visibility = View.VISIBLE
            textCheckoutSplit2Val.text = "Rp $price2"
        } else {
            rowPricingSplit2.visibility = View.GONE
        }

        textCheckoutPackagingVal.text = "Rp $packagingCost"

        // Discount calculations
        var discount = 0
        if (isPromoApplied) {
            discount += (subtotal * 0.1).toInt()
            rowCheckoutPromo.visibility = View.VISIBLE
            textCheckoutPromoVal.text = "-Rp $discount"
        } else {
            rowCheckoutPromo.visibility = View.GONE
        }

        if (isBonusApplied) {
            discount += 5000
            rowCheckoutBonus.visibility = View.VISIBLE
        } else {
            rowCheckoutBonus.visibility = View.GONE
        }

        val total = subtotal + packagingCost - discount
        textCheckoutTotalPrice.text = "Rp $total"
    }

    // Helper syntax conversion
    private val Int.sp: Float
        get() = this.toFloat()
}
