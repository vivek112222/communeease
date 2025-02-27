import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.communeease.Friend
import com.example.communeease.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class FriendAdapter(
    friends: List<Friend>,
    private val onFriendClick: (Friend) -> Unit
) : RecyclerView.Adapter<FriendAdapter.FriendViewHolder>() {

    private val originalFriends: MutableList<Friend> = friends.toMutableList()
    private var filteredFriends: MutableList<Friend> = friends.toMutableList()

    class FriendViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val friendName: TextView = view.findViewById(R.id.friendsname)
        val profileImage: ImageView = view.findViewById(R.id.friendsprofile)
        val moreOptions: ImageView = view.findViewById(R.id.friendOptions) // New for options
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_friend, parent, false)
        Log.d("Friends", "Creating ViewHolder")
        return FriendViewHolder(view)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val friend = filteredFriends[position]
        Log.d("Friends", "Binding Friend: ${friend.username}, ProfileImageIndex: ${friend.profileImageIndex}")

        holder.friendName.text = friend.username

        val profileImages = listOf(
            R.drawable.profile, R.drawable.profile1, R.drawable.profile2,
            R.drawable.profile3, R.drawable.profile4, R.drawable.profile5
        )
        holder.profileImage.setImageResource(profileImages[friend.profileImageIndex.toInt()])

        // Reset selection color
        holder.itemView.setBackgroundResource(R.color.defaultBackground)

        holder.itemView.setOnClickListener {
            Log.d("Friends", "Clicked on Friend: ${friend.username}")
            onFriendClick(friend)
        }

        // Handle long press to select a friend
        holder.itemView.setOnLongClickListener {
            Log.d("Friends", "Long pressed on Friend: ${friend.username}")
            holder.itemView.setBackgroundResource(R.color.selectedColor) // Change background on selection
            true
        }

        // More Options Click Listener
        holder.moreOptions.setOnClickListener {
            Log.d("Friends", "More options clicked for: ${friend.username}")
            showPopupMenu(it, friend)
        }
    }

    override fun getItemCount(): Int {
        Log.d("Friends", "getItemCount() called, size = ${filteredFriends.size}")
        return filteredFriends.size
    }

    fun updateFriends(newFriends: List<Friend>) {
        originalFriends.clear()
        originalFriends.addAll(newFriends)  // Ensure originalFriends is updated

        filteredFriends.clear()
        filteredFriends.addAll(newFriends)

        Log.d("Friends", "Updated Original Friends List: $originalFriends")
        notifyDataSetChanged()
    }



    // Filter function for search
    fun filter(query: String) {
        Log.d("Friends", "Filtering with query: $query")
        Log.d("Friends", "Original Friends List:")
        filteredFriends = if (query.isEmpty()) {
            originalFriends.toMutableList()
        } else {
            originalFriends.filter { it.username.contains(query, ignoreCase = true) }.toMutableList()
        }
        Log.d("Friends", "Filtered list size: ${filteredFriends.size}")
        notifyDataSetChanged()
    }


    private fun showPopupMenu(view: View, friend: Friend) {
        val popupMenu = PopupMenu(view.context, view)
        popupMenu.inflate(R.menu.friend_option_menu)
        Log.d("Friends", "Showing popup menu for: ${friend.username}")

        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.remove_friend -> {
                    Log.d("Friends", "Remove Friend selected for: ${friend.username}")
                    // Handle remove friend logic
                    removeFriend(friend)
                    true
                }
                R.id.view_profile -> {
                    Log.d("Friends", "View Profile selected for: ${friend.username}")
                    // Handle view profile logic
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }
    fun removeFriend(friend: Friend) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId == null) {
            Log.e("Friends", "Current user ID is null")
            return
        }

        val db = FirebaseDatabase.getInstance().getReference()

        // Remove friend from the current user's list
        db.child("users").child(currentUserId).child("friendList").child(friend.friendId)
            .removeValue()
            .addOnSuccessListener {
                Log.d("Friends", "Friend removed from Firebase Realtime DB: ${friend.friendId}")
                // Update UI after removal
                originalFriends.remove(friend)
                filteredFriends.remove(friend)
                notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Log.e("Friends", "Failed to remove friend from Firebase Realtime DB: ${friend.friendId}", e)
            }

    }



}
