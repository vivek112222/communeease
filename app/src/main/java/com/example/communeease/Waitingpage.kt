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
    private var oppUserId: String? = null // Nullable to prevent crashes
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
        val sharedPreferences = getSharedPreferences("ChatAppPrefs", MODE_PRIVATE)

        database = FirebaseDatabase.getInstance()
        waitingListRef = database.reference.child("waitingUsers")
        chatRoomsRef = database.reference.child("chatRooms")


        username = sharedPreferences.getString("username", "Unknown User") ?: "Unknown User"
        profileImageIndex = sharedPreferences.getInt("profileImageIndex", 0)
        Log.d("HomeActivity", "User details loaded from SharedPreferences: username=$username, profileImageIndex=$profileImageIndex")
        currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: generateUserId()

        resetUserState()
        addToWaitingList()
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
        waitingListRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                val waitingUsers = mutableData.children.mapNotNull { it.key }.toMutableList()

                if (waitingUsers.size > 1) {
                    val partnerId = waitingUsers.firstOrNull { it != currentUserId } ?: return Transaction.success(mutableData)
                    oppUserId = partnerId

                    // ✅ Fetch partner username & profile image from waiting list
                    val partnerUsername = mutableData.child(partnerId).child("username").value.toString()
                    val partnerProfileIndex = mutableData.child(partnerId).child("profile").value?.toString() ?: "0"

                    mutableData.child(currentUserId).value = null
                    mutableData.child(partnerId).value = null

                    val chatRoomName = createChatRoomName(currentUserId, partnerId)

                    // ✅ Store both users' details in chat room
                    val chatRoomData = mapOf(
                        "user1" to currentUserId,
                        "username1" to username,
                        "profileImage1" to profileImageIndex.toString(),
                        "user2" to partnerId,
                        "username2" to partnerUsername,
                        "profileImage2" to partnerProfileIndex
                    )

                    chatRoomsRef.child(chatRoomName).setValue(chatRoomData)

                    return Transaction.success(mutableData)
                }
                return Transaction.success(mutableData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                if (committed) {
                    listenForMatch()  // ✅ Ensures users go to chat when matched
                }
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

    private fun listenForMatch() {
        chatRoomsRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatRoom = snapshot.key ?: return
                val user1 = snapshot.child("user1").value.toString()
                val user2 = snapshot.child("user2").value.toString()

                if (user1 == currentUserId || user2 == currentUserId) {
                    oppUserId = if (user1 == currentUserId) user2 else user1

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
        if (oppUserId == null) {
            Log.e("ChatDebug", "Opponent user ID is null, cannot navigate.")
            return
        }

        Log.d("ChatDebug", "Navigating to Chat. ChatRoom: $chatRoomName, Opponent: $oppUserId")

        val intent = Intent(this@Waitingpage, random::class.java)
        intent.putExtra("CHAT_ROOM_NAME", chatRoomName)
        intent.putExtra("PARTNER_USERNAME", partnerUsername)
        intent.putExtra("OPPONENT_USER_ID", oppUserId)
        intent.putExtra("USER_USERNAME", username)
        intent.putExtra("PROFILE_IMAGE_INDEX", profileImageIndex)
        intent.putExtra("PARTNER_PROFILE_IMAGE_INDEX", partnerProfileImageIndex)

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
        waitingListRef.child(currentUserId).removeValue()
    }
}
