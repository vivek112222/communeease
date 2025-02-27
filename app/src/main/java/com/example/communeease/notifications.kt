package com.example.communeease

import NotificationAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class notifications : AppCompatActivity() {
    private lateinit var backs: ImageView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NotificationAdapter
    private val notificationsList: MutableList<Notification> = mutableListOf()

    private lateinit var currentUserNickname: String
    private var notificationsEnabled: Boolean = true
    private lateinit var notificationMessage: TextView

    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        currentUserNickname = intent.getStringExtra("USER_USERNAME") ?: "Unknown"
        Log.d("Notification", "Received Username: $userId")
        notificationsEnabled = intent.getBooleanExtra("notificationsEnabled", true)
        notificationMessage = findViewById(R.id.tvNotificationMessage)
        recyclerView = findViewById(R.id.rvFriendsList)
        backs=findViewById(R.id.back)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = NotificationAdapter(notificationsList, ::onAcceptClick, ::rejectFriendRequest)
        recyclerView.adapter = adapter
        backs.setOnClickListener {
            startActivity(Intent(this,Home::class.java))
        }

        loadNotifications()
    }

    private fun loadNotifications() {
        val sharedPreferences = getSharedPreferences("ChatAppPrefs", MODE_PRIVATE)
        val notificationEnabled = sharedPreferences.getBoolean("notificationEnabled", true)
        notificationsList.clear() // Clear the list before loading data

        Log.d("NotificationDebugs", "Received Username: $notificationsEnabled")
        if (!notificationsEnabled) {
            Log.d("NotificationDebugs", "Notifications are disabled, showing message above RecyclerView.")

            notificationMessage.visibility = View.VISIBLE // Show TextView
            recyclerView.visibility = View.GONE // Hide RecyclerView
            return
        } else {
            notificationMessage.visibility = View.GONE // Hide TextView
            recyclerView.visibility = View.VISIBLE // Show RecyclerView
        }


        val database = FirebaseDatabase.getInstance().reference
        val requestRef = database.child("notifications").child(userId).child("friendRequests")

        Log.d("NotificationDebug", "Checking Firebase path: notifications/$userId/friendRequests")

        requestRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                notificationsList.clear() // Ensure it's clean before adding data

                if (!snapshot.exists()) {
                    Log.d("NotificationDebug", "No friend requests found in Firebase")
                    adapter.notifyDataSetChanged() // Ensure UI updates
                    return
                }

                for (request in snapshot.children) {
                    val senderId = request.key ?: continue
                    val senderUsername = request.child("senderUsername").getValue(String::class.java) ?: "Unknown"
                    val profileImageIndex = request.child("senderProfileImage").getValue(String::class.java) ?: "1"
                    val status = request.child("status").getValue(String::class.java) ?: "pending"

                    Log.d("NotificationDebug", "Loaded friend request from: $senderUsername")

                    notificationsList.add(Notification(senderId, senderUsername, profileImageIndex, status))
                }

                Log.d("NotificationDebug", "Total friend requests loaded: ${notificationsList.size}")

                adapter.notifyDataSetChanged() // Ensure UI updates
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("NotificationDebug", "Firebase error: ${error.message}", error.toException())
            }
        })
    }



    private fun onAcceptClick(request: Notification) {
        val senderId=request.senderId
        val profileIndex = request.profileImageIndex.toIntOrNull() ?: 0
        acceptFriendRequest(userId, request.senderId, request.senderUsername, profileIndex)
    }
    fun acceptFriendRequest(currentUserId: String, friendId: String, friendUsername: String, friendImageIndex: Int) {
        val database = FirebaseDatabase.getInstance().reference
        val timestamp = System.currentTimeMillis() / 1000
        val expiryTime = timestamp + (3 * 24 * 60 * 60) // 3 days later
        val chatRoomId = "$currentUserId-$friendId"

        val currentUserRef = database.child("users").child(currentUserId)
        val friendUserRef = database.child("users").child(friendId)

        // Get current user's username and profileImageIndex
        currentUserRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val currentUsername = snapshot.child("username").getValue(String::class.java) ?: "Unknown"
                val currentUserProfileImageIndex = intent.getIntExtra("PROFILE_IMAGE_INDEX", 0)


                val friendData = mapOf(
                    "friendUsername" to friendUsername,
                    "friendImageIndex" to friendImageIndex,
                    "friendRoom" to chatRoomId,
                    "acceptedAt" to timestamp,
                    "expiresAt" to expiryTime
                )

                val currentUserData = mapOf(
                    "friendUsername" to currentUsername,
                    "friendImageIndex" to currentUserProfileImageIndex,
                    "friendRoom" to chatRoomId,
                    "acceptedAt" to timestamp,
                    "expiresAt" to expiryTime
                )

                val updates = hashMapOf<String, Any>(
                    "users/$currentUserId/friendList/$friendId" to friendData,
                    "users/$friendId/friendList/$currentUserId" to currentUserData
                )

                database.updateChildren(updates)
                    .addOnSuccessListener {
                        Log.d("FriendRequest", "✅ Friend request accepted and stored correctly.")
                        Toast.makeText(this@notifications, "Friend request accepted!", Toast.LENGTH_SHORT).show()
                        deleteNotification(friendId)
                        loadNotifications()
                    }
                    .addOnFailureListener { Log.e("FriendRequest", "❌ Error: ${it.message}") }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FriendRequest", "❌ Firebase error: ${error.message}")
            }
        })
    }


    private fun deleteNotification(senderUserId: String) {
        val database = FirebaseDatabase.getInstance().reference
        val notificationRef = database.child("notifications").child(userId).child("friendRequests").child(senderUserId)

        notificationRef.removeValue()
            .addOnSuccessListener {
                Log.d("Notification", "✅ Friend request from $senderUserId removed successfully.")
                Toast.makeText(this, "Notification removed!", Toast.LENGTH_SHORT).show()

                // ✅ Remove from local list and refresh RecyclerView
                notificationsList.removeAll { it.senderUsername == senderUserId }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { error ->
                Log.e("Notification", "❌ Failed to remove friend request: ${error.message}")
            }
    }
    private fun rejectFriendRequest(request: Notification) {
        val database = FirebaseDatabase.getInstance().reference
        val senderUsername = request.senderUsername

        // ✅ Use userId instead of currentUserNickname
        database.child("notifications").child(userId).child("friendRequests").child(request.senderId).removeValue()
            .addOnSuccessListener {
                Log.d("FriendRequest", "Friend request rejected.")
                Toast.makeText(this, "Friend request rejected!", Toast.LENGTH_SHORT).show()
                loadNotifications()
            }

        val senderNotificationRef = database.child("notifications").child(senderUsername).child("responses").child(userId)
        senderNotificationRef.setValue(mapOf(
            "message" to "$currentUserNickname has rejected your friend request!",
            "type" to "rejected"
        ))
    }

}
