package com.agroSystem.app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.agroSystem.app.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class SelectAddressFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_select_address, container, false)

        // Back action
        view.findViewById<MaterialCardView>(R.id.btn_back).setOnClickListener {
            findNavController().navigate(R.id.action_selectAddressFragment_to_locationPermissionFragment)
        }

        // Continue action
        view.findViewById<MaterialButton>(R.id.btn_continue).setOnClickListener {
            findNavController().navigate(R.id.action_selectAddressFragment_to_addressDetailFragment)
        }

        return view
    }
}
