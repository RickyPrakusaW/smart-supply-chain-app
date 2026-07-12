package com.agroSystem.app.features.payment

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import java.util.Locale
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
    private var currentOrderId: String = ""

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
        if (paymentUrl.startsWith("mock://payment")) {
            val uri = android.net.Uri.parse(paymentUrl)
            currentOrderId = uri.getQueryParameter("orderId") ?: "TRX-UNKNOWN"
            val amount = uri.getQueryParameter("amount") ?: "0"
            val html = getMockPaymentHtml(currentOrderId, amount)
            webView.loadDataWithBaseURL("https://app.sandbox.midtrans.com", html, "text/html", "utf-8", null)
        } else {
            webView.loadUrl(paymentUrl)
        }

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
            sharedViewModel.updateOrderStatus(currentOrderId, status) { }
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

    private fun getMockPaymentHtml(orderId: String, amount: String): String {
        val formattedPrice = java.text.NumberFormat.getNumberInstance(Locale("id", "ID")).format(amount.toDoubleOrNull() ?: 0.0)
        return """
            <html>
              <head>
                <title>Midtrans Secure Payment (Simulasi)</title>
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                  body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif; background: #F5F3EE; color: #333333; margin: 0; padding: 12px; display: flex; align-items: center; justify-content: center; min-height: 100vh; }
                  .container { background: white; border-radius: 12px; box-shadow: 0 4px 20px rgba(0,0,0,0.08); max-width: 420px; width: 100%; overflow: hidden; display: flex; flex-direction: column; border: 1px solid #D3CEC6; }
                  .header { background: #475B40; color: white; padding: 16px 20px; display: flex; align-items: center; justify-content: space-between; }
                  .header-title { font-size: 16px; font-weight: bold; letter-spacing: 0.5px; }
                  .header-subtitle { font-size: 11px; color: #D6D9D0; }
                  .amount-box { text-align: right; }
                  .amount-val { font-size: 18px; font-weight: 800; color: #10B981; }
                  .order-id { font-size: 10px; color: #E8E8E2; text-align: right; margin-top: 2px; }
                  .banner { background: #EEEDE3; border-bottom: 1px solid #D3CEC6; padding: 12px 20px; font-size: 12px; color: #475B40; display: flex; align-items: center; gap: 8px; }
                  .content { padding: 20px; display: flex; flex-direction: column; gap: 16px; }
                  .info-text { font-size: 13px; color: #6b7280; line-height: 1.4; text-align: center; }
                  .actions { display: flex; flex-direction: column; gap: 10px; margin-top: 10px; }
                  .btn-action { display: block; width: 100%; padding: 12px; border-radius: 8px; border: none; font-size: 14px; font-weight: bold; cursor: pointer; text-align: center; box-sizing: border-box; }
                  .btn-primary { background: #475B40; color: white; }
                  .btn-primary:hover { background: #374632; }
                  .btn-danger { background: #EF4444; color: white; }
                  .btn-danger:hover { background: #DC2626; }
                </style>
              </head>
              <body>
                <div class="container">
                  <div class="header">
                    <div>
                      <div class="header-title">AgriMitra Secure Pay</div>
                      <div class="header-subtitle">Mode Uji Simulasi</div>
                    </div>
                    <div class="amount-box">
                      <div class="amount-val">Rp ${formattedPrice}</div>
                      <div class="order-id">${orderId}</div>
                    </div>
                  </div>
                  <div class="banner">
                    <span>🛡️</span>
                    <span>Simulator Pembayaran Midtrans (AgriMitra Offline)</span>
                  </div>
                  <div class="content">
                    <p class="info-text">Ini adalah simulator pembayaran terintegrasi untuk aplikasi AgriMitra. Pilih salah satu tombol di bawah untuk menyimulasikan hasil transaksi.</p>
                    <div class="actions">
                      <button class="btn-action btn-primary" onclick="window.location.href='agrimitra://payment_result?status=success&orderId=${orderId}'">BAYAR SEKARANG (SUKSES)</button>
                      <button class="btn-action btn-danger" onclick="window.location.href='agrimitra://payment_result?status=failed&orderId=${orderId}'">BATALKAN TRANSAKSI (GAGAL)</button>
                    </div>
                  </div>
                </div>
              </body>
            </html>
        """.trimIndent()
    }
}
