package com.agroSystem.app.features.address

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.agroSystem.app.R
import com.agroSystem.app.features.auth.AuthViewModel
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import java.util.Locale

class SelectAddressFragment : Fragment() {

    private val authViewModel: AuthViewModel by activityViewModels()

    private lateinit var mapView: MapView
    private lateinit var inputSearch: EditText
    
    private lateinit var layoutStreet: TextInputLayout
    private lateinit var layoutDetail: TextInputLayout
    private lateinit var layoutPostalCode: TextInputLayout
    private lateinit var layoutLandmark: TextInputLayout

    private lateinit var inputStreet: EditText
    private lateinit var inputDetail: EditText
    private lateinit var inputPostalCode: EditText
    private lateinit var inputLandmark: EditText
    
    private lateinit var btnSaveAddress: Button

    private var geocodeJob: Job? = null
    private var isFirstLoad = true
    private var isProgrammaticMove = false

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineGranted || coarseGranted) {
            centerMapOnGps()
        } else {
            Toast.makeText(requireContext(), "Akses lokasi ditolak, silakan geser peta manual.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize OSMDroid Configuration
        val ctx = requireContext().applicationContext
        Configuration.getInstance().load(ctx, ctx.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
        Configuration.getInstance().userAgentValue = ctx.packageName
        
        // Use internal cache directory to prevent write permission errors on modern Android (resolves blank map bug)
        val tileCache = java.io.File(ctx.cacheDir, "osmdroid")
        Configuration.getInstance().osmdroidTileCache = tileCache
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_select_address, container, false)

        mapView = view.findViewById(R.id.map_view)
        inputSearch = view.findViewById(R.id.input_search_address)

        layoutStreet = view.findViewById(R.id.layout_selected_street)
        layoutDetail = view.findViewById(R.id.layout_selected_detail)
        layoutPostalCode = view.findViewById(R.id.layout_selected_postal_code)
        layoutLandmark = view.findViewById(R.id.layout_selected_landmark)

        inputStreet = view.findViewById(R.id.text_selected_street)
        inputDetail = view.findViewById(R.id.text_selected_detail)
        inputPostalCode = view.findViewById(R.id.text_selected_postal_code)
        inputLandmark = view.findViewById(R.id.text_selected_landmark)
        
        btnSaveAddress = view.findViewById(R.id.btn_continue)

        // Set Map Settings
        mapView.setMultiTouchControls(true)
        mapView.setBuiltInZoomControls(true)
        val mapController = mapView.controller
        mapController.setZoom(16.0)

        // Default Center Point: Malang, East Java (-7.9839, 112.6214)
        val defaultPoint = GeoPoint(-7.9839, 112.6214)
        mapController.setCenter(defaultPoint)

        // Pre-populate fields from current user address backup
        val currentUser = authViewModel.currentUser.value
        if (currentUser != null) {
            val addr = currentUser.address ?: ""
            parseAndSetAddressFields(addr, inputStreet, inputDetail, inputLandmark, inputPostalCode)
            if (addr.isNotEmpty()) {
                var streetStr = ""
                var detailStr = ""
                try {
                    val patokanIndex = addr.indexOf(" (Patokan: ")
                    var mainPart = addr
                    if (patokanIndex != -1) {
                        mainPart = addr.substring(0, patokanIndex)
                    }
                    val commaIndex = mainPart.indexOf(", ")
                    if (commaIndex != -1) {
                        streetStr = mainPart.substring(0, commaIndex)
                        detailStr = mainPart.substring(commaIndex + 2)
                    } else {
                        streetStr = mainPart
                    }
                } catch (e: Exception) {
                    streetStr = addr
                }
                val cleanAddr = if (detailStr.isNotEmpty()) "$streetStr, $detailStr" else streetStr
                if (cleanAddr.isNotEmpty()) {
                    geocodeAddressAndCenter(cleanAddr)
                }
            }
        }

        // Request runtime permission and center map if granted
        if (currentUser?.address.isNullOrEmpty()) {
            checkAndRequestLocation()
        }

        // Back Action
        view.findViewById<MaterialCardView>(R.id.btn_back).setOnClickListener {
            findNavController().navigateUp()
        }

        // Listen to Map drag to update address inputs dynamically
        mapView.addMapListener(object : MapListener {
            override fun onScroll(event: ScrollEvent?): Boolean {
                if (isFirstLoad) {
                    isFirstLoad = false
                    return true
                }
                triggerReverseGeocoding()
                return true
            }

            override fun onZoom(event: ZoomEvent?): Boolean {
                triggerReverseGeocoding()
                return true
            }
        })

        // Search Input Action
        inputSearch.setOnEditorActionListener { _, _, _ ->
            val query = inputSearch.text.toString().trim()
            if (query.isNotEmpty()) {
                performSearch(query)
            }
            true
        }

        // Clear error text on text changes
        inputStreet.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                layoutStreet.error = null
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        inputDetail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                layoutDetail.error = null
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        inputPostalCode.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                layoutPostalCode.error = null
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        inputLandmark.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                layoutLandmark.error = null
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Save & Sync Address
        btnSaveAddress.setOnClickListener {
            val street = inputStreet.text.toString().trim()
            val detail = inputDetail.text.toString().trim()
            val postalCode = inputPostalCode.text.toString().trim()
            val landmark = inputLandmark.text.toString().trim()

            // Reset errors
            layoutStreet.error = null
            layoutDetail.error = null
            layoutPostalCode.error = null
            layoutLandmark.error = null

            var hasError = false

            // Strict Validations
            if (street.length < 5) {
                layoutStreet.error = "Nama jalan utama harus diisi (minimal 5 karakter)!"
                layoutStreet.requestFocus()
                hasError = true
            }

            if (detail.length < 5) {
                layoutDetail.error = "Detail No. Rumah / RT-RW harus diisi (minimal 5 karakter)!"
                layoutDetail.requestFocus()
                hasError = true
            }

            if (postalCode.length != 5 || !postalCode.all { it.isDigit() }) {
                layoutPostalCode.error = "Kode pos harus berupa 5 digit angka!"
                layoutPostalCode.requestFocus()
                hasError = true
            }

            if (landmark.length < 3) {
                layoutLandmark.error = "Patokan alamat harus diisi (minimal 3 karakter)!"
                layoutLandmark.requestFocus()
                hasError = true
            }

            if (hasError) return@setOnClickListener

            // Combine composite address format
            val fullAddress = "$street, $detail (Patokan: $landmark, Kode Pos: $postalCode)"

            if (currentUser != null) {
                Toast.makeText(requireContext(), "Menyimpan alamat...", Toast.LENGTH_SHORT).show()
                authViewModel.updateProfile(
                    name = currentUser.name,
                    email = currentUser.email,
                    phone = currentUser.phone,
                    address = fullAddress,
                    photoUrl = currentUser.photoUrl,
                    role = currentUser.role
                ) {
                    Toast.makeText(requireContext(), "Alamat berhasil disimpan!", Toast.LENGTH_SHORT).show()
                    val popped = findNavController().popBackStack(R.id.homeFragment, false)
                    if (!popped) {
                        findNavController().navigate(R.id.action_selectAddressFragment_to_homeFragment)
                    }
                }
            } else {
                val popped = findNavController().popBackStack(R.id.homeFragment, false)
                if (!popped) {
                    findNavController().navigate(R.id.action_selectAddressFragment_to_homeFragment)
                }
            }
        }

        return view
    }

    private fun checkAndRequestLocation() {
        val finePermission = Manifest.permission.ACCESS_FINE_LOCATION
        val coarsePermission = Manifest.permission.ACCESS_COARSE_LOCATION
        val fineGranted = ContextCompat.checkSelfPermission(requireContext(), finePermission) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(requireContext(), coarsePermission) == PackageManager.PERMISSION_GRANTED
        
        if (fineGranted || coarseGranted) {
            centerMapOnGps()
        } else {
            requestPermissionLauncher.launch(arrayOf(finePermission, coarsePermission))
        }
    }

    private fun centerMapOnGps() {
        try {
            val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager
            val providers = locationManager.getProviders(true)
            var bestLocation: android.location.Location? = null
            for (provider in providers) {
                val loc = locationManager.getLastKnownLocation(provider) ?: continue
                if (bestLocation == null || loc.accuracy < bestLocation.accuracy) {
                    bestLocation = loc
                }
            }
            if (bestLocation != null && (bestLocation.latitude != 0.0 || bestLocation.longitude != 0.0)) {
                val centerPoint = GeoPoint(bestLocation.latitude, bestLocation.longitude)
                isProgrammaticMove = true
                mapView.controller.setCenter(centerPoint)
                lifecycleScope.launch {
                    delay(1500)
                    isProgrammaticMove = false
                }
                triggerReverseGeocoding()
            }
        } catch (e: SecurityException) {
            Log.e("SelectAddress", "Location permission security exception", e)
        } catch (e: Exception) {
            Log.e("SelectAddress", "Error retrieving GPS coordinates", e)
        }
    }

    private fun triggerReverseGeocoding() {
        if (isProgrammaticMove) return
        // Debounce map moves to prevent excessive geocoding calls
        geocodeJob?.cancel()
        geocodeJob = lifecycleScope.launch {
            delay(800) // wait for map panning to stop
            val center = mapView.mapCenter
            val lat = center.latitude
            val lon = center.longitude
            reverseGeocode(lat, lon)
        }
    }

    private suspend fun reverseGeocode(latitude: Double, longitude: Double) {
        withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(requireContext(), Locale.getDefault())
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    val addr = addresses[0]
                    
                    val street = addr.thoroughfare ?: addr.subLocality ?: addr.featureName ?: "Jalan Tanpa Nama"
                    val subdistrict = buildString {
                        addr.locality?.let { append("$it, ") }
                        addr.subAdminArea?.let { append("$it, ") }
                        addr.adminArea?.let { append(it) }
                    }.trimEnd(',', ' ')

                    withContext(Dispatchers.Main) {
                        inputStreet.setText(street)
                        inputDetail.setText(subdistrict)
                    }
                }
            } catch (e: Exception) {
                Log.e("SelectAddress", "Reverse Geocoding Failed", e)
            }
        }
    }

    private fun performSearch(query: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(requireContext(), Locale.getDefault())
                val addresses = geocoder.getFromLocationName(query, 1)
                if (!addresses.isNullOrEmpty()) {
                    val addr = addresses[0]
                    val point = GeoPoint(addr.latitude, addr.longitude)
                    withContext(Dispatchers.Main) {
                        isProgrammaticMove = true
                        mapView.controller.animateTo(point)
                        lifecycleScope.launch {
                            delay(1500)
                            isProgrammaticMove = false
                        }
                        Toast.makeText(requireContext(), "Menemukan lokasi: $query", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Lokasi tidak ditemukan.", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Pencarian gagal: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun geocodeAddressAndCenter(addressStr: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(requireContext(), Locale.getDefault())
                val addresses = geocoder.getFromLocationName(addressStr, 1)
                if (!addresses.isNullOrEmpty()) {
                    val addr = addresses[0]
                    val point = GeoPoint(addr.latitude, addr.longitude)
                    withContext(Dispatchers.Main) {
                        isProgrammaticMove = true
                        mapView.controller.setCenter(point)
                        lifecycleScope.launch {
                            delay(2000)
                            isProgrammaticMove = false
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("SelectAddress", "Error geocoding startup address", e)
            }
        }
    }

    private fun parseAndSetAddressFields(
        address: String?,
        inputStreet: EditText,
        inputDetail: EditText,
        inputLandmark: EditText,
        inputPostalCode: EditText
    ) {
        if (address.isNullOrEmpty()) {
            inputStreet.setText("")
            inputDetail.setText("")
            inputLandmark.setText("")
            inputPostalCode.setText("")
            return
        }
        try {
            var streetStr = ""
            var detailStr = ""
            var landmarkStr = ""
            var postalCodeStr = ""
            
            val patokanIndex = address.indexOf(" (Patokan: ")
            var mainPart = address
            if (patokanIndex != -1) {
                val bracketsContent = address.substring(patokanIndex + 11, address.length - 1)
                val zipIndex = bracketsContent.indexOf(", Kode Pos: ")
                if (zipIndex != -1) {
                    landmarkStr = bracketsContent.substring(0, zipIndex)
                    postalCodeStr = bracketsContent.substring(zipIndex + 12)
                } else {
                    landmarkStr = bracketsContent
                }
                mainPart = address.substring(0, patokanIndex)
            }
            
            val commaIndex = mainPart.indexOf(", ")
            if (commaIndex != -1) {
                streetStr = mainPart.substring(0, commaIndex)
                detailStr = mainPart.substring(commaIndex + 2)
            } else {
                streetStr = mainPart
            }
            
            inputStreet.setText(streetStr)
            inputDetail.setText(detailStr)
            inputLandmark.setText(landmarkStr)
            inputPostalCode.setText(postalCodeStr)
        } catch (e: Exception) {
            inputStreet.setText(address)
            inputDetail.setText("")
            inputLandmark.setText("")
            inputPostalCode.setText("")
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }
}
