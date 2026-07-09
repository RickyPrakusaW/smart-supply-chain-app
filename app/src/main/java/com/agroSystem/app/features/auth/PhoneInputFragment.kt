package com.agroSystem.app.features.auth

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.agroSystem.app.R
import com.agroSystem.app.databinding.FragmentPhoneInputBinding
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch

class PhoneInputFragment : Fragment() {

    private val authViewModel: AuthViewModel by activityViewModels()
    private var _binding: FragmentPhoneInputBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPhoneInputBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = authViewModel

        // Back action
        binding.btnBack.setOnClickListener {
            findNavController().navigate(R.id.action_phoneInputFragment_to_onboardingFragment)
        }

        // Guest mode action
        binding.btnGuest.setOnClickListener {
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
            binding.root.findViewById<MaterialButton>(id).setOnClickListener {
                authViewModel.appendPhoneDigit(char)
            }
        }

        binding.root.findViewById<MaterialButton>(R.id.key_del).setOnClickListener {
            authViewModel.deletePhoneDigit()
        }

        // Continue action
        binding.btnContinue.setOnClickListener {
            val phoneVal = authViewModel.phone.value ?: ""
            val bundle = Bundle().apply {
                putString("phone", phoneVal)
            }
            findNavController().navigate(R.id.action_phoneInputFragment_to_otpInputFragment, bundle)
        }

        // Google Sign-In action using Android Credential Manager
        binding.btnGoogle.setOnClickListener {
            performGoogleSignIn()
        }

        // Observe phone state changes to update UI
        authViewModel.phone.observe(viewLifecycleOwner) { phoneVal ->
            if (phoneVal.isEmpty()) {
                binding.textPhoneNumber.text = ""
                binding.textPhonePlaceholder.visibility = View.VISIBLE
                binding.btnContinue.isEnabled = false
            } else {
                binding.textPhoneNumber.text = formatPhone(phoneVal)
                binding.textPhonePlaceholder.visibility = View.GONE
                binding.btnContinue.isEnabled = phoneVal.length >= 9
            }
        }

        return binding.root
    }

    private fun performGoogleSignIn() {
        val credentialManager = CredentialManager.create(requireContext())

        // Set up the Google ID request
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId("183291450985-phvcp8lkqn2hmh0k1ep6vmss9asj4qtv.apps.googleusercontent.com")
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val result = credentialManager.getCredential(
                    context = requireActivity(),
                    request = request
                )
                val credential = result.credential
                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val idToken = googleIdTokenCredential.idToken
                    val email = googleIdTokenCredential.id
                    val name = googleIdTokenCredential.displayName ?: googleIdTokenCredential.givenName ?: "User Google"

                    authViewModel.loginWithGoogle(idToken, name, email) {
                        Toast.makeText(requireContext(), "Masuk sebagai $name", Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.action_phoneInputFragment_to_homeFragment)
                    }
                } else {
                    throw Exception("Tipe kredensial tidak dikenali")
                }
            } catch (e: Exception) {
                Log.e("PhoneInputFragment", "Credential Manager failed, falling back to mock login: ${e.message}", e)
                Toast.makeText(requireContext(), "Mode Demo: Menggunakan login Google simulasi", Toast.LENGTH_SHORT).show()
                
                // Fallback demo mode mock login
                authViewModel.loginWithGoogle(
                    idToken = "mock_google_id_token",
                    name = "Ricky Prakusa",
                    email = "ricky.prakusa@gmail.com"
                ) {
                    findNavController().navigate(R.id.action_phoneInputFragment_to_homeFragment)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

