package com.agroSystem.app.features.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.agroSystem.app.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class OtpInputFragment : Fragment() {

    private val authViewModel: AuthViewModel by activityViewModels()
    private var otpCode: String = ""
    private var isError: Boolean = false
    private lateinit var boxViews: List<TextView>
    private lateinit var textError: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_otp_input, container, false)

        val mode = arguments?.getString("mode") ?: "phone"
        val phone = arguments?.getString("phone") ?: "812-3456-7890"
        val email = arguments?.getString("email") ?: ""

        val textSub = view.findViewById<TextView>(R.id.text_sub)
        if (mode == "email_register") {
            textSub.text = "Dikirim ke email $email"
        } else {
            textSub.text = "Dikirim ke nomor +62 ${formatPhone(phone)}"
        }

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
        val mode = arguments?.getString("mode") ?: "phone"
        
        if (mode == "email_register") {
            val email = arguments?.getString("email") ?: ""
            val password = arguments?.getString("password") ?: ""
            val name = arguments?.getString("name") ?: ""
            val sentOtp = arguments?.getString("sentOtp") ?: ""

            // Check if OTP matches (or quick bypass "123456" for ease of demo/testing)
            if (otpCode == sentOtp || otpCode == "123456") {
                Toast.makeText(requireContext(), "OTP Berhasil Terverifikasi! Mendaftarkan...", Toast.LENGTH_SHORT).show()
                val firebaseAuth = com.google.firebase.auth.FirebaseAuth.getInstance()
                firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val firebaseUser = task.result?.user
                            
                            // Set display name in Firebase Auth
                            val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                                .setDisplayName(name)
                                .build()
                            
                            firebaseUser?.updateProfile(profileUpdates)?.addOnCompleteListener {
                                // Sync session to local database & Firestore
                                authViewModel.loginWithGoogle("email_register", name, email) {
                                    Toast.makeText(requireContext(), "Pendaftaran berhasil! Selamat datang $name", Toast.LENGTH_LONG).show()
                                    findNavController().navigate(R.id.action_otpInputFragment_to_profileSetupFragment)
                                }
                            }
                        } else {
                            isError = true
                            updateOtpDisplay()
                            Toast.makeText(requireContext(), "Pendaftaran gagal: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
            } else {
                isError = true
                updateOtpDisplay()
                Toast.makeText(requireContext(), "Kode OTP salah!", Toast.LENGTH_SHORT).show()
            }
            return
        }

        // Phone number verification logic
        val verificationId = arguments?.getString("verificationId")
        val phoneNum = arguments?.getString("phone") ?: ""

        if (verificationId.isNullOrEmpty()) {
            // Mode Demo / Simulasi jika verificationId tidak tersedia
            if (otpCode == "123456") {
                authViewModel.setPhone(phoneNum)
                authViewModel.loginWithPhone {
                    findNavController().navigate(R.id.action_otpInputFragment_to_profileSetupFragment)
                }
            } else {
                isError = true
                updateOtpDisplay()
            }
            return
        }

        // Mode Riil: Verifikasi OTP menggunakan Firebase Authentication
        val credential = com.google.firebase.auth.PhoneAuthProvider.getCredential(verificationId, otpCode)
        com.google.firebase.auth.FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = task.result?.user
                    val formattedPhone = firebaseUser?.phoneNumber ?: phoneNum
                    
                    authViewModel.setPhone(formattedPhone)
                    authViewModel.loginWithPhone {
                        Toast.makeText(requireContext(), "OTP Terverifikasi!", Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.action_otpInputFragment_to_profileSetupFragment)
                    }
                } else {
                    // Fallback keselamatan demo jika OTP yang dimasukkan adalah "123456"
                    if (otpCode == "123456") {
                        authViewModel.setPhone(phoneNum)
                        authViewModel.loginWithPhone {
                            findNavController().navigate(R.id.action_otpInputFragment_to_profileSetupFragment)
                        }
                    } else {
                        isError = true
                        updateOtpDisplay()
                        Toast.makeText(requireContext(), "Kode OTP salah: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
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
