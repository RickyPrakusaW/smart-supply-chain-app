package com.agroSystem.app.features.chat

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.agroSystem.app.R
import com.agroSystem.app.data.models.DirectMessage
import com.agroSystem.app.features.auth.AuthViewModel
import com.google.android.material.card.MaterialCardView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class DirectChatFragment : Fragment() {

    private val authViewModel: AuthViewModel by activityViewModels()

    private lateinit var chatRoomId: String
    private lateinit var otherUserName: String

    private lateinit var btnBack: MaterialCardView
    private lateinit var textTitle: TextView
    private lateinit var rvDirectMessages: RecyclerView
    private lateinit var inputMessage: EditText
    private lateinit var btnSend: MaterialCardView

    private lateinit var messagesAdapter: DirectChatAdapter
    private var messagesListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        chatRoomId = arguments?.getString("chatRoomId") ?: ""
        otherUserName = arguments?.getString("otherUserName") ?: "Mitra"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_direct_chat, container, false)

        btnBack = view.findViewById(R.id.btn_back)
        textTitle = view.findViewById(R.id.text_chatting_user_name)
        rvDirectMessages = view.findViewById(R.id.rv_direct_messages)
        inputMessage = view.findViewById(R.id.input_chat_message)
        btnSend = view.findViewById(R.id.btn_send_chat)

        textTitle.text = otherUserName

        btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        setupRecyclerView()
        listenForMessages()

        btnSend.setOnClickListener {
            val text = inputMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                inputMessage.text.clear()
                sendMessageToFirebase(text)
            }
        }

        return view
    }

    private fun setupRecyclerView() {
        val currentUser = authViewModel.currentUser.value
        val userId = currentUser?.id ?: ""
        
        messagesAdapter = DirectChatAdapter(emptyList(), userId)
        val layoutManager = LinearLayoutManager(requireContext()).apply {
            stackFromEnd = true
        }
        rvDirectMessages.layoutManager = layoutManager
        rvDirectMessages.adapter = messagesAdapter
    }

    private fun listenForMessages() {
        val db = FirebaseFirestore.getInstance()
        messagesListener = db.collection("chats")
            .document(chatRoomId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("DirectChat", "Firestore listening failed.", e)
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    val messagesList = snapshots.mapNotNull { doc ->
                        doc.toObject(DirectMessage::class.java)
                    }
                    messagesAdapter.updateMessages(messagesList)
                    if (messagesList.isNotEmpty()) {
                        rvDirectMessages.scrollToPosition(messagesList.size - 1)
                    }
                }
            }
    }

    private fun sendMessageToFirebase(text: String) {
        val currentUser = authViewModel.currentUser.value ?: return
        val db = FirebaseFirestore.getInstance()
        val messageId = db.collection("chats").document(chatRoomId).collection("messages").document().id

        val message = DirectMessage(
            messageId = messageId,
            senderId = currentUser.id,
            senderName = currentUser.name,
            messageText = text,
            timestamp = System.currentTimeMillis()
        )

        db.collection("chats").document(chatRoomId)
            .collection("messages")
            .document(messageId)
            .set(message)
            .addOnSuccessListener {
                db.collection("chats").document(chatRoomId)
                    .update(
                        "lastMessage", text,
                        "lastMessageTimestamp", System.currentTimeMillis()
                    )
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Gagal mengirim pesan.", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        messagesListener?.remove()
    }
}
