package com.agroSystem.app.features.address

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.agroSystem.app.R
import com.agroSystem.app.features.auth.AuthViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputLayout

class AddressDetailFragment : Fragment() {

    private val authViewModel: AuthViewModel by activityViewModels()
    
    private lateinit var layoutDetail: TextInputLayout
    private lateinit var layoutStreet: TextInputLayout
    private lateinit var layoutLandmark: TextInputLayout
    private lateinit var layoutPostalCode: TextInputLayout

    private lateinit var inputDetail: EditText
    private lateinit var inputStreet: EditText
    private lateinit var inputLandmark: EditText
    private lateinit var inputPostalCode: EditText
    private lateinit var btnSave: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_address_detail, container, false)

        layoutDetail = view.findViewById(R.id.layout_detail)
        layoutStreet = view.findViewById(R.id.layout_street)
        layoutLandmark = view.findViewById(R.id.layout_landmark)
        layoutPostalCode = view.findViewById(R.id.layout_postal_code)

        inputDetail = view.findViewById(R.id.input_detail)
        inputStreet = view.findViewById(R.id.input_street)
        inputLandmark = view.findViewById(R.id.input_landmark)
        inputPostalCode = view.findViewById(R.id.input_postal_code)
        btnSave = view.findViewById(R.id.btn_save)

        // Make button always enabled to support submit validation warnings
        btnSave.isEnabled = true

        // Pre-populate fields from maps arguments if passed
        val argStreet = arguments?.getString("street") ?: ""
        val argDetail = arguments?.getString("detail") ?: ""
        if (argStreet.isNotEmpty()) {
            inputStreet.setText(argStreet)
        }
        if (argDetail.isNotEmpty()) {
            inputDetail.setText(argDetail)
        }

        // Back action
        view.findViewById<MaterialCardView>(R.id.btn_back).setOnClickListener {
            findNavController().navigateUp()
        }

        // Clear error text on text changes
        inputDetail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                layoutDetail.error = null
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        inputStreet.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                layoutStreet.error = null
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        inputPostalCode.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                layoutPostalCode.error = null
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        inputLandmark.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                layoutLandmark.error = null
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Save & enter action with strict validation
        btnSave.setOnClickListener {
            val detail = inputDetail.text.toString().trim()
            val street = inputStreet.text.toString().trim()
            val landmark = inputLandmark.text.toString().trim()
            val postalCode = inputPostalCode.text.toString().trim()

            // Reset errors
            layoutDetail.error = null
            layoutStreet.error = null
            layoutLandmark.error = null
            layoutPostalCode.error = null

            var hasError = false

            // Strict Validation
            if (detail.length < 5) {
                layoutDetail.error = "Detail alamat harus diisi (minimal 5 karakter)!"
                layoutDetail.requestFocus()
                hasError = true
            }

            if (street.length < 5) {
                layoutStreet.error = "Nama jalan utama harus diisi (minimal 5 karakter)!"
                layoutStreet.requestFocus()
                hasError = true
            }

            if (postalCode.length != 5 || !postalCode.all { it.isDigit() }) {
                layoutPostalCode.error = "Kode pos harus berupa 5 digit angka!"
                layoutPostalCode.requestFocus()
                hasError = true
            }

            if (landmark.length < 3) {
                layoutLandmark.error = "Patokan alamat harus diisi (minimal 3 karakter)!"
                layoutLandmark.requestFocus()
                hasError = true
            }

            if (hasError) return@setOnClickListener

            // Composite address format: "Street, Detail (Patokan: Landmark, Kode Pos: PostalCode)"
            val fullAddress = "$street, $detail (Patokan: $landmark, Kode Pos: $postalCode)"

            val currentUser = authViewModel.currentUser.value
            if (currentUser != null) {
                authViewModel.updateProfile(
                    name = currentUser.name,
                    email = currentUser.email,
                    phone = currentUser.phone,
                    address = fullAddress,
                    photoUrl = currentUser.photoUrl,
                    role = currentUser.role
                ) {
                    findNavController().popBackStack(R.id.homeFragment, false)
                }
            } else {
                findNavController().popBackStack(R.id.homeFragment, false)
            }
        }

        return view
    }
}
