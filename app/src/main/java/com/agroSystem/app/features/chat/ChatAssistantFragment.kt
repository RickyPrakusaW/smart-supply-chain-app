package com.agroSystem.app.features.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.agroSystem.app.R
import com.agroSystem.app.data.models.ChatMessage
import com.agroSystem.app.features.shared.MainSharedViewModel
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import com.agroSystem.app.features.catalog.EdamamResponse
import com.agroSystem.app.features.catalog.EdamamHint

class ChatAssistantFragment : Fragment() {

    private val sharedViewModel: MainSharedViewModel by activityViewModels()

    private lateinit var btnBack: MaterialCardView
    private lateinit var rvChatMessages: RecyclerView
    private lateinit var layoutTypingIndicator: View
    private lateinit var inputChatMessage: EditText
    private lateinit var btnSendChat: MaterialCardView

    private lateinit var chatAdapter: ChatAdapter
    private val messagesList = mutableListOf<ChatMessage>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chat_assistant, container, false)

        btnBack = view.findViewById(R.id.btn_back)
        rvChatMessages = view.findViewById(R.id.rv_chat_messages)
        layoutTypingIndicator = view.findViewById(R.id.layout_typing_indicator)
        inputChatMessage = view.findViewById(R.id.input_chat_message)
        btnSendChat = view.findViewById(R.id.btn_send_chat)

        setupRecyclerView()
        setupListeners()
        
        // Add initial greeting if list is empty
        if (messagesList.isEmpty()) {
            addAiMessage("Halo! Saya adalah Asisten Tani AI untuk AgriMitra. Ada yang bisa saya bantu hari ini mengenai pertanian, nutrisi sayuran, resep makanan, atau informasi budidaya tani?")
        }

        return view
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter(messagesList) { product ->
            navigateToProductDetail(product)
        }
        val layoutManager = LinearLayoutManager(requireContext())
        layoutManager.stackFromEnd = true // scroll to bottom as content grows
        rvChatMessages.layoutManager = layoutManager
        rvChatMessages.adapter = chatAdapter
    }

    private fun navigateToProductDetail(product: com.agroSystem.app.data.models.Product) {
        val bundle = androidx.core.os.bundleOf("productId" to product.id)
        findNavController().navigate(R.id.action_chatAssistantFragment_to_productDetailFragment, bundle)
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        btnSendChat.setOnClickListener {
            sendMessage()
        }
    }

    private fun sendMessage() {
        val text = inputChatMessage.text.toString().trim()
        if (text.isEmpty()) return

        // 1. Add User Message
        addUserMessage(text)
        inputChatMessage.setText("")

        // 2. Show Typing State
        layoutTypingIndicator.visibility = View.VISIBLE
        scrollToBottom()

        // Check if query is related to food recommendations from Edamam API
        val lowerText = text.lowercase()
        val foodQuery = getEnglishFoodQuery(lowerText)
        if (foodQuery != null) {
            fetchEdamamRecommendations(text, foodQuery)
            return
        }

        // 3. Request AI Response from Gemini directly via HTTP client
        val client = okhttp3.OkHttpClient()
        val requestBodyJson = """
            {
              "contents": [
                {
                  "parts": [
                    {
                      "text": ${com.google.gson.Gson().toJson(text)}
                    }
                  ]
                }
              ]
            }
        """.trimIndent()

        val requestBody = requestBodyJson.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

        // API Key placeholder - if empty, will gracefully execute the local smart fallback chatbot
        val geminiApiKey = "AIzaSyBH8qg5yoZDcVe0_dbrAFPLVnFzAPv5GWs"
        val request = okhttp3.Request.Builder()
            .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$geminiApiKey")
            .post(requestBody)
            .build()

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                if (geminiApiKey.isEmpty()) {
                    throw Exception("API Key is empty, using fallback")
                }
                
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                withContext(Dispatchers.Main) {
                    layoutTypingIndicator.visibility = View.GONE
                    if (response.isSuccessful && !responseBody.isNullOrEmpty()) {
                        @Suppress("UNCHECKED_CAST")
                        val responseMap = com.google.gson.Gson().fromJson(responseBody, Map::class.java) as? Map<String, Any>
                        @Suppress("UNCHECKED_CAST")
                        val candidates = responseMap?.get("candidates") as? List<Map<String, Any>>
                        val firstCandidate = candidates?.firstOrNull()
                        @Suppress("UNCHECKED_CAST")
                        val content = firstCandidate?.get("content") as? Map<String, Any>
                        @Suppress("UNCHECKED_CAST")
                        val parts = content?.get("parts") as? List<Map<String, Any>>
                        val firstPart = parts?.firstOrNull()
                        val reply = firstPart?.get("text") as? String ?: "Maaf, Asisten Tani tidak memahami respon tersebut."

                        val recommended = sharedViewModel.productsList.value?.filter { product ->
                            reply.contains(product.name, ignoreCase = true) || reply.contains(product.category, ignoreCase = true)
                        }?.take(3)

                        addAiMessage(reply, recommended)
                    } else {
                        val localReply = getSmartLocalReply(text)
                        val recommended = sharedViewModel.productsList.value?.filter { product ->
                            localReply.contains(product.name, ignoreCase = true) || localReply.contains(product.category, ignoreCase = true)
                        }?.take(3)
                        addAiMessage(localReply, recommended)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    layoutTypingIndicator.visibility = View.GONE
                    val localReply = getSmartLocalReply(text)
                    val recommended = sharedViewModel.productsList.value?.filter { product ->
                        localReply.contains(product.name, ignoreCase = true) || localReply.contains(product.category, ignoreCase = true)
                    }?.take(3)
                    addAiMessage(localReply, recommended)
                }
            }
        }
    }

    private fun fetchEdamamRecommendations(userQuery: String, englishQuery: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val client = okhttp3.OkHttpClient()
                val url = "https://edamam-food-and-grocery-database.p.rapidapi.com/api/food-database/v2/parser?ingr=" + java.net.URLEncoder.encode(englishQuery, "UTF-8")
                
                val request = okhttp3.Request.Builder()
                    .url(url)
                    .get()
                    .addHeader("X-RapidAPI-Host", "edamam-food-and-grocery-database.p.rapidapi.com")
                    .addHeader("X-RapidAPI-Key", "d0cb222ccfmshdb370c52ef78688p171408jsn420b6036d587")
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                withContext(Dispatchers.Main) {
                    layoutTypingIndicator.visibility = View.GONE
                    if (response.isSuccessful && !responseBody.isNullOrEmpty()) {
                        val edamamResponse = com.google.gson.Gson().fromJson(responseBody, EdamamResponse::class.java)
                        val hints = edamamResponse.hints ?: emptyList()
                        
                        if (hints.isNotEmpty()) {
                            val df = java.text.DecimalFormat("#.#")
                            val mappedProducts = hints.take(4).mapNotNull { hint ->
                                val food = hint.food ?: return@mapNotNull null
                                val foodId = food.foodId ?: return@mapNotNull null
                                val rawId = foodId.hashCode()
                                val finalId = if (rawId == Int.MIN_VALUE) 99999 else Math.abs(rawId)
                                
                                val rawCategoryForMapping = food.category?.lowercase() ?: ""
                                val categoryMapped = when {
                                    rawCategoryForMapping.contains("dairy") || rawCategoryForMapping.contains("susu") -> "Susu"
                                    rawCategoryForMapping.contains("vegetable") || rawCategoryForMapping.contains("sayuran") || rawCategoryForMapping.contains("bayam") || rawCategoryForMapping.contains("tomat") || rawCategoryForMapping.contains("kentang") || rawCategoryForMapping.contains("fruit") || rawCategoryForMapping.contains("buah") || rawCategoryForMapping.contains("greens") || rawCategoryForMapping.contains("cabbage") -> "Sayuran"
                                    rawCategoryForMapping.contains("meat") || rawCategoryForMapping.contains("daging") || rawCategoryForMapping.contains("chicken") || rawCategoryForMapping.contains("poultry") || rawCategoryForMapping.contains("beef") -> "Daging"
                                    rawCategoryForMapping.contains("egg") || rawCategoryForMapping.contains("telur") -> "Telur"
                                    else -> "Sayuran"
                                }
                                
                                val basePrice = 15000
                                val variance = ((food.label ?: "").length * 500) % 10000
                                val calculatedPrice = basePrice + variance
                                
                                val nutrients = food.nutrients
                                
                                val product = com.agroSystem.app.data.models.Product(
                                    id = finalId,
                                    name = food.label ?: "Produk Segar Edamam",
                                    farmer = "Mitra Tani Edamam",
                                    rating = "4.9",
                                    price = calculatedPrice,
                                    unit = "1 kg",
                                    imageResId = R.drawable.sayuran,
                                    category = categoryMapped,
                                    isEcoFriendly = true,
                                    deliveryDays = 2,
                                    protein = "${df.format(nutrients?.PROCNT ?: 0.0)}g",
                                    fat = "${df.format(nutrients?.FAT ?: 0.0)}g",
                                    carbs = "${df.format(nutrients?.CHOCDF ?: 0.0)}g",
                                    calories = "${df.format(nutrients?.ENERC_KCAL ?: 0.0)} Kcal",
                                    ingredients = "Bahan segar bersumber dari Edamam Global Database.",
                                    ownerId = "google_admingmailcom",
                                    imageBytes = food.image
                                )
                                
                                // Register in allProducts so detail screen works
                                if (!sharedViewModel.allProducts.contains(product)) {
                                    sharedViewModel.allProducts.add(product)
                                }
                                
                                product
                            }
                            
                            val explanation = getFoodExplanation(userQuery)
                            val aiReply = "$explanation\n\nBerikut adalah rekomendasi produk segar '${englishQuery}' yang dicari langsung secara real-time dari API Edamam. Anda dapat mengklik kartu di bawah untuk melihat rincian gizi lengkap."
                            addAiMessage(aiReply, mappedProducts)
                        } else {
                            val aiReply = "Maaf, saya tidak menemukan produk segar dengan kata kunci '${userQuery}' di database Edamam."
                            addAiMessage(aiReply)
                        }
                    } else {
                        fallbackToLocalRecommendations(userQuery)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    layoutTypingIndicator.visibility = View.GONE
                    fallbackToLocalRecommendations(userQuery)
                }
            }
        }
    }

    private fun getEnglishFoodQuery(inputText: String): String? {
        val query = inputText.lowercase()
        return when {
            query.contains("apel") || query.contains("apple") -> "apple"
            query.contains("pisang") || query.contains("banana") -> "banana"
            query.contains("mangga") || query.contains("mango") -> "mango"
            query.contains("jeruk") || query.contains("orange") -> "orange"
            query.contains("tomat") || query.contains("tomato") -> "tomato"
            query.contains("alpukat") || query.contains("avocado") -> "avocado"
            query.contains("stroberi") || query.contains("strawberry") -> "strawberry"
            query.contains("anggur") || query.contains("grape") -> "grape"
            query.contains("pepaya") || query.contains("papaya") -> "papaya"
            query.contains("semangka") || query.contains("watermelon") -> "watermelon"
            query.contains("melon") -> "melon"
            query.contains("nanas") || query.contains("pineapple") -> "pineapple"
            query.contains("lemon") -> "lemon"
            query.contains("ceri") || query.contains("cherry") -> "cherry"
            query.contains("pir") || query.contains("pear") -> "pear"
            query.contains("sawi") || query.contains("cabbage") || query.contains("mustard") -> "mustard greens"
            query.contains("wortel") || query.contains("carrot") -> "carrot"
            query.contains("bayam") || query.contains("spinach") -> "spinach"
            query.contains("daging") || query.contains("beef") || query.contains("meat") -> "beef"
            query.contains("telur") || query.contains("egg") -> "egg"
            query.contains("keju") || query.contains("cheese") -> "cheese"
            query.contains("susu") || query.contains("milk") -> "milk"
            query.contains("buah") || query.contains("fruit") -> "fruit"
            query.contains("sayur") -> "vegetable"
            else -> null
        }
    }

    private fun getFoodExplanation(query: String): String {
        val lower = query.lowercase()
        return when {
            lower.contains("apel") -> "Apel adalah buah manis dan renyah yang kaya serat, vitamin C, dan berbagai antioksidan. Apel sangat baik untuk mendukung kesehatan pencernaan dan jantung."
            lower.contains("pisang") -> "Pisang adalah buah padat nutrisi yang kaya akan kalium, vitamin B6, dan serat pangan. Sangat baik untuk menambah energi secara cepat dan mencegah kram otot."
            lower.contains("mangga") -> "Mangga adalah buah tropis kaya serat, vitamin A, dan C. Membantu memperkuat imunitas tubuh serta mencerahkan kesehatan kulit secara alami."
            lower.contains("jeruk") -> "Jeruk kaya akan vitamin C dan flavonoid. Berperan aktif meningkatkan kekebalan tubuh dari serangan radikal bebas."
            lower.contains("tomat") -> "Tomat kaya akan likopen, zat antioksidan kuat penunjang kesehatan jantung dan pencegah penuaan dini pada sel tubuh."
            lower.contains("alpukat") -> "Alpukat memiliki kandungan lemak sehat tak jenuh tunggal yang tinggi untuk melancarkan sirkulasi darah serta menjaga kesehatan kolesterol."
            lower.contains("stroberi") -> "Stroberi adalah buah beri manis-asam kaya antioksidan polifenol dan vitamin C untuk memperlancar sirkulasi darah."
            lower.contains("anggur") -> "Anggur kaya serat dan resveratrol, senyawa yang mendukung fleksibilitas pembuluh darah dan kesehatan organ vital."
            lower.contains("pepaya") -> "Pepaya mengandung enzim papain yang membantu pencernaan lambung, serta tinggi vitamin A dan C."
            lower.contains("semangka") -> "Semangka memiliki kandungan air tinggi (92%) serta likopen untuk membantu hidrasi tubuh secara maksimal."
            lower.contains("melon") -> "Melon mengandung kalium tinggi dan vitamin C, sangat baik untuk menjaga keseimbangan tekanan darah."
            lower.contains("nanas") -> "Nanas mengandung bromelain yang sangat membantu mencerna protein, serta kaya akan vitamin C."
            lower.contains("lemon") -> "Lemon kaya asam sitrat dan vitamin C yang mendukung proses detoksifikasi tubuh dan meningkatkan absorpsi zat besi."
            lower.contains("pir") -> "Pir adalah buah renyah berair tinggi kaya serat larut pektin untuk membantu kelancaran pencernaan."
            lower.contains("sawi") -> "Sawi adalah sayuran hijau berdaun lebar kaya kalsium, serat, dan vitamin K untuk memperkuat tulang serta daya tahan tubuh."
            lower.contains("wortel") -> "Wortel sangat tinggi beta-karoten yang diubah tubuh menjadi vitamin A, elemen penting untuk kesehatan mata dan regenerasi kulit."
            lower.contains("bayam") -> "Bayam adalah sayuran hijau kaya zat besi, kalsium, dan asam folat, menunjang proses produksi sel darah merah."
            lower.contains("daging") -> "Daging sapi merupakan sumber protein hewani berkualitas tinggi, kaya zat besi heme yang mudah diserap, zink, dan vitamin B12."
            lower.contains("telur") -> "Telur adalah sumber protein hewani lengkap dengan sembilan asam amino esensial, kolin untuk otak, serta lutein untuk kesehatan retina mata."
            lower.contains("keju") -> "Keju adalah olahan fermentasi susu kaya protein, kalsium, dan vitamin B12 untuk menjaga kekuatan email gigi dan kepadatan tulang."
            lower.contains("susu") -> "Susu sapi murni merupakan sumber kalsium alami terbaik, fosfor, dan vitamin D untuk menunjang kekuatan struktur tulang."
            lower.contains("buah") -> "Buah-buahan segar merupakan sumber vitamin, mineral, serat pangan alami, dan air yang vital bagi kebugaran tubuh."
            lower.contains("sayur") -> "Sayur-sayuran hijau dan segar kaya akan serat larut, klorofil, serta zat gizi mikro pembangun imunitas tubuh."
            else -> "Produk segar berkualitas tinggi pilihan langsung dari mitra pertanian lokal dan global."
        }
    }

    private fun fallbackToLocalRecommendations(userQuery: String) {
        val localReply = "Koneksi ke API Edamam bermasalah. Berikut adalah alternatif sayur dan buah segar lokal yang tersedia di katalog kami:"
        val recommended = sharedViewModel.productsList.value?.filter { product ->
            product.category.lowercase().contains("sayur") || product.name.lowercase().contains("wortel") || product.name.lowercase().contains("tomat") || product.name.lowercase().contains("bayam")
        }?.take(3)
        addAiMessage(localReply, recommended)
    }

    private fun getSmartLocalReply(prompt: String): String {
        val query = prompt.lowercase()
        return when {
            query.contains("halo") || query.contains("hi") || query.contains("pagi") || query.contains("siang") || query.contains("sore") -> {
                "Halo! Selamat datang di Asisten AgriMitra. Saya di sini untuk membantu Anda mengelola rantai pasok tani, mencari produk segar berkualitas, atau memandu transaksi Anda. Ada yang bisa saya bantu hari ini?"
            }
            query.contains("bayam") || query.contains("sayur") -> {
                "Kami memiliki persediaan sayuran segar berkualitas tinggi seperti Bayam Hidroponik Bersih yang langsung dipanen oleh petani mitra kami. Anda bisa membelinya melalui katalog produk."
            }
            query.contains("padi") || query.contains("beras") -> {
                "Beras merah organik dan padi berkualitas tinggi tersedia di platform AgriMitra. Produk dipanen secara eco-friendly untuk menjaga kualitas nutrisinya."
            }
            query.contains("harga") || query.contains("murah") -> {
                "Harga produk di AgriMitra ditentukan langsung oleh petani mitra untuk memotong perantara, sehingga Anda mendapatkan harga terbaik. Silakan cek menu katalog untuk daftar harga terupdate."
            }
            query.contains("bayar") || query.contains("transaksi") || query.contains("payment") -> {
                "Untuk melakukan pembayaran, silakan masukkan produk ke keranjang belanja, lakukan checkout, dan Anda akan diarahkan ke simulasi gerbang pembayaran aman Midtrans."
            }
            query.contains("admin") -> {
                "Sebagai admin, Anda memiliki akses penuh ke menu Panel Admin di bagian profil untuk melihat statistik omset penjualan, total pengguna, serta mengelola daftar produk dan pengguna."
            }
            else -> {
                "Terima kasih atas pertanyaannya. Sebagai Asisten AgriMitra, saya merekomendasikan Anda untuk menjelajahi katalog kami untuk melihat sayuran segar, beras organik, dan hasil tani berkualitas lainnya dari petani lokal pilihan."
            }
        }
    }

    private fun addUserMessage(text: String) {
        messagesList.add(ChatMessage(text = text, isUser = true))
        chatAdapter.updateMessages(messagesList)
        scrollToBottom()
    }

    private fun addAiMessage(text: String, recommendedProducts: List<com.agroSystem.app.data.models.Product>? = null) {
        messagesList.add(ChatMessage(text = text, isUser = false, recommendedProducts = recommendedProducts))
        chatAdapter.updateMessages(messagesList)
        scrollToBottom()
    }

    private fun scrollToBottom() {
        if (messagesList.isNotEmpty()) {
            rvChatMessages.post {
                rvChatMessages.smoothScrollToPosition(messagesList.size - 1)
            }
        }
    }
}
