package com.example.communeease

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class Waitingpage : AppCompatActivity() {
    private lateinit var database: FirebaseDatabase
    private lateinit var username: String
    private lateinit var currentUserId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_waitingpage)

        // Set up padding for edge-to-edge support
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Firebase Realtime Database
        database = FirebaseDatabase.getInstance()

        // Get username and Firebase user ID
        username = intent.getStringExtra("USER_USERNAME") ?: ""
        currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: generateUserId()

        // Add the user to the waiting list
        addToWaitingList()

        // Check for a match
        checkForMatch()

        // Ensure the user is removed from the waiting list if they disconnect
        removeFromWaitingListOnDisconnect()
    }

    // Generate a temporary user ID if Firebase Auth UID is not available (for safety)
    private fun generateUserId(): String {
        return System.currentTimeMillis().toString() // Fallback to a temporary ID
    }

    private fun addToWaitingList() {
        val waitingListRef = database.reference.child("waitingUsers").child(currentUserId)

        val userMap = mapOf(
            "username" to username,
            "waiting" to true
        )

        waitingListRef.setValue(userMap).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Added to waiting list.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to add to waiting list.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkForMatch() {
        val waitingListRef = database.reference.child("waitingUsers")

        // Listen for changes in the waiting list
        waitingListRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val waitingUsers = snapshot.children.map { it.key!! }

                if (waitingUsers.size > 1) {
                    // Find the first available match (user that is not the current user)
                    val partnerId = waitingUsers.first { it != currentUserId }

                    // Create a chat room name using both user IDs
                    val chatRoomName = createChatRoomName(currentUserId, partnerId)

                    // Store the chat room in the database under "chatRooms"
                    createChatRoomInDatabase(chatRoomName, currentUserId, partnerId)

                    // Remove users from the waiting list
                    removeUsersFromWaitingList(currentUserId, partnerId)

                    // Start chat with partner
                    val intent = Intent(this@Waitingpage, random::class.java)
                    intent.putExtra("USER_USERNAME", username)
                    intent.putExtra("CHAT_ROOM_NAME", chatRoomName)
                    startActivity(intent)
                    finish() // Close waiting page
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Waitingpage, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun createChatRoomName(userId1: String, userId2: String): String {
        return if (userId1 < userId2) {
            "$userId1-$userId2"
        } else {
            "$userId2-$userId1"
        }
    }

    private fun createChatRoomInDatabase(chatRoomName: String, userId1: String, userId2: String) {
        val chatRoomRef = database.reference.child("chatRooms").child(chatRoomName)

        val chatRoomData = mapOf(
            "user1" to userId1,
            "user2" to userId2
        )

        chatRoomRef.setValue(chatRoomData).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Chat room created.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to create chat room.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun removeUsersFromWaitingList(userId1: String, userId2: String) {
        val waitingListRef = database.reference.child("waitingUsers")

        // Remove users from the waiting list
        waitingListRef.child(userId1).removeValue()
        waitingListRef.child(userId2).removeValue()
    }

    // Remove user from the waiting list when they disconnect (close the app or leave the page)
    private fun removeFromWaitingListOnDisconnect() {
        val waitingListRef = database.reference.child("waitingUsers").child(currentUserId)

        // Use Firebase's onDisconnect() to remove user from the waiting list if disconnected
        waitingListRef.onDisconnect().removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "User will be removed from waiting list upon disconnection.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to set disconnect behavior.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Remove user from the waiting list when they leave the waiting page
    override fun onPause() {
        super.onPause()
        removeUserFromWaitingList()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        removeUserFromWaitingList()
    }

    private fun removeUserFromWaitingList() {
        val waitingListRef = database.reference.child("waitingUsers").child(currentUserId)

        waitingListRef.removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Removed from waiting list.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to remove from waiting list.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
