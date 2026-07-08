package com.agroSystem.app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.agroSystem.app.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class LocationPermissionFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_location_permission, container, false)

        // Back action
        view.findViewById<MaterialCardView>(R.id.btn_back).setOnClickListener {
            findNavController().navigate(R.id.action_locationPermissionFragment_to_profileSetupFragment)
        }

        // Location permission buttons
        view.findViewById<MaterialButton>(R.id.btn_while_using).setOnClickListener {
            navigateToSelectAddress()
        }

        view.findViewById<MaterialButton>(R.id.btn_one_time).setOnClickListener {
            navigateToSelectAddress()
        }

        view.findViewById<Button>(R.id.btn_deny).setOnClickListener {
            navigateToSelectAddress()
        }

        return view
    }

    private fun navigateToSelectAddress() {
        findNavController().navigate(R.id.action_locationPermissionFragment_to_selectAddressFragment)
    }
}
