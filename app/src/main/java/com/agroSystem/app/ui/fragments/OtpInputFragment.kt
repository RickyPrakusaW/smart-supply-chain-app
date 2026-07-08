package com.agroSystem.app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.agroSystem.app.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class OtpInputFragment : Fragment() {

    private var otpCode: String = ""
    private var isError: Boolean = false
    private lateinit var boxViews: List<TextView>
    private lateinit var textError: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_otp_input, container, false)

        val phone = arguments?.getString("phone") ?: "812-3456-7890"
        view.findViewById<TextView>(R.id.text_sub).text = "Dikirim ke nomor +62 ${formatPhone(phone)}"

        textError = view.findViewById(R.id.text_error)

        boxViews = listOf(
            view.findViewById(R.id.box_1),
            view.findViewById(R.id.box_2),
            view.findViewById(R.id.box_3),
            view.findViewById(R.id.box_4),
            view.findViewById(R.id.box_5),
            view.findViewById(R.id.box_6)
        )

        // Back Action
        view.findViewById<MaterialCardView>(R.id.btn_back).setOnClickListener {
            findNavController().navigate(R.id.action_otpInputFragment_to_phoneInputFragment)
        }

        // Register custom keyboard actions
        val keys = listOf(
            R.id.key_0 to "0", R.id.key_1 to "1", R.id.key_2 to "2",
            R.id.key_3 to "3", R.id.key_4 to "4", R.id.key_5 to "5",
            R.id.key_6 to "6", R.id.key_7 to "7", R.id.key_8 to "8",
            R.id.key_9 to "9"
        )

        for ((id, char) in keys) {
            view.findViewById<MaterialButton>(id).setOnClickListener {
                if (otpCode.length < 6) {
                    otpCode += char
                    isError = false
                    updateOtpDisplay()
                    if (otpCode.length == 6) {
                        validateOtp()
                    }
                }
            }
        }

        view.findViewById<MaterialButton>(R.id.key_del).setOnClickListener {
            if (otpCode.isNotEmpty()) {
                otpCode = otpCode.dropLast(1)
                isError = false
                updateOtpDisplay()
            }
        }

        updateOtpDisplay()
        return view
    }

    private fun updateOtpDisplay() {
        textError.visibility = if (isError) View.VISIBLE else View.GONE

        for (i in boxViews.indices) {
            val textView = boxViews[i]
            val digit = if (i < otpCode.length) otpCode[i].toString() else ""
            textView.text = digit

            val bgDrawableRes = when {
                isError -> R.drawable.bg_otp_box_error
                i == otpCode.length -> R.drawable.bg_otp_box_active
                else -> R.drawable.bg_otp_box
            }
            textView.setBackgroundResource(bgDrawableRes)

            val textColor = ContextCompat.getColor(
                requireContext(),
                if (isError) R.color.color_red_error else R.color.color_text_dark
            )
            textView.setTextColor(textColor)
        }
    }

    private fun validateOtp() {
        if (otpCode == "123456") {
            findNavController().navigate(R.id.action_otpInputFragment_to_profileSetupFragment)
        } else {
            isError = true
            updateOtpDisplay()
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
