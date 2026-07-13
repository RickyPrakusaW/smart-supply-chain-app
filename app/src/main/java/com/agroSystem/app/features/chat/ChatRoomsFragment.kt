package com.agroSystem.app.features.chat

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.agroSystem.app.R
import com.agroSystem.app.data.models.ChatRoom
import com.agroSystem.app.features.auth.AuthViewModel
import com.google.android.material.card.MaterialCardView
import com.google.firebase.firestore.FirebaseFirestore

class ChatRoomsFragment : Fragment() {

    private val authViewModel: AuthViewModel by activityViewModels()

    private lateinit var btnBack: MaterialCardView
    private lateinit var progressLoader: ProgressBar
    private lateinit var rvChatRooms: RecyclerView
    private lateinit var layoutEmptyChat: View

    private lateinit var roomsAdapter: ChatRoomsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chat_rooms, container, false)

        btnBack = view.findViewById(R.id.btn_back)
        progressLoader = view.findViewById(R.id.progress_loader)
        rvChatRooms = view.findViewById(R.id.rv_chat_rooms)
        layoutEmptyChat = view.findViewById(R.id.layout_empty_chat)

        btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        setupRecyclerView()
        loadChatRooms()

        return view
    }

    private fun setupRecyclerView() {
        val currentUser = authViewModel.currentUser.value
        val userId = currentUser?.id ?: ""
        
        roomsAdapter = ChatRoomsAdapter(emptyList(), userId) { room, otherName ->
            val bundle = bundleOf(
                "chatRoomId" to room.chatRoomId,
                "otherUserName" to otherName
            )
            findNavController().navigate(R.id.action_chatRoomsFragment_to_directChatFragment, bundle)
        }
        
        rvChatRooms.layoutManager = LinearLayoutManager(requireContext())
        rvChatRooms.adapter = roomsAdapter
    }

    private fun loadChatRooms() {
        val currentUser = authViewModel.currentUser.value
        if (currentUser == null) {
            Toast.makeText(requireContext(), "Silakan login terlebih dahulu!", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
            return
        }

        progressLoader.visibility = View.VISIBLE
        rvChatRooms.visibility = View.GONE
        layoutEmptyChat.visibility = View.GONE

        val db = FirebaseFirestore.getInstance()
        
        db.collection("chats")
            .get()
            .addOnSuccessListener { result ->
                progressLoader.visibility = View.GONE
                try {
                    val allRooms = result.mapNotNull { doc ->
                        doc.toObject(ChatRoom::class.java)
                    }
                    
                    // Filter rooms where user is involved
                    val filteredRooms = allRooms.filter { room ->
                        room.buyerId == currentUser.id || room.sellerId == currentUser.id
                    }.sortedByDescending { it.lastMessageTimestamp }

                    if (filteredRooms.isEmpty()) {
                        layoutEmptyChat.visibility = View.VISIBLE
                        rvChatRooms.visibility = View.GONE
                    } else {
                        roomsAdapter.updateRooms(filteredRooms)
                        rvChatRooms.visibility = View.VISIBLE
                        layoutEmptyChat.visibility = View.GONE
                    }
                } catch (e: Exception) {
                    Log.e("ChatRooms", "Error parsing chat rooms", e)
                    layoutEmptyChat.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener { e ->
                progressLoader.visibility = View.GONE
                layoutEmptyChat.visibility = View.VISIBLE
                Log.e("ChatRooms", "Error fetching chats", e)
            }
    }
}
