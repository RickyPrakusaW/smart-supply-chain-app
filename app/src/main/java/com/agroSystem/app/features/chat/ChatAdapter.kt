package com.agroSystem.app.features.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.agroSystem.app.R
import com.agroSystem.app.data.models.ChatMessage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatAdapter(
    private var messages: List<ChatMessage>
) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    fun updateMessages(newMessages: List<ChatMessage>) {
        messages = newMessages
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_bubble, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    override fun getItemCount(): Int = messages.size

    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val layoutUserBubble: View = itemView.findViewById(R.id.layout_user_bubble)
        private val textUserMessage: TextView = itemView.findViewById(R.id.text_user_message)
        private val textUserTime: TextView = itemView.findViewById(R.id.text_user_time)

        private val layoutAiBubble: View = itemView.findViewById(R.id.layout_ai_bubble)
        private val textAiMessage: TextView = itemView.findViewById(R.id.text_ai_message)
        private val textAiTime: TextView = itemView.findViewById(R.id.text_ai_time)

        private val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())

        fun bind(message: ChatMessage) {
            val timeString = timeFormatter.format(Date(message.timestamp))
            
            if (message.isUser) {
                layoutUserBubble.visibility = View.VISIBLE
                layoutAiBubble.visibility = View.GONE
                textUserMessage.text = message.text
                textUserTime.text = timeString
            } else {
                layoutUserBubble.visibility = View.GONE
                layoutAiBubble.visibility = View.VISIBLE
                textAiMessage.text = message.text
                textAiTime.text = timeString
            }
        }
    }
}
