package com.example.communeease

import ChatAdapter
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DatabaseReference
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase

class random : AppCompatActivity() {
    private lateinit var senderName: TextView
    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var messageInput: EditText
    private lateinit var sendButton: Button
    private lateinit var chatAdapter: ChatAdapter
    private val chatMessages = mutableListOf<ChatMessage>()
    private lateinit var database: DatabaseReference
    private lateinit var currentUserNickname: String
    private lateinit var partnerId: String
    private lateinit var chatRoomId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_random)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        senderName = findViewById(R.id.senderName)
        chatRecyclerView = findViewById(R.id.chatRecyclerView)
        messageInput = findViewById(R.id.messageInput)
        sendButton = findViewById(R.id.sendButton)

        // Initialize Variables
        currentUserNickname = "User" // Replace with dynamic nickname if available
        senderName.text = currentUserNickname

        // Retrieve the partnerId from intent
        partnerId = intent.getStringExtra("PARTNER_ID") ?: ""

        // Generate a unique chat room ID for the two users
        chatRoomId = if (currentUserNickname < partnerId) {
            "$currentUserNickname-$partnerId"
        } else {
            "$partnerId-$currentUserNickname"
        }

        // Initialize RecyclerView
        chatRecyclerView.layoutManager = LinearLayoutManager(this)
        chatAdapter = ChatAdapter(chatMessages)
        chatRecyclerView.adapter = chatAdapter

        // Firebase Database Reference for the Chat Room
        database = FirebaseDatabase.getInstance().getReference("chatRooms").child(chatRoomId)

        // Send Message
        sendButton.setOnClickListener {
            val messageText = messageInput.text.toString().trim()
            if (messageText.isNotEmpty()) {
                val timestamp = System.currentTimeMillis()
                val chatMessage = ChatMessage(currentUserNickname, messageText, timestamp)

                // Save message to Firebase
                database.push().setValue(chatMessage).addOnCompleteListener {
                    if (it.isSuccessful) {
                        messageInput.text.clear()
                        Toast.makeText(this, "Message sent", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        database.addChildEventListener(object : ChildEventListener {
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
    }

}