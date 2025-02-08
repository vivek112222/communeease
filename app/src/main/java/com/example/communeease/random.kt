package com.example.communeease

import ChatAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.*

class random : AppCompatActivity() {
    private lateinit var SenderName: TextView
    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var messageInput: EditText
    private lateinit var nextbutton: Button
    private lateinit var sendButton: Button
    private lateinit var chatAdapter: ChatAdapter
    private val chatMessages = mutableListOf<ChatMessage>()
    private lateinit var database: DatabaseReference
    private lateinit var currentUserNickname: String
    private lateinit var partnerId: String
    private lateinit var chatRoomRef: DatabaseReference
    private lateinit var chatRoomId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_random)
        nextbutton=findViewById(R.id.next)

        SenderName = findViewById(R.id.senderName)
        chatRecyclerView = findViewById(R.id.chatRecyclerView)
        messageInput = findViewById(R.id.messageInput)
        sendButton = findViewById(R.id.sendButton)

        // Get user details
        currentUserNickname = intent.getStringExtra("CURRENT_USER_NICKNAME") ?: "User"
        partnerId = intent.getStringExtra("PARTNER_USERNAME") ?: ""
        val chatRoomName = intent.getStringExtra("CHAT_ROOM_NAME") ?: "Unknown"
        val partnerProfileImageIndex = intent.getStringExtra("PARTNER_PROFILE_IMAGE_INDEX") ?: "0"


        Log.d("ChatDebug", "Received Intent in random.kt. ChatRoom: $chatRoomName, Partner Username: $partnerId")

        val partnerProfileImage: ImageView = findViewById(R.id.profileImage)
        val profileResId = getProfileImageResource(partnerProfileImageIndex)
        partnerProfileImage.setImageResource(profileResId)


        // Generate unique chat room ID
        chatRoomId = chatRoomName

        SenderName.text = partnerId


        chatRoomRef = FirebaseDatabase.getInstance().getReference("chatRooms").child(chatRoomId)

        chatRoomRef.child("users").child(currentUserNickname).setValue("active")

        chatRecyclerView.layoutManager = LinearLayoutManager(this)
        chatAdapter = ChatAdapter(chatMessages, currentUserNickname)
        chatRecyclerView.adapter = chatAdapter


        chatRoomRef.child("messages").addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java)
                chatMessage?.let {
                    chatMessages.add(it)
                    chatAdapter.notifyItemInserted(chatMessages.size - 1)
                    chatRecyclerView.scrollToPosition(chatMessages.size - 1)
                }
            }
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        })

        nextbutton.setOnClickListener {
            val intent =Intent(this,Endpage::class.java)
            startActivity(intent)
        }

        // Send Message
        sendButton.setOnClickListener {
            val messageText = messageInput.text.toString().trim()
            if (messageText.isNotEmpty()) {
                val timestamp = System.currentTimeMillis()
                val chatMessage = ChatMessage(currentUserNickname, messageText, timestamp)

                chatRoomRef.child("messages").push().setValue(chatMessage).addOnCompleteListener {
                    if (it.isSuccessful) {
                        messageInput.text.clear()
                    } else {
                        Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun getProfileImageResource(index: String): Int {
        return when (index) {
            "1" -> R.drawable.profile1
            "2" -> R.drawable.profile2
            "3" -> R.drawable.profile3
            "4" -> R.drawable.profile4
            "5" -> R.drawable.profile5
            else -> R.drawable.profile
        }
    }

    // ✅ Mark user as "left" instead of deleting chat immediately
    private fun markUserLeft() {
        chatRoomRef.child("users").child(currentUserNickname).setValue("left").addOnCompleteListener {
            checkAndDeleteChatRoom()
        }
    }

    // ✅ Check if both users have left before deleting chat room
    private fun checkAndDeleteChatRoom() {
        chatRoomRef.child("users").get().addOnSuccessListener { snapshot ->
            var bothUsersLeft = true
            for (user in snapshot.children) {
                if (user.value != "left") {
                    bothUsersLeft = false
                    break
                }
            }
            if (bothUsersLeft) {
                chatRoomRef.removeValue().addOnCompleteListener {
                    Toast.makeText(this, "Chat room deleted", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        markUserLeft()
    }

    override fun onDestroy() {
        super.onDestroy()
        markUserLeft()
    }

    override fun onBackPressed() {
        markUserLeft()
        super.onBackPressed()
    }
}