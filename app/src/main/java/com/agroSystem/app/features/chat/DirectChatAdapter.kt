package com.agroSystem.app.features.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.agroSystem.app.R
import com.agroSystem.app.data.models.DirectMessage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DirectChatAdapter(
    private var messages: List<DirectMessage>,
    private val currentUserId: String
) : RecyclerView.Adapter<DirectChatAdapter.ViewHolder>() {

    fun updateMessages(newMessages: List<DirectMessage>) {
        messages = newMessages
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_direct_message, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val msg = messages[position]
        holder.bind(msg, currentUserId)
    }

    override fun getItemCount(): Int = messages.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val layoutSent: View = itemView.findViewById(R.id.layout_sent)
        private val textSentBody: TextView = itemView.findViewById(R.id.text_sent_body)
        private val textSentTime: TextView = itemView.findViewById(R.id.text_sent_time)

        private val layoutReceived: View = itemView.findViewById(R.id.layout_received)
        private val textReceivedBody: TextView = itemView.findViewById(R.id.text_received_body)
        private val textReceivedTime: TextView = itemView.findViewById(R.id.text_received_time)

        fun bind(message: DirectMessage, currentUserId: String) {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            val timeStr = if (message.timestamp > 0L) sdf.format(Date(message.timestamp)) else ""

            if (message.senderId == currentUserId) {
                // Sent message
                layoutSent.visibility = View.VISIBLE
                layoutReceived.visibility = View.GONE
                
                textSentBody.text = message.messageText
                textSentTime.text = timeStr
            } else {
                // Received message
                layoutSent.visibility = View.GONE
                layoutReceived.visibility = View.VISIBLE

                textReceivedBody.text = message.messageText
                textReceivedTime.text = timeStr
            }
        }
    }
}
