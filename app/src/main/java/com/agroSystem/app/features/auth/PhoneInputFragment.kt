package com.agroSystem.app.features.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.agroSystem.app.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class PhoneInputFragment : Fragment() {

    private var phoneNumber: String = ""
    private lateinit var textPhoneNumber: TextView
    private lateinit var textPhonePlaceholder: TextView
    private lateinit var btnContinue: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_phone_input, container, false)

        textPhoneNumber = view.findViewById(R.id.text_phone_number)
        textPhonePlaceholder = view.findViewById(R.id.text_phone_placeholder)
        btnContinue = view.findViewById(R.id.btn_continue)

        // Back action
        view.findViewById<MaterialCardView>(R.id.btn_back).setOnClickListener {
            findNavController().navigate(R.id.action_phoneInputFragment_to_onboardingFragment)
        }

        // Guest mode action
        view.findViewById<MaterialButton>(R.id.btn_guest).setOnClickListener {
            findNavController().navigate(R.id.action_phoneInputFragment_to_homeFragment)
        }

        // Register custom keyboard click actions
        val keys = listOf(
            R.id.key_0 to "0", R.id.key_1 to "1", R.id.key_2 to "2",
            R.id.key_3 to "3", R.id.key_4 to "4", R.id.key_5 to "5",
            R.id.key_6 to "6", R.id.key_7 to "7", R.id.key_8 to "8",
            R.id.key_9 to "9"
        )

        for ((id, char) in keys) {
            view.findViewById<MaterialButton>(id).setOnClickListener {
                if (phoneNumber.length < 12) {
                    phoneNumber += char
                    updatePhoneNumberDisplay()
                }
            }
        }

        view.findViewById<MaterialButton>(R.id.key_del).setOnClickListener {
            if (phoneNumber.isNotEmpty()) {
                phoneNumber = phoneNumber.dropLast(1)
                updatePhoneNumberDisplay()
            }
        }

        // Continue action
        btnContinue.setOnClickListener {
            val bundle = Bundle().apply {
                putString("phone", phoneNumber)
            }
            findNavController().navigate(R.id.action_phoneInputFragment_to_otpInputFragment, bundle)
        }

        return view
    }

    private fun updatePhoneNumberDisplay() {
        if (phoneNumber.isEmpty()) {
            textPhoneNumber.text = ""
            textPhonePlaceholder.visibility = View.VISIBLE
            btnContinue.isEnabled = false
        } else {
            textPhoneNumber.text = formatPhone(phoneNumber)
            textPhonePlaceholder.visibility = View.GONE
            btnContinue.isEnabled = phoneNumber.length >= 9
        }
    }

    private fun formatPhone(raw: String): String {
        val sb = StringBuilder()
        for (i in raw.indices) {
            sb.append(raw[i])
            if (i == 2 || i == 6) {
                sb.append("-")
            }
        }
        return sb.toString()
    }
}
