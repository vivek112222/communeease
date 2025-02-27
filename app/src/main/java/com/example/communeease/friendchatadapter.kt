package com.example.communeease

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FriendChatAdapter(
    private val messages: MutableList<FriendChat>,
    private val currentUserId: String,
    private val friendRoom: String,
    private val context: Context,
    private val selectionListener: SelectionListener // 🔥 Added listener for selection mode changes
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_RIGHT = 1
        private const val VIEW_TYPE_LEFT = 2
    }

    private val database = FirebaseDatabase.getInstance().reference
    private val selectedMessages = mutableSetOf<Int>()
    var isSelectionMode = false

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].senderNickname == currentUserId) VIEW_TYPE_RIGHT else VIEW_TYPE_LEFT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return if (viewType == VIEW_TYPE_RIGHT) {
            val view = inflater.inflate(R.layout.item_message_sent, parent, false)
            Log.d("AdapterDebug", "Inflating item_message_sent layout")
            RightMessageViewHolder(view)
        } else {
            val view = inflater.inflate(R.layout.item_message_received, parent, false)
            Log.d("AdapterDebug", "Inflating item_message_received layout")
            LeftMessageViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        Log.d("ChatAdapter", "Message: ${message.messageText}, Sender: ${message.senderNickname}")

        if (holder is RightMessageViewHolder) {
            holder.bind(message, position)
        } else if (holder is LeftMessageViewHolder) {
            holder.bind(message)
        }
    }

    override fun getItemCount(): Int = messages.size

    private fun formatTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    private fun deleteSelectedMessages() {
        val sortedSelectedPositions = selectedMessages.sortedDescending() // Avoid index shifting
        for (position in sortedSelectedPositions) {
            val message = messages[position]
            database.child("friendRoom").child(friendRoom).child(message.messageId).removeValue()
            messages.removeAt(position)
            notifyItemRemoved(position)
        }
        selectedMessages.clear()
        isSelectionMode = false
        selectionListener.onSelectionStateChanged(false) // 🔥 Notify that selection mode is OFF
        notifyDataSetChanged()
        Toast.makeText(context, "Messages deleted", Toast.LENGTH_SHORT).show()
    }

    private fun copyMessagesToClipboard() {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val copiedText = selectedMessages.map { position -> messages[position].messageText }
            .joinToString("\n")

        val clip = ClipData.newPlainText("Copied Messages", copiedText)
        clipboard.setPrimaryClip(clip)

        Toast.makeText(context, "Messages copied", Toast.LENGTH_SHORT).show()
    }

    inner class RightMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textMessage: TextView? = itemView.findViewById(R.id.messageText)
        private val messageTime: TextView? = itemView.findViewById(R.id.timestamp)

        fun bind(message: FriendChat, position: Int) {
            textMessage?.text = message.messageText ?: "Error loading message"
            messageTime?.text = formatTime(message.timestamp)

            itemView.setBackgroundColor(
                if (selectedMessages.contains(position)) context.getColor(R.color.selectedColor)
                else context.getColor(android.R.color.transparent)
            )

            itemView.setOnClickListener {
                if (isSelectionMode) toggleSelection(position)
            }

            itemView.setOnLongClickListener {
                isSelectionMode = true
                toggleSelection(position)
                selectionListener.onSelectionStateChanged(true) // 🔥 Notify selection mode ON
                true
            }
        }
    }

    inner class LeftMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textMessage: TextView? = itemView.findViewById(R.id.messageText)
        private val messageTime: TextView? = itemView.findViewById(R.id.timestamp)

        fun bind(message: FriendChat) {
            textMessage?.text = message.messageText ?: "Error loading message"
            messageTime?.text = formatTime(message.timestamp)
        }
    }

    private fun toggleSelection(position: Int) {
        if (selectedMessages.contains(position)) {
            selectedMessages.remove(position)
            if (selectedMessages.isEmpty()) {
                isSelectionMode = false
                selectionListener.onSelectionStateChanged(false) // 🔥 Notify selection mode OFF
            }
        } else {
            selectedMessages.add(position)
            isSelectionMode = true
        }
        notifyItemChanged(position)
    }

    fun clearSelection() {
        selectedMessages.clear()
        isSelectionMode = false
        selectionListener.onSelectionStateChanged(false) // 🔥 Notify selection mode OFF
        notifyDataSetChanged()
    }

    interface SelectionListener {
        fun onSelectionStateChanged(isSelectionActive: Boolean)
    }

    fun showPopupMenu(view: View, position: Int) {
        val popupMenu = PopupMenu(context, view)
        popupMenu.menuInflater.inflate(R.menu.friendchatmenu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.copyMessage -> {
                    copyMessagesToClipboard()
                    true
                }
                R.id.deleteMessage -> {
                    deleteSelectedMessages()
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }
}
