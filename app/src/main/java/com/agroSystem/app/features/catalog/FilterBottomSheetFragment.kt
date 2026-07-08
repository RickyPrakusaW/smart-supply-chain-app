package com.agroSystem.app.features.catalog

import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.agroSystem.app.R
import com.agroSystem.app.features.shared.MainSharedViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial

class FilterBottomSheetFragment : BottomSheetDialogFragment() {

    private val sharedViewModel: MainSharedViewModel by activityViewModels()

    private lateinit var btnBack: View
    private lateinit var btnReset: TextView
    private lateinit var layoutActiveParameters: View
    private lateinit var layoutActiveChips: LinearLayout

    private lateinit var chipDeliveryToday: TextView
    private lateinit var chipDeliveryTomorrow: TextView
    private lateinit var chipDelivery3Days: TextView
    private lateinit var chipDelivery5Days: TextView

    private lateinit var chipRegionMalang: TextView
    private lateinit var chipRegionClosest: TextView

    private lateinit var switchEcoFriendly: SwitchMaterial
    private lateinit var switchDiscountOnly: SwitchMaterial

    private lateinit var chipDietVeg: TextView
    private lateinit var chipDietVegan: TextView
    private lateinit var chipDietKeto: TextView

    private lateinit var chipAllergenLactose: TextView
    private lateinit var chipAllergenGluten: TextView

    private lateinit var chipNutrCalcium: TextView
    private lateinit var chipNutrProtein: TextView
    private lateinit var chipNutrFiber: TextView

    private lateinit var btnApplyFilters: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_filter_sheet, container, false)

        bindViews(view)
        setupActions()
        observeStates()

        return view
    }

    private fun bindViews(view: View) {
        btnBack = view.findViewById(R.id.btn_back)
        btnReset = view.findViewById(R.id.btn_reset)
        layoutActiveParameters = view.findViewById(R.id.layout_active_parameters)
        layoutActiveChips = view.findViewById(R.id.layout_active_chips)

        chipDeliveryToday = view.findViewById(R.id.chip_delivery_today)
        chipDeliveryTomorrow = view.findViewById(R.id.chip_delivery_tomorrow)
        chipDelivery3Days = view.findViewById(R.id.chip_delivery_3days)
        chipDelivery5Days = view.findViewById(R.id.chip_delivery_5days)

        chipRegionMalang = view.findViewById(R.id.chip_region_malang)
        chipRegionClosest = view.findViewById(R.id.chip_region_closest)

        switchEcoFriendly = view.findViewById(R.id.switch_eco_friendly)
        switchDiscountOnly = view.findViewById(R.id.switch_discount_only)

        chipDietVeg = view.findViewById(R.id.chip_diet_veg)
        chipDietVegan = view.findViewById(R.id.chip_diet_vegan)
        chipDietKeto = view.findViewById(R.id.chip_diet_keto)

        chipAllergenLactose = view.findViewById(R.id.chip_allergen_lactose)
        chipAllergenGluten = view.findViewById(R.id.chip_allergen_gluten)

        chipNutrCalcium = view.findViewById(R.id.chip_nutr_calcium)
        chipNutrProtein = view.findViewById(R.id.chip_nutr_protein)
        chipNutrFiber = view.findViewById(R.id.chip_nutr_fiber)

        btnApplyFilters = view.findViewById(R.id.btn_apply_filters)
    }

    private fun setupActions() {
        btnBack.setOnClickListener { dismiss() }
        btnReset.setOnClickListener { sharedViewModel.resetFilters() }

        // Delivery Days
        chipDeliveryToday.setOnClickListener { toggleDelivery(1) }
        chipDeliveryTomorrow.setOnClickListener { toggleDelivery(2) }
        chipDelivery3Days.setOnClickListener { toggleDelivery(3) }
        chipDelivery5Days.setOnClickListener { toggleDelivery(5) }

        // Regions
        chipRegionMalang.setOnClickListener { toggleRegion("Malang Raya") }
        chipRegionClosest.setOnClickListener { toggleRegion("Terdekat") }

        // Switches
        switchEcoFriendly.setOnCheckedChangeListener { _, isChecked ->
            sharedViewModel.filterEcoFriendly.value = isChecked
        }
        switchDiscountOnly.setOnCheckedChangeListener { _, isChecked ->
            sharedViewModel.filterDiscountedOnly.value = isChecked
        }

        // Diets
        chipDietVeg.setOnClickListener { toggleDiet("Vegetarian") }
        chipDietVegan.setOnClickListener { toggleDiet("Vegan") }
        chipDietKeto.setOnClickListener { toggleDiet("Keto") }

        // Allergens
        chipAllergenLactose.setOnClickListener { toggleAllergen("Bebas Laktosa") }
        chipAllergenGluten.setOnClickListener { toggleAllergen("Bebas Gluten") }

        // Nutrients
        chipNutrCalcium.setOnClickListener { toggleNutrient("Tinggi Kalsium") }
        chipNutrProtein.setOnClickListener { toggleNutrient("Tinggi Protein") }
        chipNutrFiber.setOnClickListener { toggleNutrient("Kaya Serat") }

        btnApplyFilters.setOnClickListener {
            dismiss()
        }
    }

    private fun observeStates() {
        // Observe and update delivery chips
        sharedViewModel.filterDeliveryDays.observe(viewLifecycleOwner) { days ->
            updateChipSelection(chipDeliveryToday, days == 1)
            updateChipSelection(chipDeliveryTomorrow, days == 2)
            updateChipSelection(chipDelivery3Days, days == 3)
            updateChipSelection(chipDelivery5Days, days == 5)
            updateButtonCount()
            updateActiveParameters()
        }

        // Observe and update region chips
        sharedViewModel.filterRegion.observe(viewLifecycleOwner) { region ->
            updateChipSelection(chipRegionMalang, region == "Malang Raya")
            updateChipSelection(chipRegionClosest, region == "Terdekat")
            updateButtonCount()
            updateActiveParameters()
        }

        // Observe switches
        sharedViewModel.filterEcoFriendly.observe(viewLifecycleOwner) { active ->
            if (switchEcoFriendly.isChecked != active) switchEcoFriendly.isChecked = active
            updateButtonCount()
            updateActiveParameters()
        }
        sharedViewModel.filterDiscountedOnly.observe(viewLifecycleOwner) { active ->
            if (switchDiscountOnly.isChecked != active) switchDiscountOnly.isChecked = active
            updateButtonCount()
            updateActiveParameters()
        }

        // Observe diets
        sharedViewModel.filterSelectedDiets.observe(viewLifecycleOwner) { list ->
            updateChipSelection(chipDietVeg, list.contains("Vegetarian"))
            updateChipSelection(chipDietVegan, list.contains("Vegan"))
            updateChipSelection(chipDietKeto, list.contains("Keto"))
            updateButtonCount()
            updateActiveParameters()
        }

        // Observe allergens
        sharedViewModel.filterSelectedAllergens.observe(viewLifecycleOwner) { list ->
            updateChipSelection(chipAllergenLactose, list.contains("Bebas Laktosa"))
            updateChipSelection(chipAllergenGluten, list.contains("Bebas Gluten"))
            updateButtonCount()
            updateActiveParameters()
        }

        // Observe nutrients
        sharedViewModel.filterSelectedNutrients.observe(viewLifecycleOwner) { list ->
            updateChipSelection(chipNutrCalcium, list.contains("Tinggi Kalsium"))
            updateChipSelection(chipNutrProtein, list.contains("Tinggi Protein"))
            updateChipSelection(chipNutrFiber, list.contains("Kaya Serat"))
            updateButtonCount()
            updateActiveParameters()
        }
    }

    private fun updateActiveParameters() {
        val days = sharedViewModel.filterDeliveryDays.value ?: 0
        val reg = sharedViewModel.filterRegion.value ?: "Semua"
        val eco = sharedViewModel.filterEcoFriendly.value ?: false
        val disc = sharedViewModel.filterDiscountedOnly.value ?: false
        val diets = sharedViewModel.filterSelectedDiets.value ?: emptyList()
        val allergens = sharedViewModel.filterSelectedAllergens.value ?: emptyList()
        val nutrients = sharedViewModel.filterSelectedNutrients.value ?: emptyList()

        val activeList = mutableListOf<Pair<String, () -> Unit>>()

        if (days > 0) {
            val label = when (days) {
                1 -> "Hari Ini"
                2 -> "Besok"
                else -> "s.d $days Hari"
            }
            activeList.add(Pair(label) { sharedViewModel.filterDeliveryDays.value = 0 })
        }
        if (reg != "Semua") {
            activeList.add(Pair(reg) { sharedViewModel.filterRegion.value = "Semua" })
        }
        if (eco) {
            activeList.add(Pair("Eco-friendly") { sharedViewModel.filterEcoFriendly.value = false })
        }
        if (disc) {
            activeList.add(Pair("Diskon") { sharedViewModel.filterDiscountedOnly.value = false })
        }
        diets.forEach { item ->
            activeList.add(Pair(item) {
                val current = sharedViewModel.filterSelectedDiets.value ?: mutableListOf()
                current.remove(item)
                sharedViewModel.filterSelectedDiets.value = current
            })
        }
        allergens.forEach { item ->
            activeList.add(Pair(item) {
                val current = sharedViewModel.filterSelectedAllergens.value ?: mutableListOf()
                current.remove(item)
                sharedViewModel.filterSelectedAllergens.value = current
            })
        }
        nutrients.forEach { item ->
            activeList.add(Pair(item) {
                val current = sharedViewModel.filterSelectedNutrients.value ?: mutableListOf()
                current.remove(item)
                sharedViewModel.filterSelectedNutrients.value = current
            })
        }

        layoutActiveChips.removeAllViews()

        if (activeList.isEmpty()) {
            layoutActiveParameters.visibility = View.GONE
        } else {
            layoutActiveParameters.visibility = View.VISIBLE
            activeList.forEach { (label, onDismiss) ->
                val chipView = LayoutInflater.from(requireContext()).inflate(R.layout.item_parameter_chip, layoutActiveChips, false)
                val text: TextView = chipView.findViewById(R.id.text_chip_label)
                val dismiss: View = chipView.findViewById(R.id.btn_dismiss_chip)

                text.text = label
                dismiss.setOnClickListener { onDismiss() }

                layoutActiveChips.addView(chipView)
            }
        }
    }

    private fun toggleDelivery(days: Int) {
        val current = sharedViewModel.filterDeliveryDays.value ?: 0
        sharedViewModel.filterDeliveryDays.value = if (current == days) 0 else days
    }

    private fun toggleRegion(regionName: String) {
        val current = sharedViewModel.filterRegion.value ?: "Semua"
        sharedViewModel.filterRegion.value = if (current == regionName) "Semua" else regionName
    }

    private fun toggleDiet(dietName: String) {
        val current = sharedViewModel.filterSelectedDiets.value ?: mutableListOf()
        if (current.contains(dietName)) current.remove(dietName) else current.add(dietName)
        sharedViewModel.filterSelectedDiets.value = current
    }

    private fun toggleAllergen(allergenName: String) {
        val current = sharedViewModel.filterSelectedAllergens.value ?: mutableListOf()
        if (current.contains(allergenName)) current.remove(allergenName) else current.add(allergenName)
        sharedViewModel.filterSelectedAllergens.value = current
    }

    private fun toggleNutrient(nutrientName: String) {
        val current = sharedViewModel.filterSelectedNutrients.value ?: mutableListOf()
        if (current.contains(nutrientName)) current.remove(nutrientName) else current.add(nutrientName)
        sharedViewModel.filterSelectedNutrients.value = current
    }

    private fun updateChipSelection(textView: TextView, isSelected: Boolean) {
        val context = textView.context
        if (isSelected) {
            textView.setBackgroundResource(R.drawable.bg_tag_primary)
            textView.setTextColor(ContextCompat.getColor(context, R.color.white))
        } else {
            textView.setBackgroundResource(R.drawable.bg_tag_olive)
            textView.setTextColor(ContextCompat.getColor(context, R.color.color_text_dark))
        }
    }

    private fun updateButtonCount() {
        val count = sharedViewModel.getFilteredProducts().size
        btnApplyFilters.text = "Tampilkan $count Produk"
    }

    private fun Int.dpToPx(context: Context): Int {
        return (this * context.resources.displayMetrics.density + 0.5f).toInt()
    }
}
