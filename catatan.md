# 📝 Catatan Panduan Sidang MDP - AgriMitra

Berkas ini berisi rangkuman kriteria tugas besar pemrograman mobile (MDP) yang telah sukses diimplementasikan pada aplikasi **AgriMitra** beserta lokasi foldernya di dalam basis kode Anda. Gunakan catatan ini sebagai panduan cepat saat mempresentasikan aplikasi dihadapan dosen penguji.

---

## 🏛️ 1. Arsitektur MVVM & Repository Pattern
Semua fitur dipisahkan secara ketat berdasarkan tanggung jawabnya untuk memastikan kode terstruktur dan mudah diuji.
*   **Domain Models (Model Data)**:
    *   `app/src/main/java/com/agroSystem/app/data/models/`
    *   Berkas utama: `User.kt`, `Product.kt`, `Farmer.kt`, `ChatMessage.kt`.
*   **Layer Data (Local Database & Remote API)**:
    *   `app/src/main/java/com/agroSystem/app/data/local/` (Room Database)
    *   `app/src/main/java/com/agroSystem/app/data/remote/` (Retrofit API Service)
*   **Layer Repository (Penyedia Data)**:
    *   `app/src/main/java/com/agroSystem/app/data/repository/`
    *   Berkas utama: `AuthRepository.kt`.
*   **Layer ViewModel (Logika Bisnis UI)**:
    *   `app/src/main/java/com/agroSystem/app/features/auth/AuthViewModel.kt`
    *   `app/src/main/java/com/agroSystem/app/features/shared/MainSharedViewModel.kt` (ViewModel bersama untuk pertukaran data antar fragment).

---

## 💾 2. Offline-First Caching (Room Database)
Menyimpan sesi login pengguna secara lokal di perangkat Android agar tidak perlu login ulang saat aplikasi dibuka kembali.
*   **Database Config**: `app/src/main/java/com/agroSystem/app/data/local/AppDatabase.kt`
*   **Entity (Struktur Tabel)**: `app/src/main/java/com/agroSystem/app/data/local/entities/UserEntity.kt`
*   **DAO (Data Access Object / Query)**: `app/src/main/java/com/agroSystem/app/data/local/dao/UserDao.kt`

---

## 🔌 3. Koneksi REST API & Server (Retrofit)
Menghubungkan aplikasi Android dengan database cloud secara online.
*   **Retrofit Client & Base URL**: `app/src/main/java/com/agroSystem/app/data/remote/ApiClient.kt`
*   **API Interface Endpoint**: `app/src/main/java/com/agroSystem/app/data/remote/AuthApiService.kt`

---

## 🔑 4. Multi-Authentication & Google Sign-In
*   **Pendaftaran & Masuk Biasa**: Melalui Firebase Authentication (E-mail & Password).
*   **Google Sign-In**: Menggunakan **Google Credential Manager API** terbaru untuk otentikasi sekali sentuh.
*   **Lokasi Kode**:
    *   `app/src/main/java/com/agroSystem/app/features/auth/`
    *   Berkas utama: `PhoneInputFragment.kt`, `OtpInputFragment.kt`, `ProfileSetupFragment.kt`.

---

## 🗺️ 5. Google Maps / OpenStreetMap & Runtime Permission
*   **Peta GPS OSM**: Pengguna bisa memilih koordinat alamat pengiriman di peta OpenStreetMap.
*   **Runtime Permission**: Izin lokasi GPS Android (`ACCESS_FINE_LOCATION`).
*   **Lokasi Kode**:
    *   `app/src/main/java/com/agroSystem/app/features/address/`
    *   Berkas utama: `SelectAddressFragment.kt`, `LocationPermissionFragment.kt`, `AddressDetailFragment.kt`.
    *   Layout: `app/src/main/res/layout/fragment_select_address.xml`.

---

## 💳 6. Integrasi Midtrans Payment Gateway
*   **Lanjutkan Pembayaran**: Menggunakan Snap Redirection Web View untuk melakukan pembayaran riil di sandbox Midtrans.
*   **Lokasi Kode**:
    *   `app/src/main/java/com/agroSystem/app/features/payment/PaymentWebViewFragment.kt`
    *   Layout: `app/src/main/res/layout/fragment_payment_webview.xml`.

---

## 🛒 7. Alur Transaksi Lengkap B2B E-Commerce (Buyer & Seller)
*   **Fitur Pembeli (Buyer)**:
    *   **Keranjang Belanja**: Pengelompokan produk secara otomatis berdasarkan nama petani mitra tani.
    *   **Struk Detail Pesanan**: `OrderDetailBottomSheetFragment.kt`.
    *   **Konfirmasi Penerimaan**: Pembeli mengonfirmasi status barang sudah sampai (`completed`).
    *   *Lokasi Folder*: `app/src/main/java/com/agroSystem/app/features/cart/` & `com/agroSystem/app/features/payment/`.
*   **Fitur Penjual (Seller)**:
    *   **Daftar Produk Jualan & Riwayat Pesanan Masuk**: Dibagi menggunakan TabLayout di panel kelola.
    *   **Form Tambah/Ubah Produk**: Form input lengkap dengan upload foto galeri Base64 dan validasi.
    *   **Konfirmasi Pengiriman**: Penjual mengubah status pesanan masuk menjadi sedang dikirim (`shipped`).
    *   *Lokasi Folder*: `app/src/main/java/com/agroSystem/app/features/seller/`.

---

## 🤖 8. Asisten Chat AI (Google Gemini 3.5 Flash)
*   **Tombol Melayang Chat (FAB)**: Terintegrasi di halaman utama.
*   **Gelembung Chat AI**: `item_chat_bubble.xml` & `ChatAdapter.kt`.
*   **Rekomendasi Produk Cerdas**: Jika pengguna chat mencari produk jualan (contoh: *"telur"*), chatbot akan menampilkan daftar kartu produk horizontal yang bisa diklik untuk membuka detail belanjaan.
*   *Lokasi Folder*: `app/src/main/java/com/agroSystem/app/features/chat/`.

---

## 🧪 9. Unit Testing Otomatis
*   **Lokasi Kode**: `app/src/test/java/com/agroSystem/app/AuthRepositoryTest.kt`
*   **Fungsi**: Menguji fungsi otentikasi pendaftaran, login, dan penyimpanan lokal di layer Repository secara otomatis.

---

## 🖥️ 10. Node.js Express Backend & Web Hosting Cloud
*   **Lokasi Kode**: Folder `/backend/` di root proyek Anda.
*   **Berkas Utama**: `server.js` (Express REST APIs), `.env` (Midtrans & Gemini config).
*   **Hosting**: Dideploy Live menggunakan **Render.com** (`https://smart-supply-chain-app.onrender.com`).

---

## 🔌 11. Integrasi Pihak Ketiga (3rd Party APIs)
Aplikasi Anda menggunakan 6 integrasi pihak ketiga berikut untuk menambahkan fitur-fitur premium:

1.  **Google Gemini AI API (gemini-3.5-flash)**
    *   *Fungsi*: Mesin kecerdasan buatan (Generative AI) yang digunakan untuk menjawab chat dari pengguna seputar cara bertani, resep masakan, dan kandungan nutrisi sayuran di ruang obrolan Asisten Tani AI.
2.  **Midtrans Payment Gateway**
    *   *Fungsi*: Gerbang pembayaran online yang digunakan untuk memproses transaksi e-commerce secara otomatis (Snap Redirection), mendukung metode pembayaran transfer bank, gopay, shopeepay, kartu kredit, dll di lingkungan Sandbox.
3.  **OpenStreetMap (OSMDroid API)**
    *   *Fungsi*: Layanan pemetaan alternatif Google Maps yang gratis dan open-source untuk me-render peta interaktif, pin GPS koordinat lokasi, dan mengambil nama jalan (Reverse Geocoding) secara langsung di halaman pilih alamat.
4.  **Google Identity / Credential Manager API**
    *   *Fungsi*: Layanan otentikasi dari Google untuk masuk ke dalam aplikasi menggunakan akun Google yang terdaftar di handphone Android secara instan dengan satu kali ketukan (One-Tap Sign-In).
5.  **Firebase Authentication**
    *   *Fungsi*: Layanan dari Google Cloud Platform (GCP) yang mengelola database pendaftaran pengguna baru, masuk akun menggunakan E-mail & Sandi, dan verifikasi nomor telepon.
6.  **Spoonacular Food & Groceries API**
    *   *Fungsi*: API data makanan yang digunakan oleh server backend pada saat proses seeding untuk memperkaya info produk pangan (detail kandungan nutrisi, saran penyimpanan, dan masa kadaluarsa sayuran segar).
