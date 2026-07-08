package com.agroSystem.app.features.auth

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

class ProfileSetupFragment : Fragment() {

    private lateinit var inputName: EditText
    private lateinit var btnContinue: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile_setup, container, false)

        inputName = view.findViewById(R.id.input_name)
        btnContinue = view.findViewById(R.id.btn_continue)

        // Back action
        view.findViewById<MaterialCardView>(R.id.btn_back).setOnClickListener {
            findNavController().navigate(R.id.action_profileSetupFragment_to_otpInputFragment)
        }

        // Validate name entry to enable continue button
        inputName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                btnContinue.isEnabled = !s.isNullOrBlank()
            }
        })

        // Continue action
        btnContinue.setOnClickListener {
            findNavController().navigate(R.id.action_profileSetupFragment_to_locationPermissionFragment)
        }

        return view
    }
}
