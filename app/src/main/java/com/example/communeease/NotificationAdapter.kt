import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.communeease.Notification
import com.example.communeease.R

class NotificationAdapter(
private val notifications: List<Notification>,
private val onAcceptClick: (Notification) -> Unit,
private val onRejectClick: (Notification) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.notification_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val notification = notifications[position]
        holder.bind(notification, onAcceptClick, onRejectClick)
    }

    override fun getItemCount() = notifications.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val profileimageindex:ImageView =itemView.findViewById(R.id.profileImage)
        private val senderNameTextView: TextView = itemView.findViewById(R.id.username)
        private val acceptButton: Button = itemView.findViewById(R.id.acceptButton)
        private val rejectButton: Button = itemView.findViewById(R.id.rejectButton)

        fun bind(notification: Notification, onAcceptClick: (Notification) -> Unit, onRejectClick: (Notification) -> Unit) {
            senderNameTextView.text = notification.senderUsername

            profileimageindex.setImageResource(getProfileImageResource(notification.profileImageIndex))



            acceptButton.setOnClickListener { onAcceptClick(notification) }
            rejectButton.setOnClickListener { onRejectClick(notification) }
        }
        private fun getProfileImageResource(index: String): Int {
            return when (index) {
                "1" -> R.drawable.profile1
                "2" -> R.drawable.profile2
                "3" -> R.drawable.profile3
                "4" ->R.drawable.profile4
                "5"-> R.drawable.profile5
                else -> R.drawable. profile
            }
        }
    }

}
