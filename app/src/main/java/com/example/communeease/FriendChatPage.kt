package com.example.communeease

import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import android.view.View

class FriendChatPage : AppCompatActivity(),FriendChatAdapter.SelectionListener   {
    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var chatAdapter: FriendChatAdapter
    private val chatList = mutableListOf<FriendChat>()

    private lateinit var sendButton: Button
    private lateinit var messageInput: EditText
    private lateinit var friendNameTextView: TextView
    private lateinit var profileImageView: ImageView
    private lateinit var moreIcon: ImageView

    private lateinit var friendRoom: String
    private lateinit var currentUsername: String
    private lateinit var friendName: String
    private var profileImageIndex: Int = 0

    private val database = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend_chat_page)
        applySavedBackground()

        // Initialize UI elements
        chatRecyclerView = findViewById(R.id.chatRecyclerView)
        sendButton = findViewById(R.id.sendButton)
        messageInput = findViewById(R.id.messageInput)
        friendNameTextView = findViewById(R.id.senderName)
        profileImageView = findViewById(R.id.profileImage)
        moreIcon = findViewById(R.id.more)
        moreIcon.visibility = View.GONE // Hide by default

        // Get data from Intent
        currentUsername = intent.getStringExtra("CurrentUsername") ?: "Unknown"
        friendName = intent.getStringExtra("FRIEND_USERNAME") ?: "Unknown"
        friendRoom = intent.getStringExtra("FRIEND_ROOM") ?: ""
        profileImageIndex = intent.getIntExtra("FRIEND_PROFILE_IMAGE_INDEX", 0)

        friendNameTextView.text = friendName
        setProfileImage(profileImageIndex)

        // Initialize RecyclerView
        chatRecyclerView.layoutManager = LinearLayoutManager(this)
        chatAdapter = FriendChatAdapter(chatList, currentUsername, friendRoom, this,this)
        chatRecyclerView.adapter = chatAdapter

        // Fetch username if needed
        if (currentUsername == "Unknown") fetchCurrentUsername()

        loadMessages()

        sendButton.setOnClickListener { sendMessage() }

        moreIcon.setOnClickListener { view ->
            if (chatList.isNotEmpty()) {
                val lastMessage = chatList.last()
                chatAdapter.showPopupMenu(view, chatList.size - 1, )
            } else {
                Toast.makeText(this, "No messages available", Toast.LENGTH_SHORT).show()
            }
        }

        // Hide moreIcon when touching outside
        val parentLayout = findViewById<View>(R.id.main)
        parentLayout.setOnTouchListener { _, _ ->
            if (chatAdapter.isSelectionMode) {
                chatAdapter.clearSelection()
            }
            false
        }
    }
    override fun onSelectionStateChanged(isSelectionActive: Boolean) {
        toggleMoreIcon(isSelectionActive) // Hide/show more icon based on selection
    }


    fun toggleMoreIcon(isVisible: Boolean) {
        moreIcon.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    private fun fetchCurrentUsername() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userRef = database.child("users").child(uid)

        userRef.child("username").get().addOnSuccessListener { snapshot ->
            currentUsername = snapshot.value.toString()
        }.addOnFailureListener {
            Log.e("FriendChatPage", "Failed to fetch username", it)
        }
    }

    private fun loadMessages() {
        if (friendRoom.isEmpty()) {
            Toast.makeText(this, "Invalid chat room", Toast.LENGTH_SHORT).show()
            return
        }

        val messagesRef = database.child("friendRoom").child(friendRoom)
        messagesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                chatList.clear()
                for (messageSnapshot in snapshot.children) {
                    messageSnapshot.getValue(ChatMessage::class.java)?.let { chatMessage ->
                        val friendChat = FriendChat(
                            messageId = messageSnapshot.key ?: "",
                            messageText = chatMessage.messageText,
                            senderNickname = chatMessage.senderNickname,
                            timestamp = chatMessage.timestamp
                        )
                        chatList.add(friendChat)
                    }
                }
                chatAdapter.notifyDataSetChanged()
                chatRecyclerView.scrollToPosition(chatList.size - 1)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FriendChatPage", "Failed to load messages - ${error.message}")
            }
        })
    }

    private fun sendMessage() {
        val messageText = messageInput.text.toString().trim()
        if (messageText.isEmpty()) return

        sendButton.isEnabled = false

        val message = FriendChat(
            senderNickname = currentUsername,
            messageText = messageText,
            timestamp = System.currentTimeMillis()
        )

        database.child("friendRoom").child(friendRoom).push().setValue(message).addOnCompleteListener {
            if (it.isSuccessful) {
                messageInput.text.clear()
            } else {
                Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show()
            }
            sendButton.isEnabled = true
        }
    }

    private fun setProfileImage(index: Int) {
        val profileImages = listOf(
            R.drawable.profile, R.drawable.profile1, R.drawable.profile2,
            R.drawable.profile3, R.drawable.profile4, R.drawable.profile5
        )
        profileImageView.setImageResource(
            if (index in profileImages.indices) profileImages[index] else R.drawable.profile
        )
    }

    private fun applySavedBackground() {
        val sharedPref = getSharedPreferences("ChatSettings", MODE_PRIVATE)
        sharedPref.getString("chat_bg", null)?.let { loadBackgroundImage(Uri.parse(it)) }
    }

    private fun loadBackgroundImage(uri: Uri) {
        val chatLayout: ConstraintLayout = findViewById(R.id.main)

        Glide.with(this)
            .load(uri)
            .into(object : CustomTarget<Drawable>() {
                override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                    chatLayout.background = resource
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    chatLayout.setBackgroundResource(R.color.white)
                }
            })
    }

}
