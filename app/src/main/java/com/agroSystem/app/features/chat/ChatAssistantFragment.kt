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

        val requestBody = okhttp3.RequestBody.create(
            okhttp3.MediaType.parse("application/json; charset=utf-8"),
            requestBodyJson
        )

        // API Key placeholder - if empty, will gracefully execute the local smart fallback chatbot
        val geminiApiKey = ""
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
                val responseBody = response.body()?.string()

                withContext(Dispatchers.Main) {
                    layoutTypingIndicator.visibility = View.GONE
                    if (response.isSuccessful && !responseBody.isNullOrEmpty()) {
                        val jsonObject = com.google.gson.JsonParser.parseString(responseBody).asJsonObject
                        val reply = jsonObject.getAsJsonArray("candidates")
                            ?.get(0)?.asJsonObject
                            ?.getAsJsonObject("content")
                            ?.getAsJsonArray("parts")
                            ?.get(0)?.asJsonObject
                            ?.get("text")?.asString ?: "Maaf, Asisten Tani tidak memahami respon tersebut."

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
