package com.agroSystem.app.features.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.agroSystem.app.R

class ProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        val btnResetOnboarding: Button = view.findViewById(R.id.btn_reset_onboarding)
        btnResetOnboarding.setOnClickListener {
            // Navigate back to onboarding slide using the parentNavController
            val navController = parentFragment?.findNavController()
            navController?.navigate(R.id.action_homeFragment_to_onboardingFragment)
        }

        return view
    }
}
