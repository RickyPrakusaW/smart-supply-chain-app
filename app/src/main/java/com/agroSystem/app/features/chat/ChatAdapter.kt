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

import androidx.recyclerview.widget.LinearLayoutManager
import com.agroSystem.app.data.models.Product

class ChatAdapter(
    private var messages: List<ChatMessage>,
    private val onProductClick: (Product) -> Unit
) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    fun updateMessages(newMessages: List<ChatMessage>) {
        messages = newMessages
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_bubble, parent, false)
        return ChatViewHolder(view, onProductClick)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    override fun getItemCount(): Int = messages.size

    class ChatViewHolder(
        itemView: View,
        private val onProductClick: (Product) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val layoutUserBubble: View = itemView.findViewById(R.id.layout_user_bubble)
        private val textUserMessage: TextView = itemView.findViewById(R.id.text_user_message)
        private val textUserTime: TextView = itemView.findViewById(R.id.text_user_time)

        private val layoutAiBubble: View = itemView.findViewById(R.id.layout_ai_bubble)
        private val textAiMessage: TextView = itemView.findViewById(R.id.text_ai_message)
        private val textAiTime: TextView = itemView.findViewById(R.id.text_ai_time)
        private val rvRecommendedProducts: RecyclerView = itemView.findViewById(R.id.rv_recommended_products)

        private val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())

        fun bind(message: ChatMessage) {
            val timeString = timeFormatter.format(Date(message.timestamp))
            
            if (message.isUser) {
                layoutUserBubble.visibility = View.VISIBLE
                layoutAiBubble.visibility = View.GONE
                textUserMessage.text = message.text
                textUserTime.text = timeString
                rvRecommendedProducts.visibility = View.GONE
            } else {
                layoutUserBubble.visibility = View.GONE
                layoutAiBubble.visibility = View.VISIBLE
                textAiMessage.text = message.text
                textAiTime.text = timeString

                if (!message.recommendedProducts.isNullOrEmpty()) {
                    rvRecommendedProducts.visibility = View.VISIBLE
                    rvRecommendedProducts.layoutManager = LinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL, false)
                    rvRecommendedProducts.adapter = ChatRecommendedProductsAdapter(message.recommendedProducts, onProductClick)
                } else {
                    rvRecommendedProducts.visibility = View.GONE
                }
            }
        }
    }
}
