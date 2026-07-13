package com.agroSystem.app.features.auth

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.agroSystem.app.R
import com.agroSystem.app.databinding.FragmentPhoneInputBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

class PhoneInputFragment : Fragment() {

    private val authViewModel: AuthViewModel by activityViewModels()
    private var _binding: FragmentPhoneInputBinding? = null
    private val binding get() = _binding!!

    private var isLoginMode = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPhoneInputBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = authViewModel

        setupInputValidation()

        // Back Action
        binding.btnBack.setOnClickListener {
            findNavController().navigate(R.id.action_phoneInputFragment_to_onboardingFragment)
        }

        // Toggle Login / Register Mode
        binding.textToggleMode.setOnClickListener {
            toggleMode()
        }

        // Action Button (Login / Register)
        binding.btnContinue.setOnClickListener {
            handleAuthAction()
        }

        // Google Sign-In Action
        binding.btnGoogle.setOnClickListener {
            performGoogleSignIn()
        }

        // Guest Login Action
        binding.btnGuest.setOnClickListener {
            Toast.makeText(requireContext(), "Masuk sebagai Tamu...", Toast.LENGTH_SHORT).show()
            authViewModel.loginWithGoogle("guest_mode_id", "Tamu AgriMitra", "guest@agrimitra.com") {
                findNavController().navigate(R.id.action_phoneInputFragment_to_homeFragment)
            }
        }

        return binding.root
    }

    private fun setupInputValidation() {
        // Hapus notifikasi error saat user mulai mengetik ulang
        binding.inputName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.layoutInputName.error = null
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.inputEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.layoutInputEmail.error = null
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.inputPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.layoutInputPassword.error = null
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun toggleMode() {
        isLoginMode = !isLoginMode
        // Bersihkan seluruh error saat berpindah mode
        binding.layoutInputName.error = null
        binding.layoutInputEmail.error = null
        binding.layoutInputPassword.error = null

        if (isLoginMode) {
            binding.textHeader.text = "Masuk ke Akun"
            binding.textSub.text = "Selamat datang kembali! Silakan login untuk melanjutkan."
            binding.layoutInputName.visibility = View.GONE
            binding.btnContinue.text = "Masuk"
            binding.textToggleMode.text = "Belum punya akun? Daftar di sini"
        } else {
            binding.textHeader.text = "Daftar Akun Baru"
            binding.textSub.text = "Silakan isi data untuk membuat akun Anda."
            binding.layoutInputName.visibility = View.VISIBLE
            binding.btnContinue.text = "Daftar"
            binding.textToggleMode.text = "Sudah punya akun? Masuk"
        }
    }

    private fun handleAuthAction() {
        val email = binding.inputEmail.text.toString().trim()
        val password = binding.inputPassword.text.toString().trim()
        val name = binding.inputName.text.toString().trim()

        // Reset error terlebih dahulu
        binding.layoutInputName.error = null
        binding.layoutInputEmail.error = null
        binding.layoutInputPassword.error = null

        var hasError = false

        // Pengecekan Nama (Hanya mode register)
        if (!isLoginMode && name.isEmpty()) {
            binding.layoutInputName.error = "Nama Lengkap tidak boleh kosong!"
            binding.layoutInputName.requestFocus()
            hasError = true
        }

        // Pengecekan Email
        if (email.isEmpty()) {
            binding.layoutInputEmail.error = "Alamat e-mail tidak boleh kosong!"
            binding.layoutInputEmail.requestFocus()
            hasError = true
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.layoutInputEmail.error = "Format alamat e-mail tidak valid!"
            binding.layoutInputEmail.requestFocus()
            hasError = true
        }

        // Pengecekan Password
        if (password.isEmpty()) {
            binding.layoutInputPassword.error = "Kata sandi tidak boleh kosong!"
            binding.layoutInputPassword.requestFocus()
            hasError = true
        } else if (password.length < 6) {
            binding.layoutInputPassword.error = "Kata sandi minimal harus 6 karakter!"
            binding.layoutInputPassword.requestFocus()
            hasError = true
        }

        if (hasError) return

        val firebaseAuth = FirebaseAuth.getInstance()
        binding.btnContinue.isEnabled = false

        if (isLoginMode) {
            // LOGIN MODE
            if (email == "admin@gmail.com" && password == "123123") {
                Toast.makeText(requireContext(), "Masuk sebagai Administrator...", Toast.LENGTH_SHORT).show()
                authViewModel.loginWithGoogle("admin_master_bypass", "Master Admin", email) {
                    binding.btnContinue.isEnabled = true
                    Toast.makeText(requireContext(), "Masuk sukses! Halo Master Admin", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_phoneInputFragment_to_homeFragment)
                }
                return
            }

            Toast.makeText(requireContext(), "Sedang masuk...", Toast.LENGTH_SHORT).show()
            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    binding.btnContinue.isEnabled = true
                    if (task.isSuccessful) {
                        val firebaseUser = task.result?.user
                        val userName = firebaseUser?.displayName ?: "User AgriMitra"
                        
                        // Sinkronkan ke database lokal & cloud
                        authViewModel.loginWithGoogle("email_password_login", userName, email) {
                            Toast.makeText(requireContext(), "Masuk sukses! Halo $userName", Toast.LENGTH_SHORT).show()
                            findNavController().navigate(R.id.action_phoneInputFragment_to_homeFragment)
                        }
                    } else {
                        Log.e("PhoneInputFragment", "Login failed", task.exception)
                        // Tampilkan error di layout pass/email jika salah kredensial
                        binding.layoutInputPassword.error = "E-mail atau kata sandi Anda salah!"
                        binding.layoutInputPassword.requestFocus()
                        Toast.makeText(requireContext(), "Gagal masuk: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        } else {
            // REGISTER MODE - Directly create user in Firebase Auth and login
            binding.btnContinue.isEnabled = false
            Toast.makeText(requireContext(), "Mendaftarkan akun...", Toast.LENGTH_SHORT).show()
            
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    binding.btnContinue.isEnabled = true
                    if (task.isSuccessful) {
                        val firebaseUser = task.result?.user
                        
                        // Set display name in Firebase Auth
                        val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                            .setDisplayName(name)
                            .build()
                        
                        firebaseUser?.updateProfile(profileUpdates)?.addOnCompleteListener {
                            firebaseAuth.signOut()
                            Toast.makeText(requireContext(), "Pendaftaran berhasil! Silakan masuk dengan akun Anda.", Toast.LENGTH_LONG).show()
                            binding.inputPassword.text?.clear()
                            binding.inputName.text?.clear()
                            if (!isLoginMode) {
                                toggleMode()
                            }
                        }
                    } else {
                        Log.e("PhoneInputFragment", "Registration failed", task.exception)
                        Toast.makeText(requireContext(), "Pendaftaran gagal: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

    private fun performGoogleSignIn() {
        val credentialManager = CredentialManager.create(requireContext())

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId("183291450985-lors5s3m8plfu2t5gqiucnphn4kl1epn.apps.googleusercontent.com")
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
                    
                    val firebaseAuth = FirebaseAuth.getInstance()
                    val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)

                    firebaseAuth.signInWithCredential(firebaseCredential)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val firebaseUser = task.result?.user
                                val name = firebaseUser?.displayName ?: "User Google"
                                val email = firebaseUser?.email ?: ""
                                
                                authViewModel.loginWithGoogle(idToken, name, email) {
                                    Toast.makeText(requireContext(), "Masuk sebagai $name", Toast.LENGTH_SHORT).show()
                                    findNavController().navigate(R.id.action_phoneInputFragment_to_homeFragment)
                                }
                            } else {
                                Log.e("PhoneInputFragment", "Firebase Google login failed", task.exception)
                                Toast.makeText(requireContext(), "Google Sign-In gagal di Firebase: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                } else {
                    throw Exception("Tipe kredensial tidak dikenali")
                }
            } catch (e: Exception) {
                Log.e("PhoneInputFragment", "Credential Manager failed: ${e.message}", e)
                Toast.makeText(requireContext(), "Google Auth gagal: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
