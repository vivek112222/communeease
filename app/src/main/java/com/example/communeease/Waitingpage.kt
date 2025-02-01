package com.example.communeease

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Waitingpage : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var username: String
    private lateinit var currentUserId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_waitingpage)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = FirebaseFirestore.getInstance()
        username = intent.getStringExtra("USER_USERNAME") ?: ""
        currentUserId = generateUserId()

        // Add the user to the waiting list
        addToWaitingList()

        // Check for a match
        checkForMatch()
    }

    private fun generateUserId(): String {
        return System.currentTimeMillis().toString() // Use a unique identifier for the user
    }

    private fun addToWaitingList() {
        db.collection("users")
            .whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val userId = documents.first().id
                    // Add to waiting list with userId
                    db.collection("waitingUsers").document(userId).set(mapOf("waiting" to true))
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                Toast.makeText(this, "Added to waiting list.", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this, "Failed to add to waiting list.", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    Toast.makeText(this, "Error fetching user details", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun checkForMatch() {
        val waitingListRef = FirebaseDatabase.getInstance().reference.child("waitingUsers")

        waitingListRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val waitingUsers = snapshot.children.map { it.key!! }

                if (waitingUsers.size > 1) {
                    val partnerId = waitingUsers.first { it != currentUserId }

                    // Create chatroom name using both userIds
                    val chatRoomName = createChatRoomName(currentUserId, partnerId)

                    // Remove users from waiting list
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

    private fun removeUsersFromWaitingList(userId1: String, userId2: String) {
        FirebaseDatabase.getInstance().reference.child("waitingUsers").child(userId1).removeValue()
        FirebaseDatabase.getInstance().reference.child("waitingUsers").child(userId2).removeValue()
    }
}
