package com.agroSystem.app.features.seller

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.agroSystem.app.R
import com.agroSystem.app.data.models.Product
import com.agroSystem.app.data.models.bindImageTo
import com.agroSystem.app.features.auth.AuthViewModel
import com.agroSystem.app.features.shared.MainSharedViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputLayout
import java.io.InputStream

class AddEditProductFragment : Fragment() {

    private val authViewModel: AuthViewModel by activityViewModels()
    private val sharedViewModel: MainSharedViewModel by activityViewModels()

    private var productId: Int = -1
    private var editingProduct: Product? = null
    private var pickedBase64Image: String? = null

    private lateinit var btnBack: MaterialCardView
    private lateinit var textHeaderTitle: TextView
    
    private lateinit var cardProductImage: MaterialCardView
    private lateinit var imageProductPreview: ImageView
    
    private lateinit var layoutName: TextInputLayout
    private lateinit var inputName: EditText
    
    private lateinit var layoutCategory: TextInputLayout
    private lateinit var inputCategory: AutoCompleteTextView
    
    private lateinit var layoutPrice: TextInputLayout
    private lateinit var inputPrice: EditText
    
    private lateinit var layoutUnit: TextInputLayout
    private lateinit var inputUnit: EditText
    
    private lateinit var layoutDelivery: TextInputLayout
    private lateinit var inputDelivery: EditText
    
    private lateinit var layoutIngredients: TextInputLayout
    private lateinit var inputIngredients: EditText
    
    private lateinit var switchEcoFriendly: SwitchMaterial
    private lateinit var btnSaveProduct: MaterialButton

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val base64Str = uriToBase64(it)
            if (base64Str != null) {
                pickedBase64Image = base64Str
                val decodedString = Base64.decode(base64Str, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                imageProductPreview.setImageBitmap(bitmap)
            } else {
                Toast.makeText(requireContext(), "Gagal memproses gambar.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        productId = arguments?.getInt("productId", -1) ?: -1
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_edit_product, container, false)

        btnBack = view.findViewById(R.id.btn_back)
        textHeaderTitle = view.findViewById(R.id.text_header_title)
        cardProductImage = view.findViewById(R.id.card_product_image)
        imageProductPreview = view.findViewById(R.id.image_product_preview)

        layoutName = view.findViewById(R.id.layout_product_name)
        inputName = view.findViewById(R.id.input_product_name)
        
        layoutCategory = view.findViewById(R.id.layout_product_category)
        inputCategory = view.findViewById(R.id.input_product_category)
        
        layoutPrice = view.findViewById(R.id.layout_product_price)
        inputPrice = view.findViewById(R.id.input_product_price)
        
        layoutUnit = view.findViewById(R.id.layout_product_unit)
        inputUnit = view.findViewById(R.id.input_product_unit)
        
        layoutDelivery = view.findViewById(R.id.layout_product_delivery)
        inputDelivery = view.findViewById(R.id.input_product_delivery)
        
        layoutIngredients = view.findViewById(R.id.layout_product_ingredients)
        inputIngredients = view.findViewById(R.id.input_product_ingredients)
        
        switchEcoFriendly = view.findViewById(R.id.switch_eco_friendly)
        btnSaveProduct = view.findViewById(R.id.btn_save_product)

        setupCategoryDropdown()
        setupValidationWatchers()

        if (productId != -1) {
            textHeaderTitle.text = "Ubah Produk Jualan"
            loadProductData()
        } else {
            textHeaderTitle.text = "Tambah Produk Baru"
        }

        btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        cardProductImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        btnSaveProduct.setOnClickListener {
            saveProduct()
        }

        return view
    }

    private fun setupCategoryDropdown() {
        val items = listOf("Telur", "Susu", "Sayuran", "Daging", "Bahan Sup")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, items)
        inputCategory.setAdapter(adapter)
    }

    private fun loadProductData() {
        val prodList = sharedViewModel.productsList.value ?: sharedViewModel.allProducts
        editingProduct = prodList.firstOrNull { it.id == productId }
        
        val product = editingProduct
        if (product == null) {
            Toast.makeText(requireContext(), "Produk tidak ditemukan.", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
            return
        }

        inputName.setText(product.name)
        inputCategory.setText(product.category, false)
        inputPrice.setText(product.price.toString())
        inputUnit.setText(product.unit)
        inputDelivery.setText(product.deliveryDays.toString())
        inputIngredients.setText(product.ingredients)
        switchEcoFriendly.isChecked = product.isEcoFriendly
        pickedBase64Image = product.imageBytes

        product.bindImageTo(imageProductPreview)
    }

    private fun setupValidationWatchers() {
        val clearErrorWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                layoutName.error = null
                layoutCategory.error = null
                layoutPrice.error = null
                layoutUnit.error = null
                layoutDelivery.error = null
                layoutIngredients.error = null
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        }
        
        inputName.addTextChangedListener(clearErrorWatcher)
        inputCategory.addTextChangedListener(clearErrorWatcher)
        inputPrice.addTextChangedListener(clearErrorWatcher)
        inputUnit.addTextChangedListener(clearErrorWatcher)
        inputDelivery.addTextChangedListener(clearErrorWatcher)
        inputIngredients.addTextChangedListener(clearErrorWatcher)
    }

    private fun saveProduct() {
        val name = inputName.text.toString().trim()
        val category = inputCategory.text.toString().trim()
        val priceStr = inputPrice.text.toString().trim()
        val unit = inputUnit.text.toString().trim()
        val deliveryStr = inputDelivery.text.toString().trim()
        val ingredients = inputIngredients.text.toString().trim()

        var isValid = true

        if (name.isEmpty()) {
            layoutName.error = "Nama produk jualan tidak boleh kosong"
            isValid = false
        }
        if (category.isEmpty()) {
            layoutCategory.error = "Silakan pilih kategori produk"
            isValid = false
        }
        if (priceStr.isEmpty()) {
            layoutPrice.error = "Harga tidak boleh kosong"
            isValid = false
        }
        if (unit.isEmpty()) {
            layoutUnit.error = "Kemasan/satuan tidak boleh kosong"
            isValid = false
        }
        if (deliveryStr.isEmpty()) {
            layoutDelivery.error = "Estimasi pengiriman tidak boleh kosong"
            isValid = false
        }
        if (ingredients.isEmpty()) {
            layoutIngredients.error = "Kandungan/deskripsi tidak boleh kosong"
            isValid = false
        }

        if (!isValid) return

        val price = priceStr.toIntOrNull() ?: 0
        val deliveryDays = deliveryStr.toIntOrNull() ?: 1
        val currentUser = authViewModel.currentUser.value ?: return

        btnSaveProduct.isEnabled = false

        val composedProduct = Product(
            id = if (productId == -1) 0 else productId,
            name = name,
            farmer = currentUser.name,
            rating = editingProduct?.rating ?: "5.0",
            price = price,
            unit = unit,
            imageResId = editingProduct?.imageResId ?: R.drawable.padi,
            category = category,
            isEcoFriendly = switchEcoFriendly.isChecked,
            deliveryDays = deliveryDays,
            ingredients = ingredients,
            ownerId = currentUser.id,
            imageBytes = pickedBase64Image
        )

        if (productId == -1) {
            // Creation
            sharedViewModel.createProduct(composedProduct) { success ->
                btnSaveProduct.isEnabled = true
                if (success) {
                    Toast.makeText(requireContext(), "Produk jualan berhasil ditambahkan!", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                } else {
                    Toast.makeText(requireContext(), "Gagal menambahkan produk.", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            // Update
            sharedViewModel.updateProduct(composedProduct) { success ->
                btnSaveProduct.isEnabled = true
                if (success) {
                    Toast.makeText(requireContext(), "Produk jualan berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                } else {
                    Toast.makeText(requireContext(), "Gagal memperbarui produk.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun uriToBase64(uri: Uri): String? {
        return try {
            val inputStream: InputStream? = requireContext().contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes()
            inputStream?.close()
            if (bytes != null) {
                Base64.encodeToString(bytes, Base64.DEFAULT)
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
