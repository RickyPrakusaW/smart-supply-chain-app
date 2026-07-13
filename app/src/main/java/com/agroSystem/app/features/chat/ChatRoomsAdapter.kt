package com.agroSystem.app.features.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.agroSystem.app.R
import com.agroSystem.app.data.models.ChatRoom
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatRoomsAdapter(
    private var rooms: List<ChatRoom>,
    private val currentUserId: String,
    private val onItemClick: (ChatRoom, String) -> Unit
) : RecyclerView.Adapter<ChatRoomsAdapter.ViewHolder>() {

    fun updateRooms(newRooms: List<ChatRoom>) {
        rooms = newRooms
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_room, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val room = rooms[position]
        
        // Show the other user's name (if current user is buyer, show seller name; and vice-versa)
        val otherName = if (currentUserId == room.buyerId) room.sellerName else room.buyerName
        
        holder.bind(room, otherName)
        holder.itemView.setOnClickListener {
            onItemClick(room, otherName)
        }
    }

    override fun getItemCount(): Int = rooms.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textUserName: TextView = itemView.findViewById(R.id.text_chat_user_name)
        private val textLastMsg: TextView = itemView.findViewById(R.id.text_chat_last_msg)
        private val textTimestamp: TextView = itemView.findViewById(R.id.text_chat_timestamp)

        fun bind(room: ChatRoom, otherName: String) {
            textUserName.text = otherName
            textLastMsg.text = room.lastMessage
            
            if (room.lastMessageTimestamp > 0) {
                val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                textTimestamp.text = sdf.format(Date(room.lastMessageTimestamp))
                textTimestamp.visibility = View.VISIBLE
            } else {
                textTimestamp.visibility = View.GONE
            }
        }
    }
}
