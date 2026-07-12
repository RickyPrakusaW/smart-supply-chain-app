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
            <!DOCTYPE html>
            <html>
            <head>
              <title>Midtrans Snap (Simulasi)</title>
              <meta name="viewport" content="width=device-width, initial-scale=1.0">
              <style>
                body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif; background: #F5F3EE; color: #333333; margin: 0; padding: 12px; display: flex; align-items: center; justify-content: center; min-height: 100vh; }
                .container { background: white; border-radius: 12px; box-shadow: 0 4px 20px rgba(0,0,0,0.08); max-width: 420px; width: 100%; overflow: hidden; display: flex; flex-direction: column; border: 1px solid #D3CEC6; box-sizing: border-box; }
                .header { background: #ffffff; border-bottom: 1px solid #E8E8E2; padding: 16px 20px; display: flex; align-items: center; justify-content: space-between; }
                .merchant-logo { font-size: 16px; font-weight: bold; color: #475B40; display: flex; align-items: center; gap: 6px; }
                .amount-box { text-align: right; }
                .amount-label { font-size: 9px; color: #8C8A82; font-weight: bold; letter-spacing: 0.5px; }
                .amount-val { font-size: 16px; font-weight: 800; color: #475B40; }
                .order-id { font-size: 10px; color: #8C8A82; }
                
                .midtrans-snap-header { background: #1B75BB; color: white; padding: 10px 20px; font-size: 12px; font-weight: bold; display: flex; align-items: center; justify-content: space-between; }
                
                .content { padding: 20px; display: flex; flex-direction: column; gap: 16px; min-height: 340px; }
                .section-title { font-size: 14px; font-weight: bold; color: #333; margin-bottom: 8px; }
                
                /* Payment List */
                .payment-list { display: flex; flex-direction: column; gap: 10px; }
                .payment-item { display: flex; align-items: center; justify-content: space-between; padding: 14px 16px; border: 1px solid #E8E8E2; border-radius: 8px; cursor: pointer; transition: background 0.2s; }
                .payment-item:hover { background: #F9F8F5; }
                .payment-item-left { display: flex; align-items: center; gap: 12px; }
                .payment-icon { font-size: 20px; }
                .payment-name { font-size: 13px; font-weight: 600; color: #333; }
                .payment-desc { font-size: 11px; color: #8C8A82; margin-top: 2px; }
                .arrow-right { font-size: 14px; color: #C4C2BA; }
                
                /* Details view */
                .details-view { display: none; flex-direction: column; gap: 16px; }
                .back-btn { font-size: 13px; font-weight: bold; color: #1B75BB; cursor: pointer; display: inline-flex; align-items: center; gap: 4px; border: none; background: none; padding: 0; align-self: flex-start; }
                
                .qr-box { display: flex; flex-direction: column; align-items: center; gap: 8px; padding: 16px; border: 1px dashed #D3CEC6; border-radius: 8px; background: #FAF9F6; }
                
                .va-box { background: #FAF9F6; border: 1px solid #E8E8E2; border-radius: 8px; padding: 16px; }
                .va-row { display: flex; justify-content: space-between; margin-bottom: 8px; font-size: 13px; }
                .va-label { color: #8C8A82; }
                .va-val { font-weight: bold; color: #333; }
                .va-num { font-size: 16px; font-weight: 800; color: #1B75BB; letter-spacing: 0.5px; }
                
                .cc-form { display: flex; flex-direction: column; gap: 10px; }
                .form-group { display: flex; flex-direction: column; gap: 4px; }
                .form-group label { font-size: 11px; color: #8C8A82; }
                .form-group input { padding: 10px; border: 1px solid #D3CEC6; border-radius: 6px; font-size: 13px; width: 100%; box-sizing: border-box; }
                
                .info-text { font-size: 12px; color: #6b7280; line-height: 1.4; text-align: center; margin: 0; }
                .actions { display: flex; flex-direction: column; gap: 8px; margin-top: auto; }
                .btn { display: block; width: 100%; padding: 12px; border-radius: 8px; border: none; font-size: 14px; font-weight: bold; cursor: pointer; text-align: center; box-sizing: border-box; }
                .btn-pay { background: #1B75BB; color: white; }
                .btn-pay:hover { background: #155d96; }
                .btn-cancel { background: #EF4444; color: white; }
                .btn-cancel:hover { background: #dc2626; }
                .btn-sec { background: #F3F4F6; color: #4B5563; }
              </style>
            </head>
            <body>
              <div class="container">
                <!-- Merchant Header -->
                <div class="header">
                  <div class="merchant-logo">
                    <span>🌾</span> AgriMitra
                  </div>
                  <div class="amount-box">
                    <div class="amount-label">TOTAL BELANJA</div>
                    <div class="amount-val">Rp ${formattedPrice}</div>
                    <div class="order-id">ID: ${orderId}</div>
                  </div>
                </div>
                
                <!-- Midtrans Snap Indicator -->
                <div class="midtrans-snap-header">
                  <span>🛡️ Midtrans snap</span>
                  <span>TEST MODE (SIMULASI)</span>
                </div>
                
                <!-- Content Section -->
                <div class="content">
                  <!-- SCREEN 1: LIST METODE -->
                  <div id="screen-list" style="display: block;">
                    <div class="section-title">Pilih Metode Pembayaran</div>
                    <div class="payment-list">
                      <div class="payment-item" onclick="showScreen('gopay')">
                        <div class="payment-item-left">
                          <span class="payment-icon">📱</span>
                          <div style="text-align: left;">
                            <div class="payment-name">GoPay / ShopeePay / QRIS</div>
                            <div class="payment-desc">Bayar instan menggunakan aplikasi e-wallet</div>
                          </div>
                        </div>
                        <span class="arrow-right">❯</span>
                      </div>
                      
                      <div class="payment-item" onclick="showScreen('bca')">
                        <div class="payment-item-left">
                          <span class="payment-icon">🏦</span>
                          <div style="text-align: left;">
                            <div class="payment-name">BCA Virtual Account</div>
                            <div class="payment-desc">Transfer via m-BCA, KlikBCA, atau ATM BCA</div>
                          </div>
                        </div>
                        <span class="arrow-right">❯</span>
                      </div>
                      
                      <div class="payment-item" onclick="showScreen('mandiri')">
                        <div class="payment-item-left">
                          <span class="payment-icon">🏛️</span>
                          <div style="text-align: left;">
                            <div class="payment-name">Mandiri Virtual Account</div>
                            <div class="payment-desc">Transfer via Livin' by Mandiri atau ATM Mandiri</div>
                          </div>
                        </div>
                        <span class="arrow-right">❯</span>
                      </div>
                      
                      <div class="payment-item" onclick="showScreen('cc')">
                        <div class="payment-item-left">
                          <span class="payment-icon">💳</span>
                          <div style="text-align: left;">
                            <div class="payment-name">Kartu Kredit / Debit Online</div>
                            <div class="payment-desc">Mendukung Visa, MasterCard, JCB, Amex</div>
                          </div>
                        </div>
                        <span class="arrow-right">❯</span>
                      </div>
                    </div>
                    
                    <div class="actions" style="margin-top: 24px;">
                      <button class="btn btn-cancel" onclick="window.location.href='agrimitra://payment_result?status=failed&orderId=${orderId}'">Batalkan Transaksi</button>
                    </div>
                  </div>
                  
                  <!-- SCREEN 2: DETAILS (GOPAY/QRIS) -->
                  <div id="screen-gopay" class="details-view">
                    <button class="back-btn" onclick="showList()">❮ Kembali ke pilihan</button>
                    <div class="section-title">Scan Kode QRIS</div>
                    <div class="qr-box">
                      <!-- Inline Mock QR SVG -->
                      <svg width="140" height="140" viewBox="0 0 100 100" style="background:#fff; padding:6px; border-radius:4px;">
                        <rect x="10" y="10" width="20" height="20" fill="#000"/>
                        <rect x="15" y="15" width="10" height="10" fill="#fff"/>
                        <rect x="70" y="10" width="20" height="20" fill="#000"/>
                        <rect x="75" y="15" width="10" height="10" fill="#fff"/>
                        <rect x="10" y="70" width="20" height="20" fill="#000"/>
                        <rect x="15" y="75" width="10" height="10" fill="#fff"/>
                        <rect x="40" y="40" width="20" height="20" fill="#000"/>
                        <rect x="45" y="45" width="10" height="10" fill="#fff"/>
                        <rect x="40" y="15" width="5" height="15" fill="#000"/>
                        <rect x="15" y="40" width="15" height="5" fill="#000"/>
                        <rect x="70" y="45" width="15" height="10" fill="#000"/>
                        <rect x="45" y="70" width="10" height="15" fill="#000"/>
                        <rect x="70" y="70" width="20" height="20" fill="#000"/>
                      </svg>
                      <div style="font-size: 11px; font-weight: bold; color: #555; margin-top: 4px;">GOPAY / SHOPEEPAY / LINKAJA</div>
                    </div>
                    <p class="info-text">Silakan klik "Konfirmasi Pembayaran" di bawah untuk menyimulasikan pembayaran e-wallet yang berhasil.</p>
                    <div class="actions">
                      <button class="btn btn-pay" onclick="window.location.href='agrimitra://payment_result?status=success&orderId=${orderId}'">Konfirmasi Pembayaran</button>
                      <button class="btn btn-sec" onclick="showList()">Ubah Metode</button>
                    </div>
                  </div>
                  
                  <!-- SCREEN 3: DETAILS (BCA VA) -->
                  <div id="screen-bca" class="details-view">
                    <button class="back-btn" onclick="showList()">❮ Kembali ke pilihan</button>
                    <div class="section-title" style="text-align: left;">BCA Virtual Account</div>
                    <div class="va-box" style="text-align: left;">
                      <div class="va-row">
                        <span class="va-label">Nama Perusahaan</span>
                        <span class="va-val">AGRIMITRA SEJAHTERA</span>
                      </div>
                      <div class="va-row">
                        <span class="va-label">Nomor Virtual Account</span>
                        <span class="va-num">8077 0812 7394 8820</span>
                      </div>
                      <div class="va-row">
                        <span class="va-label">Total Pembayaran</span>
                        <span class="va-val" style="color: #475B40;">Rp ${formattedPrice}</span>
                      </div>
                    </div>
                    <p class="info-text">Salin nomor VA di atas dan lakukan pembayaran via M-BCA atau ATM BCA. Lalu klik tombol di bawah.</p>
                    <div class="actions">
                      <button class="btn btn-pay" onclick="window.location.href='agrimitra://payment_result?status=success&orderId=${orderId}'">Saya Sudah Bayar</button>
                      <button class="btn btn-sec" onclick="showList()">Ubah Metode</button>
                    </div>
                  </div>
                  
                  <!-- SCREEN 4: DETAILS (MANDIRI VA) -->
                  <div id="screen-mandiri" class="details-view">
                    <button class="back-btn" onclick="showList()">❮ Kembali ke pilihan</button>
                    <div class="section-title" style="text-align: left;">Mandiri Virtual Account</div>
                    <div class="va-box" style="text-align: left;">
                      <div class="va-row">
                        <span class="va-label">Kode Perusahaan</span>
                        <span class="va-val">89201</span>
                      </div>
                      <div class="va-row">
                        <span class="va-label">Nomor Virtual Account</span>
                        <span class="va-num">89201 0812 7394 8820</span>
                      </div>
                      <div class="va-row">
                        <span class="va-label">Total Pembayaran</span>
                        <span class="va-val" style="color: #475B40;">Rp ${formattedPrice}</span>
                      </div>
                    </div>
                    <p class="info-text">Silakan lakukan pembayaran menggunakan aplikasi Livin' Mandiri atau ATM Mandiri. Lalu klik konfirmasi.</p>
                    <div class="actions">
                      <button class="btn btn-pay" onclick="window.location.href='agrimitra://payment_result?status=success&orderId=${orderId}'">Saya Sudah Bayar</button>
                      <button class="btn btn-sec" onclick="showList()">Ubah Metode</button>
                    </div>
                  </div>
                  
                  <!-- SCREEN 5: DETAILS (CREDIT CARD) -->
                  <div id="screen-cc" class="details-view">
                    <button class="back-btn" onclick="showList()">❮ Kembali ke pilihan</button>
                    <div class="section-title" style="text-align: left;">Kartu Kredit / Debit Online</div>
                    <div class="cc-form" style="text-align: left;">
                      <div class="form-group">
                        <label>Nomor Kartu</label>
                        <input type="text" placeholder="4111 2222 3333 4444" value="4111222233334444">
                      </div>
                      <div style="display: flex; gap: 10px;">
                        <div class="form-group" style="flex: 1;">
                          <label>Masa Berlaku (MM/YY)</label>
                          <input type="text" placeholder="12/28" value="12/28">
                        </div>
                        <div class="form-group" style="flex: 1;">
                          <label>CVV</label>
                          <input type="password" placeholder="123" value="123">
                        </div>
                      </div>
                    </div>
                    <div class="actions" style="margin-top: 10px;">
                      <button class="btn btn-pay" onclick="window.location.href='agrimitra://payment_result?status=success&orderId=${orderId}'">Bayar Sekarang</button>
                      <button class="btn btn-sec" onclick="showList()">Ubah Metode</button>
                    </div>
                  </div>
                </div>
              </div>

              <script>
                function showScreen(id) {
                  document.getElementById('screen-list').style.display = 'none';
                  var views = document.getElementsByClassName('details-view');
                  for (var i = 0; i < views.length; i++) {
                    views[i].style.display = 'none';
                  }
                  document.getElementById('screen-' + id).style.display = 'flex';
                }
                
                function showList() {
                  var views = document.getElementsByClassName('details-view');
                  for (var i = 0; i < views.length; i++) {
                    views[i].style.display = 'none';
                  }
                  document.getElementById('screen-list').style.display = 'block';
                }
              </script>
            </body>
            </html>
        """.trimIndent()
    }
}
