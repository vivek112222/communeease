package com.example.communeease

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatAdapter(
    private val messages: List<ChatMessage>,
    private val currentUserId: String,
    private val context: Context  // Needed for loading bad words from assets
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_RIGHT = 1
        private const val VIEW_TYPE_LEFT = 2
    }

    private val profanityFilter = ProfanityFilter(context) // Initialize profanity filter

    init {
        profanityFilter.loadBadWords() // Load bad words at initialization
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].senderNickname == currentUserId) { // ✅ Compare senderNickname
            VIEW_TYPE_RIGHT // ✅ Current user's messages → Right
        } else {
            VIEW_TYPE_LEFT // ✅ Friend's messages → Left
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_RIGHT) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_message_sent, parent, false)
            RightMessageViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_message_received, parent, false)
            LeftMessageViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        Log.d("ChatAdapter", "Message: ${message.messageText}, SenderNickname: ${message.senderNickname}, CurrentUserNickname: $currentUserId")

        if (holder is RightMessageViewHolder) {
            holder.bind(message)
        } else if (holder is LeftMessageViewHolder) {
            holder.bind(message)
        }
    }

    override fun getItemCount(): Int = messages.size

    // Convert timestamp to readable time
    private fun formatTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    inner class RightMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textMessage: TextView = itemView.findViewById(R.id.messageText)
        private val messageTime: TextView = itemView.findViewById(R.id.timestamp)

        fun bind(message: ChatMessage) {
            textMessage.text = profanityFilter.censorText(message.messageText) // Apply profanity filter
            messageTime.text = formatTime(message.timestamp)
        }
    }

    inner class LeftMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textMessage: TextView = itemView.findViewById(R.id.messageText)
        private val messageTime: TextView = itemView.findViewById(R.id.timestamp)

        fun bind(message: ChatMessage) {
            textMessage.text = profanityFilter.censorText(message.messageText) // Apply profanity filter
            messageTime.text = formatTime(message.timestamp)
        }
    }
}
