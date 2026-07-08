# NOTE.md — AgriMitra: B2B Marketplace Petani ↔ Pembeli (Pengepul/Hotel/Restoran/Dapur MBG)

> Panduan kerja Aplikasi Android Native (Kotlin) untuk tugas kelompok (MDP). Menghubungkan **Petani Lokal (Seller)** langsung dengan **Pembeli B2B** (Pengepul Sayur, Hotel, Restoran, dan Dapur Program MBG) tanpa perantara tengkulak, dibantu AI (Gemini) untuk rekomendasi harga & permintaan.

> **PENTING — platform**: dokumen ini untuk **Android Studio native (Kotlin + XML/Jetpack)**, BUKAN Flutter. Kriteria dosen kamu (ConstraintLayout, Fragment, Navigation Component + NavArgs, Room, ViewModel + LiveData, Data Binding, Jetpack Compose) adalah komponen Android native — kalau dikerjakan di Flutter, komponen-komponen itu tidak akan ada secara harfiah dan berisiko dianggap tidak memenuhi syarat.

---

## 0. Ringkasan Cepat (TL;DR)

| Kebutuhan | Solusi |
|---|---|
| Bahasa & IDE | Kotlin, Android Studio |
| Tema SDG | Goal 2 (Tanpa Kelaparan) + Goal 9 (Industri, Inovasi, Infrastruktur) |
| Layout | ConstraintLayout (semua fragment) + Jetpack Compose (Dashboard, untuk poin tambahan) |
| Arsitektur | MVVM + Repository Pattern (Local Room + Remote Retrofit) |
| Navigasi | Single Activity + Navigation Component, komunikasi antar fragment via **NavArgs (Safe Args)** |
| Local storage | Room Database (offline-first, penting karena sinyal lemah di daerah pertanian) |
| Networking | Retrofit + OkHttp (interceptor untuk logging & auth token) |
| AI | Gemini API (Context Injection / Prompt Engineering, tanpa training model sendiri) |
| Backend API | Buatan sendiri (Node.js/Express + MongoDB, atau Supabase) — di-hosting, bukan cuma mock lokal |
| Auth | JWT sederhana / Firebase Auth |
| Scanner | Google ML Kit (Barcode/QR Scanning) untuk traceability |
| Testing | JUnit + Mockito untuk ViewModel & Repository |
| Distribusi | Upload ke Play Store (internal testing track, boleh akun ISTTS) |
| Jumlah fragment minimum | 7 (lihat Bab 6) |

---

## 1. Project Overview

- **Nama Aplikasi**: AgriMitra
- **Tagline**: "Menghubungkan Petani Lokal Langsung ke Pembeli B2B — Pengepul, Hotel, Restoran, dan Dapur MBG"
- **Tema SDG**: Goal 2 (Zero Hunger) & Goal 9 (Industry, Innovation and Infrastructure)
- **Target & Indikator SDG** (wajib dicantumkan di proposal, contoh format dari dosen):
  - **Target 2.3**: *By 2030, double the agricultural productivity and incomes of small-scale food producers... including through secure and equal access to... markets...*
    **Indikator 2.3.2**: *Average income of small-scale food producers, by sex and indigenous status.*
  - **Target 9.3**: *Increase the access of small-scale industrial and other enterprises... to financial services... and their integration into value chains and markets.*
    **Indikator 9.3.2**: *Proportion of small-scale industries with a loan or line of credit* (adaptasi: proporsi petani skala kecil yang terintegrasi ke pasar digital langsung).
  - **Cara aplikasi mencapai ini**: memotong rantai tengkulak sehingga petani menjual dengan harga lebih adil, sekaligus membuka akses pasar B2B (pengepul/hotel/restoran/dapur MBG) yang sebelumnya sulit dijangkau petani kecil.
- **Roles**: `Petani` (Seller), `Pembeli` (Buyer — sub-tipe: Pengepul, Hotel/Restoran, Dapur MBG), `Admin` (opsional, untuk moderasi & verifikasi akun bisnis)
- **Anggota kelompok**: 3–4 orang (lihat Bab 9 pembagian tugas)

---

## 2. Tech Stack

| Kategori | Pilihan |
|---|---|
| Bahasa | Kotlin |
| Layout | ConstraintLayout (utama) + Jetpack Compose (dashboard/analytics, poin tambahan) |
| Arsitektur | MVVM + Repository Pattern |
| Local DB | Room (Entities, DAO, TypeConverters) |
| Networking | Retrofit2 + OkHttp3 (Interceptor untuk auth token & logging) |
| Navigasi | Navigation Component (Single Activity, NavGraph, Safe Args/NavArgs) |
| List | RecyclerView + ListAdapter (DiffUtil) + custom item layout per role |
| State & Binding | ViewModel + LiveData + Data Binding (poin tambahan, wajib di semua view kalau mau nilai penuh) |
| AI | Gemini API (google-generativeai / REST langsung via Retrofit) |
| Scanner | ML Kit Barcode Scanning (untuk QR traceability) |
| Background task | WorkManager (sinkronisasi offline→online, notifikasi) |
| Image loading | Glide/Coil |
| Testing | JUnit4 + Mockito + Coroutine Test (untuk ViewModel & Repository) |
| Backend (API sendiri) | Node.js + Express + MongoDB Atlas (atau Supabase kalau ingin lebih cepat) — WAJIB di-hosting (Render/Railway), bukan `localhost` saja |
| CI/CD (opsional) | GitHub Actions untuk build & test otomatis |

---

## 3. Peran & Fitur Utama (Two-Sided Marketplace)

### A. Petani (Seller Role)
1. **Smart Harvest Posting** — input hasil panen (komoditas, berat, kualitas/grade, foto) dengan cepat, tersimpan dulu di Room (offline), sync ke server saat online.
2. **Demand Discovery** — melihat daftar permintaan bahan dari Pembeli (Pengepul/Hotel/Restoran/Dapur MBG) secara real-time, bisa difilter jenis bahan & lokasi.
3. **AI Crop Planner** — rekomendasi masa tanam / komoditas apa yang harus difokuskan berdasarkan tren permintaan (Gemini API).
4. **Traceability Labelling** — generate QR Code unik per batch panen (siapa petani, tanggal panen, grade).
5. **Riwayat Transaksi & Rating** — melihat histori penjualan dan rating dari pembeli.

### B. Pembeli (Buyer Role — Pengepul/Hotel/Restoran/Dapur MBG)
1. **Requirement Posting** — mengunggah kebutuhan bahan harian/mingguan (jenis, jumlah, budget, tenggat).
2. **Supplier Matchmaking** — mencari petani terdekat (radius GPS) berdasarkan stok & histori kualitas.
3. **Secure Procurement** — transaksi digital dengan riwayat transparan (bukti audit).
4. **AI Budget Advisor** — analisis penghematan anggaran berdasarkan fluktuasi harga pasar (Gemini API).
5. **Verifikasi Logistik (Scan QR)** — scan QR saat barang datang untuk verifikasi asal-usul (ML Kit).

### C. Admin (opsional, kalau butuh fragment/fitur tambahan)
- Verifikasi akun bisnis Pembeli (agar hanya bisnis terverifikasi yang bisa posting demand besar).
- Moderasi laporan/dispute transaksi.

> **Catatan kompleksitas fitur**: dosen memberi +5 poin per fitur yang **bukan** sekadar insert/update/delete/select biasa. Fitur yang sudah "otomatis lolos" kategori ini: AI Crop Planner, AI Budget Advisor, Supplier Matchmaking (radius GPS + scoring), Traceability QR generate & verify, Rating otomatis berbasis riwayat transaksi. Pastikan minimal 3-4 fitur seperti ini benar-benar diimplementasikan (bukan cuma UI dummy).

---

## 4. Implementasi AI (Gemini — Decision Support System)

- **Prinsip**: tanpa training model sendiri, pakai **Context Injection / Prompt Engineering**. App mengirim cuplikan data (stok, harga, histori permintaan dari Room/server) ke Gemini API, lalu menampilkan hasilnya di UI.
- **Alur teknis**:
  1. ViewModel mengambil data relevan dari Repository (Room + Retrofit).
  2. Data diringkas jadi teks/JSON kecil, disisipkan ke prompt.
  3. Kirim ke Gemini API via Retrofit (atau SDK resmi kalau tersedia untuk Kotlin).
  4. Response Gemini di-parse, ditampilkan di Fragment AI Smart Hub.
- **Contoh prompt (AI Crop Planner)**:
  > "Berikut data permintaan bahan 3 bulan terakhir: {data JSON}. Petani menanam: {komoditas saat ini}. Beri rekomendasi singkat komoditas apa yang sebaiknya difokuskan bulan depan, dan alasan berbasis data."
- **Contoh prompt (AI Budget Advisor)**:
  > "Berikut daftar harga penawaran dari beberapa petani untuk komoditas {X}: {data JSON}. Harga rata-rata pasar: {Y}. Rekomendasikan penawaran mana yang paling efisien mempertimbangkan harga dan kualitas (grade)."
- **Penting**: simpan API key Gemini di `local.properties` / `BuildConfig`, JANGAN hardcode di kode / commit ke GitHub publik.

---

## 5. Rincian Teknis Database (Room) & API Eksternal (Retrofit)

### 5.1 Room — Local Storage (Offline-first)

| Entity | Fungsi |
|---|---|
| `UserEntity` | Sesi user, role (Petani/Pembeli/Admin), token |
| `HarvestEntity` | Cache hasil panen milik petani (sinkron ke server) |
| `DemandEntity` | Cache permintaan bahan dari Pembeli |
| `TransactionEntity` | Riwayat transaksi lokal (untuk laporan offline & audit) |
| `BatchEntity` | Data batch panen untuk traceability (kode QR, tanggal, grade) |

- Gunakan **DAO** dengan `suspend fun` (Coroutines) untuk operasi async.
- `Repository` menggabungkan Room (sumber offline) + Retrofit (sumber online) — **Single Source of Truth** pattern: Room selalu jadi sumber yang ditampilkan ke UI (via `Flow`/`LiveData`), Retrofit hanya untuk sinkronisasi ke server.

### 5.2 Retrofit — API Eksternal (API buatan sendiri, wajib di-hosting)

```
GET  /api/v1/marketplace/products      -> daftar hasil panen tersedia (dengan pagination & filter)
GET  /api/v1/marketplace/demands       -> daftar kebutuhan bahan dari Pembeli
POST /api/v1/transactions              -> submit transaksi (idPetani, idProduk, qty, totalHarga)
GET  /api/v1/analytics/prices          -> harga referensi pasar terkini
POST /api/v1/ai/crop-planner           -> proxy ke Gemini (kalau API key disimpan di backend, lebih aman)
POST /api/v1/ai/budget-advisor         -> proxy ke Gemini
POST /api/v1/auth/login                -> login, return JWT
POST /api/v1/auth/register             -> registrasi (role Petani/Pembeli)
GET  /api/v1/batches/:qrCode           -> verifikasi asal-usul batch saat scan QR
```

> Rekomendasi: taruh API key Gemini di **backend** (bukan di app), lalu app cukup call endpoint `/ai/...` milik sendiri. Ini juga menambah poin "hosting web service dan DB" (Bab 8) karena backend jadi komponen nyata, bukan cuma passthrough.

---

## 6. Daftar Fragment (Minimal 7 — Wajib)

| # | Fragment | Fungsi | ViewModel | NavArgs masuk/keluar |
|---|---|---|---|---|
| 1 | `LoginFragment` | Login/Register, pilih role | `AuthViewModel` | keluar: `userId`, `role` |
| 2 | `DashboardPetaniFragment` | Overview stok, notifikasi, shortcut fitur | `DashboardViewModel` | - |
| 3 | `DashboardPembeliFragment` | Overview demand, transaksi terakhir | `DashboardViewModel` | - |
| 4 | `SmartHarvestPostingFragment` | Form input hasil panen + generate QR | `HarvestViewModel` | keluar: `batchId` |
| 5 | `DemandDiscoveryFragment` | List kebutuhan bahan (RecyclerView) | `DemandViewModel` | keluar: `demandId` → detail |
| 6 | `DemandDetailFragment` | Detail 1 kebutuhan + tombol ajukan penawaran | `DemandViewModel` | masuk: `demandId` |
| 7 | `SupplierMatchmakingFragment` | List petani terdekat (RecyclerView + filter) | `MatchmakingViewModel` | keluar: `petaniId` |
| 8 | `AiSmartHubFragment` | AI Crop Planner / Budget Advisor | `AiViewModel` | masuk: `mode` (planner/advisor) |
| 9 | `TransactionHistoryFragment` | Riwayat transaksi (RecyclerView) | `TransactionViewModel` | keluar: `transactionId` |
| 10 | `QrScanFragment` | Scan QR verifikasi batch (ML Kit) | `TraceabilityViewModel` | keluar: `batchId` → detail |
| 11 | `BatchDetailFragment` | Detail asal-usul batch (nama petani, tanggal panen, grade) | `TraceabilityViewModel` | masuk: `batchId` |
| 12 | `ProfileFragment` | Profil user, rating, logout | `ProfileViewModel` | - |

> Sudah lebih dari 7 (syarat minimal), jadi kalau waktu terbatas, fragment #6, #11 bisa digabung ke fragment induknya (misal detail ditampilkan sebagai BottomSheet) — tetap hitung sebagai navigasi antar fragment via NavArgs asal datanya benar-benar dikirim lewat Safe Args, bukan lewat variabel global/static.

### Navigation Graph
- Gunakan 1 `nav_graph.xml`, root `LoginFragment`, lalu percabangan `action_login_to_dashboardPetani` / `action_login_to_dashboardPembeli` berdasarkan `role` yang dikirim via NavArgs.
- Gunakan `<argument>` di setiap `<fragment>` node untuk NavArgs (`app:argType="string"` dsb), bukan Bundle manual — supaya kelihatan jelas pakai Safe Args di code review dosen.

---

## 7. ViewModel + LiveData + Repository Pattern

- **Satu ViewModel per Fragment** (atau di-share antar 2 fragment terkait, misal `DemandViewModel` dipakai `DemandDiscoveryFragment` & `DemandDetailFragment` lewat `navGraphViewModels`).
- Setiap ViewModel HANYA berbicara dengan **Repository**, tidak langsung ke Room DAO / Retrofit Service.
- Contoh alur (Repository Pattern):

```kotlin
class HarvestRepository(
    private val dao: HarvestDao,
    private val api: MarketplaceApiService
) {
    fun getHarvests(): LiveData<List<HarvestEntity>> = dao.getAllHarvests()

    suspend fun syncHarvests() {
        try {
            val remote = api.getProducts()
            dao.insertAll(remote.map { it.toEntity() })
        } catch (e: Exception) {
            // offline-first: kalau gagal, tetap pakai data lokal, log error
        }
    }

    suspend fun postHarvest(harvest: HarvestEntity) {
        dao.insert(harvest) // simpan lokal dulu (offline-first)
        try {
            api.postProduct(harvest.toDto())
            dao.markSynced(harvest.id)
        } catch (e: Exception) {
            // akan disinkronkan lagi lewat WorkManager saat online
        }
    }
}
```

- Gunakan **Data Binding** di setiap layout XML (`<layout>` root tag) supaya LiveData bisa langsung di-observe di XML (`android:text="@{viewModel.harvestName}"`) — ini yang dinilai +15 poin kalau konsisten di SEMUA view, jadi jangan campur (sebagian pakai `findViewById` manual, sebagian Data Binding).

---

## 8. Poin Kompleksitas Tambahan — Checklist

| Item | Poin | Cara memenuhi |
|---|---|---|
| Repository Pattern (Local + Remote benar) | 15 | Bab 7 — pastikan SEMUA fitur (bukan cuma 1-2) pakai pola ini |
| Data Binding + LiveData untuk SEMUA view | 15 | Bab 7 — konsisten di semua fragment, tidak boleh ada yang manual `findViewById` |
| Hosting web service + DB | 10 | Backend di Render/Railway + MongoDB Atlas, bukan localhost |
| Fitur non-CRUD biasa (@5/fitur) | ~20-25 | AI Crop Planner, AI Budget Advisor, Matchmaking radius GPS, Traceability QR, Rating otomatis |
| Desain UI/UX baik | 0-15 | Ikuti panduan Material Design 3, konsisten warna/tipografi, uji alur dengan user lain |
| Unit Testing (ViewModel/Repository) | 10 | Bab 10 |
| Jetpack Compose | 10 | Minimal Dashboard/Analytics pakai Compose (boleh campur dengan XML fragment lain) |
| Upload ke Play Store | 10 | Internal testing track, akun ISTTS boleh |
| **Total potensi tambahan** | **~95-100** | Realistis kejar minimal 60-70 dari total ini |

---

## 9. Pembagian Tugas Kelompok (4 Orang)

| Peran | Anggota | Tanggung Jawab Teknis |
|---|---|---|
| Interface Lead | Mahasiswa A | UI/UX (Figma referensi Behance yang sudah dikumpulkan), ConstraintLayout semua fragment, Jetpack Compose Dashboard, integrasi ML Kit Scanner |
| API Integrator | Mahasiswa B | Setup Retrofit + OkHttp Interceptor, hosting backend (Node/Express + MongoDB Atlas atau Supabase), error handling API, dokumentasi endpoint |
| System Architect | Mahasiswa C | Room (Entity + DAO), Repository Pattern (sync local-remote), WorkManager, Navigation Component + Safe Args |
| Logic & AI Master | Mahasiswa D | ViewModel + LiveData semua fragment, integrasi Gemini API + prompt engineering, Data Binding, Unit Testing |

> Kalau kelompok hanya 3 orang: gabungkan "Interface Lead" dan "API Integrator" jadi satu, atau bagi ulang sesuai kekuatan masing-masing — yang penting setiap orang minimal pegang 3-4 endpoint/fitur sendiri (syarat pembagian tugas dosen).

---

## 10. Unit Testing

- Fokus ke **ViewModel** dan **Repository**, bukan UI (UI testing opsional/nice-to-have).
- Gunakan `kotlinx-coroutines-test` untuk suspend function, `Mockito` untuk mock DAO/API Service.
- Contoh target test:
  - `HarvestViewModelTest`: memastikan LiveData berubah saat `postHarvest()` sukses/gagal.
  - `HarvestRepositoryTest`: memastikan data tetap tersimpan di Room walau API call gagal (offline-first behavior).
  - `AiViewModelTest`: mock response Gemini API, pastikan parsing & error handling benar.

---

## 11. Roadmap Pengerjaan (Timeline)

1. **Hari 1-2**: Setup project, buat backend skeleton (Express/Supabase) + hosting awal, setup Room entities, setup Retrofit service interface.
2. **Hari 3-4**: Login/Register + role selection, Dashboard Petani & Pembeli, Navigation Graph dasar (7+ fragment kosong dulu).
3. **Hari 5-6**: Smart Harvest Posting + Demand Discovery/Detail (RecyclerView, Repository Pattern, Data Binding).
4. **Hari 7-8**: Supplier Matchmaking (GPS radius) + Secure Procurement (transaksi digital).
5. **Hari 9-10**: Integrasi Gemini API (AI Crop Planner & Budget Advisor) — prompt engineering & context injection.
6. **Hari 11-12**: Traceability (QR generate + ML Kit scan) + verifikasi batch.
7. **Hari 13**: WorkManager untuk sync offline→online + notifikasi.
8. **Hari 14**: Unit Testing (ViewModel/Repository).
9. **Hari 15**: Jetpack Compose untuk Dashboard/Analytics.
10. **Hari 16**: Polish UI/UX, review checklist Bab 8, siapkan demo untuk dosen.
11. **Hari 17**: Upload ke Play Store (internal testing) + submit proposal & source code.

---

## 12bis. Referensi Desain UI/UX & Design System (Behance)

> Referensi visual: [Farm products delivery APP | UX/UI design](https://www.behance.net/gallery/246793059/Farm-products-delivery-APP-UXUI-design). Style ini dipakai sebagai acuan tone visual "farm-to-table" yang bersih & hangat — cocok untuk tema SDG 2/9. Karena referensinya app **B2C** (konsumen beli langsung), pola UI-nya perlu diadaptasi ke konteks **B2B** AgriMitra (lihat mapping komponen di bawah), bukan ditiru mentah-mentah.

### 12bis.1 Color Palette (dari style guide referensi)

| Kelompok | Hex | Penggunaan di AgriMitra |
|---|---|---|
| **Netral hangat (background/surface)** | `#F5F3EE`, `#F0EDE9`, `#E6E3DD`, `#DDD8D2`, `#D3CEC6`, `#C9C3BB` | Background layar (`#F5F3EE`), surface card (`#F0EDE9`/putih), divider/border (`#D3CEC6`) |
| **Hijau sage (brand/primary)** | `#E8E8E2`, `#D6D9D0`, `#B2B9AC`, `#8F9A88`, `#6B7A64`, **`#475B40`** (primary utama) | Primary button, top bar/tab aktif, ikon terpilih, badge "Verified" traceability |
| **Teks utama** | `#333333` | Judul, body text di atas background terang |
| **Aksen terracotta/oranye** | **`#BE7C4A`** (aksen utama), `#D6AE8E`, `#E2C6B0`, `#EEDFD2`, `#F4ECE3` | Tombol CTA sekunder (mis. "Ajukan Penawaran", info tanggal/jadwal), badge promo/highlight, warna hangat untuk elemen yang perlu menonjol tapi bukan aksi utama |
| **Olive/khaki (chip & tag)** | `#EEEDE3`, `#E2E2D2`, `#CACCB1`, `#B2B78F`, `#9AA16E`, `#828B4C` | Filter chip (kategori komoditas, status transaksi), tag pada card (mis. "Sayur", "Grade A") |

- Definisikan semua warna ini di `res/values/colors.xml` dengan nama semantik, bukan nama hex, contoh: `color_primary_sage`, `color_accent_terracotta`, `color_surface_warm`, `color_text_primary`, dst. Ini juga berguna kalau nanti mau tambah Dark Mode.
- Untuk Jetpack Compose (Dashboard), definisikan `Color.kt` + `Theme.kt` (MaterialTheme custom) memakai palet yang sama supaya XML & Compose konsisten.

### 12bis.2 Typography

| Style | Font (referensi) | Size (referensi) | Padanan Android |
|---|---|---|---|
| Title / Headline | SF UI Display (bold) | 28 / 24 / 22 sp | `Roboto` bold / font custom serupa (mis. **Poppins/Inter SemiBold**, karena SF UI Display eksklusif iOS) |
| Subtitle, Body, Button, Caption, Footnote | SF Pro Text | 17 / 15 / 14 / 13 / 12 / 10 sp | `Roboto` reguler / **Inter Regular** |

> Catatan: SF UI Display & SF Pro Text adalah font Apple/iOS dan tidak bisa dipakai langsung di Android (lisensi & rendering system font berbeda). Gunakan font Google Fonts yang visualnya senada — rekomendasi: **Inter** atau **Poppins** (geometris, bersih, cocok dengan tone "farm-to-table" minimalis). Tambahkan sebagai `res/font/` (downloadable font atau bundled `.ttf`) lalu set di `styles.xml`/`Theme.kt`.

### 12bis.3 Pola Layar Referensi → Mapping ke Fragment AgriMitra

| Pola di referensi Behance | Adaptasi B2B di AgriMitra | Fragment terkait (Bab 6) |
|---|---|---|
| Splash/onboarding: foto hero full-bleed (ladang/ternak) + logo + tagline di bawah | Splash screen AgriMitra: foto sawah/kebun + logo + tagline "Menghubungkan Petani Lokal Langsung ke Pembeli B2B" | (Splash Activity, sebelum masuk `LoginFragment`) |
| Login dengan nomor telepon + OTP, ada tombol "Hanya ingin lihat-lihat" (guest mode) | Login/Register dengan pilihan role (Petani/Pembeli) setelah OTP; guest mode untuk Pembeli baru yang mau lihat katalog dulu sebelum daftar bisnisnya diverifikasi | `LoginFragment` |
| Top tab segmented: **Produk / Fermer / Resep** + search bar + filter icon | Untuk role **Pembeli**: tab **Produk / Petani / Permintaan Saya**. Untuk role **Petani**: tab **Panen Saya / Permintaan Pembeli / AI Hub** | `DashboardPembeliFragment`, `DashboardPetaniFragment` |
| Grid kategori (foto + label): Yaitu Sayur, Buah, Daging, dst | Grid kategori komoditas (Sayur, Buah, Umbi, Rempah, dst) sebagai entry point ke `DemandDiscoveryFragment` / listing produk | `DemandDiscoveryFragment` |
| Filter chip (mis. "Semua/Molokoo/Sметана/Сливки") + sort "By rating" | Filter chip kategori komoditas + lokasi; sort by rating **atau** by jarak (radius GPS) — relevan untuk B2B karena jarak logistik penting | `DemandDiscoveryFragment`, `SupplierMatchmakingFragment` |
| Card list: foto produk, badge rating pojok kiri atas, ikon favorit, nama, sub-info (nama toko/farm + jarak), harga, tombol/qty stepper | Card penawaran: foto batch panen, badge rating petani, nama komoditas, sub-info (nama petani + jarak GPS), harga/kg, tombol "Ajukan Penawaran" — **qty stepper** relevan untuk transaksi partai besar (kg/ton, bukan pcs) | Custom `RecyclerView.Adapter` item dipakai ulang di `DemandDiscoveryFragment`, `SupplierMatchmakingFragment`, `TransactionHistoryFragment` |
| Tombol info "tanggal pengiriman terdekat" (highlight warna terracotta) | Tombol info "tanggal panen berikutnya tersedia" / estimasi ketersediaan stok (pakai warna aksen `#BE7C4A` supaya konsisten sebagai info-highlight, bukan aksi utama) | `SmartHarvestPostingFragment`, `DemandDetailFragment` |
| Bottom navigation 4 ikon: Home / Katalog / Favorit / Profil | Bottom nav disesuaikan per role: **Petani** → Beranda/Panen/AI Hub/Profil; **Pembeli** → Beranda/Katalog/Permintaan/Profil | Root `NavGraph` + `BottomNavigationView` |
| Card daftar petani/produsen (foto profil, nama usaha, lokasi, jarak, tag komoditas) | Dipakai persis untuk `SupplierMatchmakingFragment`: card petani dengan foto, nama, lokasi, jarak GPS, tag komoditas yang dijual, rating | `SupplierMatchmakingFragment` |

### 12bis.4 Catatan Implementasi Teknis

- Semua card pakai `MaterialCardView` dengan `app:cardCornerRadius="16dp"` dan `app:cardElevation` kecil (2-4dp) supaya terasa "soft" seperti referensi, bukan flat.
- Filter chip pakai `com.google.android.material.chip.ChipGroup` + `Chip` (Material Components), warna background sesuai palet olive/khaki di atas, teks warna `#333333` atau putih tergantung kontras.
- Top segmented tab (Produk/Petani/Permintaan) pakai `TabLayout` (mode `fixed`) yang disinkronkan dengan `ViewPager2` **atau**, kalau ingin tetap 1 NavGraph, cukup pakai `TabLayout` untuk switch visibility antar 3 Fragment (lebih simpel untuk kebutuhan tugas ini).
- Badge rating (bintang + angka) & badge "Verified" (traceability) dibuat sebagai `custom view`/`include layout` kecil yang reusable, dipanggil di banyak card — ini juga nilai plus dari sisi "kerapian kode" saat demo ke dosen.
- Tetap pakai **ConstraintLayout** sebagai root semua Fragment (syarat wajib dosen) — card/chip di atas ditempel sebagai children di dalam ConstraintLayout tersebut, bukan mengganti root-nya jadi LinearLayout/RelativeLayout.
- Simpan semua warna/dimens/type scale di `colors.xml`, `dimens.xml`, dan `styles.xml`/`themes.xml` (jangan hardcode hex/size di layout XML satu-satu) — memudahkan kalau palet direvisi nanti.

---

## 13. Skenario Demo untuk Dosen

> "Saya masuk sebagai Petani. Saya input hasil panen 500kg cabai, sistem generate QR Code untuk batch ini. Kemudian sebagai Pembeli (Hotel/Restoran), saya buka AI Smart Hub dan Gemini merekomendasikan batch saya sebagai yang paling efisien (harga vs kualitas). Saya tekan beli, transaksi terkirim ke server. Saat barang sampai, saya scan QR-nya dan sistem menampilkan asal-usul (nama petani, tanggal panen, grade) — status berubah jadi 'Verified'."

---

## 14. Checklist Akhir Sebelum Submit

- [ ] Semua 7+ fragment berfungsi dan terhubung lewat Navigation Component + Safe Args
- [ ] Semua fragment pakai ConstraintLayout
- [ ] Room Database berjalan offline (matikan wifi, test input data tetap tersimpan)
- [ ] Retrofit terhubung ke backend yang SUDAH di-hosting (bukan localhost)
- [ ] Repository Pattern konsisten di semua fitur utama
- [ ] Data Binding + LiveData dipakai di semua layout, tidak ada `findViewById` manual tersisa
- [ ] Minimal 3-4 fitur "non-CRUD biasa" benar-benar berfungsi (bukan dummy)
- [ ] Unit test ViewModel/Repository jalan (`./gradlew test`)
- [ ] Minimal 1 layar pakai Jetpack Compose
- [ ] App sudah di-upload ke Play Store (internal testing) dan link tersedia
- [ ] Proposal mencantumkan target & indikator SDG secara spesifik (bukan cuma nomor goal)
- [ ] Palet warna & tipografi (Bab 12bis) sudah didefinisikan di `colors.xml`/`styles.xml`, dipakai konsisten di semua layout (tidak ada hex/size hardcoded acak)
- [ ] Card, chip, dan bottom nav sudah mengikuti pola referensi Behance yang sudah diadaptasi ke konteks B2B (bukan copy 1:1 tampilan B2C)
- [ ] README/proposal siap dipamerkan di LinkedIn (screenshot fitur + tech stack)