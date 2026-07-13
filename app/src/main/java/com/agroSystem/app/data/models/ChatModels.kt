package com.agroSystem.app.data.models

data class ChatRoom(
    val chatRoomId: String = "",
    val buyerId: String = "",
    val buyerName: String = "",
    val sellerId: String = "",
    val sellerName: String = "",
    val lastMessage: String = "",
    val lastMessageTimestamp: Long = 0L
)

data class DirectMessage(
    val messageId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val messageText: String = "",
    val timestamp: Long = 0L
)
