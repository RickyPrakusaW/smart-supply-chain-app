package com.agroSystem.app.features.payment

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.agroSystem.app.R
import com.agroSystem.app.features.shared.MainSharedViewModel
import com.google.android.material.card.MaterialCardView

class PaymentWebViewFragment : Fragment() {

    private val sharedViewModel: MainSharedViewModel by activityViewModels()
    
    private lateinit var webView: WebView
    private lateinit var progressLoader: ProgressBar
    private lateinit var btnClose: MaterialCardView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_payment_webview, container, false)

        webView = view.findViewById(R.id.web_view_payment)
        progressLoader = view.findViewById(R.id.progress_loader)
        btnClose = view.findViewById(R.id.btn_close)

        val paymentUrl = arguments?.getString("payment_url") ?: ""
        if (paymentUrl.isEmpty()) {
            Toast.makeText(requireContext(), "URL Pembayaran tidak ditemukan!", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
            return view
        }

        setupWebView()
        webView.loadUrl(paymentUrl)

        btnClose.setOnClickListener {
            showExitConfirmationDialog()
        }

        return view
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        val settings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true

        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                super.onPageStarted(view, url, favicon)
                progressLoader.visibility = View.VISIBLE
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                progressLoader.visibility = View.GONE
            }

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url?.toString() ?: return false
                Log.d("PaymentWebView", "Intercepted URL: $url")

                // Intercept custom mock simulator deep link redirects
                if (url.startsWith("agrimitra://payment_result")) {
                    val uri = request.url
                    val status = uri.getQueryParameter("status") ?: "pending"
                    handlePaymentResult(status)
                    return true
                }

                // Intercept general Midtrans snap success/fail callback keywords
                if (url.contains("status=success") || url.contains("transaction_status=settlement") || url.contains("/payment-finish")) {
                    handlePaymentResult("success")
                    return true
                }
                if (url.contains("status=failed") || url.contains("transaction_status=deny") || url.contains("transaction_status=cancel")) {
                    handlePaymentResult("failed")
                    return true
                }
                if (url.contains("/snap/v2/vtweb/close")) {
                    handlePaymentResult("pending")
                    return true
                }

                return false
            }
        }
    }

    private fun handlePaymentResult(status: String) {
        activity?.runOnUiThread {
            when (status) {
                "success" -> {
                    sharedViewModel.clearCart() // Clear cart on success!
                    AlertDialog.Builder(requireContext())
                        .setTitle("Pembayaran Sukses!")
                        .setMessage("Terima kasih, pembayaran Anda telah berhasil kami terima. Pesanan Anda akan segera diproses oleh Mitra Tani.")
                        .setCancelable(false)
                        .setPositiveButton("Kembali ke Home") { _, _ ->
                            findNavController().popBackStack(R.id.homeFragment, false)
                        }
                        .show()
                }
                "failed" -> {
                    AlertDialog.Builder(requireContext())
                        .setTitle("Pembayaran Gagal")
                        .setMessage("Mohon maaf, transaksi pembayaran Anda tidak berhasil. Silakan coba kembali beberapa saat lagi.")
                        .setCancelable(false)
                        .setPositiveButton("Tutup") { _, _ ->
                            findNavController().navigateUp()
                        }
                        .show()
                }
                else -> {
                    // Pending status
                    sharedViewModel.clearCart()
                    AlertDialog.Builder(requireContext())
                        .setTitle("Pembayaran Tertunda")
                        .setMessage("Transaksi Anda sedang diproses atau menunggu pembayaran dari Anda (Menunggu instruksi pembayaran transfer/e-wallet).")
                        .setCancelable(false)
                        .setPositiveButton("Lihat Pesanan") { _, _ ->
                            findNavController().popBackStack(R.id.homeFragment, false)
                        }
                        .show()
                }
            }
        }
    }

    private fun showExitConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Batalkan Pembayaran?")
            .setMessage("Apakah Anda yakin ingin menutup halaman pembayaran? Transaksi Anda mungkin akan tertunda.")
            .setPositiveButton("Ya, Keluar") { _, _ ->
                findNavController().navigateUp()
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}
