import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.communeease.ChatMessage
import com.example.communeease.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatAdapter(private val chatMessages: MutableList<ChatMessage>) :
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_message, parent, false)
        return ChatViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chatMessage = chatMessages[position]
        holder.bind(chatMessage)
    }

    override fun getItemCount(): Int {
        return chatMessages.size
    }

    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageTextView: TextView = itemView.findViewById(R.id.messageText)
        private val timestampTextView: TextView = itemView.findViewById(R.id.timestamp)

        fun bind(chatMessage: ChatMessage) {
            messageTextView.text = chatMessage.messageText
            timestampTextView.text = formatTimestamp(chatMessage.timestamp)
        }

        private fun formatTimestamp(timestamp: Long): String {
            val date = Date(timestamp)
            val format = SimpleDateFormat("hh:mm a", Locale.getDefault())
            return format.format(date)
        }
    }
}
