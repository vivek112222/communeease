package com.example.communeease

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class Waitingpage : AppCompatActivity() {
    private lateinit var database: FirebaseDatabase
    private lateinit var username: String
    private var profileImageIndex: Int? = null
    private lateinit var currentUserId: String
    private lateinit var waitingListRef: DatabaseReference
    private lateinit var chatRoomsRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_waitingpage)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Firebase Database
        database = FirebaseDatabase.getInstance()
        waitingListRef = database.reference.child("waitingUsers")
        chatRoomsRef = database.reference.child("chatRooms")

        // Get username and Firebase user ID
        username = intent.getStringExtra("USER_USERNAME") ?: ""
        profileImageIndex = intent.getIntExtra("PROFILE_IMAGE_INDEX", 0)
        currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: generateUserId()

        // Reset the waiting state
        resetUserState()

        // Add user to waiting list
        addToWaitingList()

        // Listen for matchmaking updates
        listenForMatch()
    }

    private fun generateUserId(): String {
        return System.currentTimeMillis().toString() // Temporary ID if Firebase Auth is unavailable
    }

    private fun resetUserState() {
        // ✅ Ensure user is removed from old chat rooms
        chatRoomsRef.orderByChild("user1").equalTo(currentUserId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (room in snapshot.children) {
                        room.ref.removeValue() // Remove any previous chat rooms
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })

        chatRoomsRef.orderByChild("user2").equalTo(currentUserId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (room in snapshot.children) {
                        room.ref.removeValue()
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun addToWaitingList() {
        val userMap = mapOf(
            "profile" to profileImageIndex,
            "username" to username,
            "waiting" to true
        )

        waitingListRef.child(currentUserId).setValue(userMap).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Added to waiting list.", Toast.LENGTH_SHORT).show()
                checkForMatch()
            } else {
                Toast.makeText(this, "Failed to add to waiting list.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkForMatch() {
        waitingListRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val waitingUsers = snapshot.children.mapNotNull { it.key }

                if (waitingUsers.size > 1) {
                    val partnerId = waitingUsers.first { it != currentUserId }

                    val chatRoomName = createChatRoomName(currentUserId, partnerId)

                    // ✅ Fetch partner's details (username & profile image index)
                    database.reference.child("waitingUsers").child(partnerId).get()
                        .addOnSuccessListener { partnerSnapshot ->
                            val partnerUsername = partnerSnapshot.child("username").value.toString()
                            val partnerProfileImageIndex = partnerSnapshot.child("profile").value.toString()

                            Log.d("ChatDebug", "Partner ID: $partnerId, Username: $partnerUsername, Profile Index: $partnerProfileImageIndex")

                            val myProfileImageIndex = profileImageIndex.toString()  // ✅ Get current user's profile image index

                            // ✅ Create chat room with profile image indices
                            createChatRoomInDatabase(chatRoomName, currentUserId, username, myProfileImageIndex, partnerId, partnerUsername, partnerProfileImageIndex)

                            removeUsersFromWaitingList(currentUserId, partnerId)

                            navigateToChat(chatRoomName, partnerUsername, partnerProfileImageIndex)
                        }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }



    private fun createChatRoomName(userId1: String, userId2: String): String {
        return if (userId1 < userId2) {
            "$userId1-$userId2"
        } else {
            "$userId2-$userId1"
        }
    }

    private fun createChatRoomInDatabase(chatRoomName: String, userId1: String, userName1: String, profileImage1: String, userId2: String, userName2: String, profileImage2: String) {
        val chatRoomRef = chatRoomsRef.child(chatRoomName)

        val chatRoomData = mapOf(
            "user1" to userId1,
            "username1" to userName1,
            "profileImage1" to profileImage1,  // ✅ Pass Profile Image Index
            "user2" to userId2,
            "username2" to userName2,
            "profileImage2" to profileImage2   // ✅ Pass Profile Image Index
        )

        chatRoomRef.setValue(chatRoomData).addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Toast.makeText(this, "Failed to create chat room.", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun removeUsersFromWaitingList(userId1: String, userId2: String) {
        waitingListRef.child(userId1).removeValue()
        waitingListRef.child(userId2).removeValue()
    }

    private fun listenForMatch() {
        chatRoomsRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatRoom = snapshot.key ?: return
                val user1 = snapshot.child("user1").value.toString()
                val user2 = snapshot.child("user2").value.toString()
                val partnerUsername = if (user1 == currentUserId) {
                    snapshot.child("username2").value.toString()
                } else {
                    snapshot.child("username1").value.toString()
                }

                val partnerProfileImageIndex = if (user1 == currentUserId) {
                    snapshot.child("profileImage2").value?.toString() ?: "0"
                } else {
                    snapshot.child("profileImage1").value?.toString() ?: "0"
                }

                if (user1 == currentUserId || user2 == currentUserId) {
                    navigateToChat(chatRoom, partnerUsername, partnerProfileImageIndex)
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        })
    }


    private fun navigateToChat(chatRoomName: String, partnerUsername: String, partnerProfileImageIndex: String) {
        Log.d("ChatDebug", "Preparing Intent. ChatRoom: $chatRoomName, Partner Username: $partnerUsername, Partner Profile Image Index: $partnerProfileImageIndex")

        val intent = Intent(this@Waitingpage, random::class.java)
        intent.putExtra("CHAT_ROOM_NAME", chatRoomName)
        intent.putExtra("PARTNER_USERNAME", partnerUsername)
        intent.putExtra("PARTNER_PROFILE_IMAGE_INDEX", partnerProfileImageIndex) // ✅ Pass partner's profile image index

        startActivity(intent)
        finish()
    }
    override fun onPause() {
        super.onPause()
        removeUserFromWaitingList()
    }

    override fun onBackPressed() {
        removeUserFromWaitingList()
        super.onBackPressed()
    }

    private fun removeUserFromWaitingList() {
        waitingListRef.child(currentUserId).removeValue().addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Toast.makeText(this, "Failed to remove from waiting list.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}