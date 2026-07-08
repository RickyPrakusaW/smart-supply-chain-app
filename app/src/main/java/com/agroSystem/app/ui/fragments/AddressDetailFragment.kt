package com.agroSystem.app.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.agroSystem.app.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class AddressDetailFragment : Fragment() {

    private lateinit var inputDetail: EditText
    private lateinit var inputStreet: EditText
    private lateinit var inputLandmark: EditText
    private lateinit var btnSave: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_address_detail, container, false)

        inputDetail = view.findViewById(R.id.input_detail)
        inputStreet = view.findViewById(R.id.input_street)
        inputLandmark = view.findViewById(R.id.input_landmark)
        btnSave = view.findViewById(R.id.btn_save)

        // Back action
        view.findViewById<MaterialCardView>(R.id.btn_back).setOnClickListener {
            findNavController().navigate(R.id.action_addressDetailFragment_to_selectAddressFragment)
        }

        // Validate required fields: detail, street, landmark
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val detail = inputDetail.text.toString().trim()
                val street = inputStreet.text.toString().trim()
                val landmark = inputLandmark.text.toString().trim()
                btnSave.isEnabled = detail.isNotEmpty() && street.isNotEmpty() && landmark.isNotEmpty()
            }
        }

        inputDetail.addTextChangedListener(watcher)
        inputStreet.addTextChangedListener(watcher)
        inputLandmark.addTextChangedListener(watcher)

        // Save & enter action
        btnSave.setOnClickListener {
            findNavController().navigate(R.id.action_addressDetailFragment_to_homeFragment)
        }

        return view
    }
}
