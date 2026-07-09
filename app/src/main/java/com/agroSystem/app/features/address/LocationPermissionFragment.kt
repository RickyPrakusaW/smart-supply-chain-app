package com.agroSystem.app.features.address

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.agroSystem.app.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class LocationPermissionFragment : Fragment() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        
        if (fineGranted || coarseGranted) {
            // Permission granted! Go to Map with GPS enabled
            val bundle = Bundle().apply {
                putBoolean("use_gps", true)
            }
            findNavController().navigate(R.id.action_locationPermissionFragment_to_selectAddressFragment, bundle)
        } else {
            // Permission denied! Go directly to manual input (empty fields)
            navigateToManualInput()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_location_permission, container, false)

        // Back action
        view.findViewById<MaterialCardView>(R.id.btn_back).setOnClickListener {
            findNavController().navigateUp()
        }

        // Location permission buttons
        view.findViewById<MaterialButton>(R.id.btn_while_using).setOnClickListener {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }

        view.findViewById<MaterialButton>(R.id.btn_one_time).setOnClickListener {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }

        view.findViewById<Button>(R.id.btn_deny).setOnClickListener {
            // User explicitly denied permission
            navigateToManualInput()
        }

        return view
    }

    private fun navigateToManualInput() {
        val bundle = Bundle().apply {
            putString("street", "")
            putString("detail", "")
        }
        findNavController().navigate(R.id.action_locationPermissionFragment_to_addressDetailFragment, bundle)
    }
}
