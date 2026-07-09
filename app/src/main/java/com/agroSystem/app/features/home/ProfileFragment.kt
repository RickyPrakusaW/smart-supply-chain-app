package com.agroSystem.app.features.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.agroSystem.app.R
import com.agroSystem.app.features.auth.AuthViewModel
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        val textName: TextView = view.findViewById(R.id.text_profile_name)
        val textContact: TextView = view.findViewById(R.id.text_profile_contact)
        val btnResetOnboarding: Button = view.findViewById(R.id.btn_reset_onboarding)

        // Reset onboarding / Logout action
        btnResetOnboarding.setOnClickListener {
            // Logout user from Room DB
            authViewModel.logout {
                // Navigate back to onboarding slide using the parentNavController
                val navController = parentFragment?.findNavController()
                navController?.navigate(R.id.action_homeFragment_to_onboardingFragment)
            }
        }

        // Dynamically bind user data using flow collection
        viewLifecycleOwner.lifecycleScope.launch {
            authViewModel.currentUser.collect { user ->
                if (user != null) {
                    textName.text = user.name
                    val contactText = when {
                        !user.email.isNullOrEmpty() && !user.phone.isNullOrEmpty() -> "${user.email} | +62 ${user.phone}"
                        !user.email.isNullOrEmpty() -> user.email
                        !user.phone.isNullOrEmpty() -> "+62 ${user.phone}"
                        else -> "Belum ada kontak"
                    }
                    textContact.text = contactText
                } else {
                    textName.text = "Tamu / Belum Masuk"
                    textContact.text = "Silakan login terlebih dahulu"
                }
            }
        }

        return view
    }
}
