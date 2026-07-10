package com.agroSystem.app.features.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.agroSystem.app.R
import com.agroSystem.app.data.models.ChatMessage
import com.agroSystem.app.data.remote.ApiClient
import com.agroSystem.app.data.remote.ChatRequest
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch

class ChatAssistantFragment : Fragment() {

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

        // 3. Request AI Response from Backend
        lifecycleScope.launch {
            try {
                val response = ApiClient.authApiService.sendChatMessage(ChatRequest(text))
                layoutTypingIndicator.visibility = View.GONE
                
                if (response.success) {
                    addAiMessage(response.reply, response.recommendedProducts)
                } else {
                    addAiMessage("Maaf, Asisten Tani sedang istirahat sebentar. Silakan coba tanyakan beberapa saat lagi.")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                layoutTypingIndicator.visibility = View.GONE
                addAiMessage("Maaf, terjadi kesalahan koneksi ke server. Pastikan koneksi internet Anda aktif.")
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
