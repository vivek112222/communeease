package com.example.communeease

import com.example.communeease.ChatAdapter
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import com.bumptech.glide.request.transition.Transition
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.google.api.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class random : AppCompatActivity() {
    private lateinit var SenderName: TextView
    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var messageInput: EditText
    private lateinit var nextButton: Button
    private lateinit var sendButton: Button
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var database: DatabaseReference
    private lateinit var currentUserNickname: String
    private lateinit var partnerId: String
    private lateinit var chatRoomRef: DatabaseReference
    private lateinit var chatRoomId: String
    private lateinit var addFriendButton: Button
    private lateinit var addedImageView: ImageView
    private val chatMessages = mutableListOf<ChatMessage>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_random)
        applySavedBackground()


        nextButton = findViewById(R.id.next)
        addFriendButton = findViewById(R.id.addfriend)
        addedImageView = findViewById(R.id.addedImageView)
        SenderName = findViewById(R.id.senderName)
        chatRecyclerView = findViewById(R.id.chatRecyclerView)
        messageInput = findViewById(R.id.messageInput)
        sendButton = findViewById(R.id.sendButton)

        val username = intent.getStringExtra("USER_USERNAME") ?: ""

        val currentImageIndex = intent.getIntExtra("PROFILE_IMAGE_INDEX", 0)
        currentUserNickname = username
        partnerId = intent.getStringExtra("PARTNER_USERNAME") ?: ""
        val chatRoomName = intent.getStringExtra("CHAT_ROOM_NAME") ?: "Unknown"
        val partnerProfileImageIndex = intent.getStringExtra("PARTNER_PROFILE_IMAGE_INDEX") ?: "0"

        Log.d(
            "ChatDebug",
            "Received Intent in random.kt. ChatRoom: $chatRoomName, Partner Username: $partnerId"
        )

        val partnerProfileImage: ImageView = findViewById(R.id.profileImage)
        val profileResId = getProfileImageResource(partnerProfileImageIndex)
        partnerProfileImage.setImageResource(profileResId)

        chatRoomId = chatRoomName
        SenderName.text = partnerId
        chatRoomRef = FirebaseDatabase.getInstance().getReference("chatRooms").child(chatRoomId)

        chatRoomRef.child("users").child(currentUserNickname).setValue("active")

        chatRecyclerView.layoutManager = LinearLayoutManager(this)
        chatAdapter = ChatAdapter(chatMessages, currentUserNickname, this)

        chatRecyclerView.adapter = chatAdapter

        checkFriendRequestStatus()

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
        listenForUserStatusChanges()

        nextButton.setOnClickListener {
            val intent = Intent(this, Endpage::class.java)
            startActivity(intent)
        }

        addFriendButton.setOnClickListener {
            Log.d("FriendRequest", "🔵 Add Friend button clicked")
            sendFriendRequest()
        }


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

    private fun checkFriendRequestStatus() {
        val partnerUserId = intent.getStringExtra("OPPONENT_USER_ID") ?: return
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val friendRequestRef = FirebaseDatabase.getInstance()
            .getReference("notifications/$partnerUserId/friendRequests/$currentUserId")

        friendRequestRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Request already sent - hide button and show "added" status
                    addFriendButton.visibility = View.GONE
                    addedImageView.visibility = View.VISIBLE
                } else {
                    // No request yet - show button
                    addFriendButton.visibility = View.VISIBLE
                    addedImageView.visibility = View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FriendRequest", "❌ Failed to check friend request", error.toException())
            }
        })
    }


    private fun sendFriendRequest() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        val partnerUserId = intent.getStringExtra("OPPONENT_USER_ID")  // Ensure correct key
        val currentUserName = intent.getStringExtra("USER_USERNAME") ?: ""
        val partnerUserName = intent.getStringExtra("PARTNER_USERNAME") ?: ""
        val currentImageIndex = intent.getIntExtra("PROFILE_IMAGE_INDEX", 0)
        val partnerImageIndex = intent.getStringExtra("PARTNER_PROFILE_IMAGE_INDEX") ?: "0"

        if (currentUserId.isNullOrEmpty() || partnerUserId.isNullOrEmpty()) {
            Log.e(
                "FriendRequest",
                "❌ Missing user data: currentUserId=$currentUserId, partnerUserId=$partnerUserId"
            )
            return
        }

        Log.d(
            "FriendRequest",
            "✅ Sending request from $currentUserId ($currentUserName) to $partnerUserId ($partnerUserName)"
        )

        val notificationRef = FirebaseDatabase.getInstance()
            .getReference("notifications/$partnerUserId/friendRequests/$currentUserId")

        val requestData = mapOf(
            "senderId" to currentUserId,
            "senderUsername" to currentUserName,
            "senderProfileImage" to currentImageIndex.toString(),
            "receiverId" to partnerUserId,
            "receiverUsername" to partnerUserName,
            "receiverProfileImage" to partnerImageIndex,
            "status" to "pending",
            "timestamp" to System.currentTimeMillis()
        )

        Log.d("FriendRequest", "📝 Writing to Firebase: $requestData")

        notificationRef.setValue(requestData)
            .addOnSuccessListener {
                Log.d("FriendRequest", "✅ Friend request sent successfully to $partnerUserId")
                runOnUiThread {
                    addFriendButton.visibility = View.GONE
                    addedImageView.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener {
                Log.e("FriendRequest", "❌ Failed to send friend request", it)
            }
    }


    private fun markUserLeft() {
        chatRoomRef.child("users").child(currentUserNickname).setValue("left")
            .addOnCompleteListener {
                checkAndDeleteChatRoom()
            }
    }

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
    private fun applySavedBackground() {
        val sharedPref = getSharedPreferences("ChatSettings", android.content.Context.MODE_PRIVATE)
        val savedUri = sharedPref.getString("chat_bg", null)

        if (savedUri != null) {
            loadBackgroundImage(Uri.parse(savedUri))
        }
    }
    private fun loadBackgroundImage(uri: Uri) {
        val chatLayout: ConstraintLayout = findViewById(R.id.main) // Ensure correct ID

        Glide.with(this)
            .load(uri)
            .into(object : CustomTarget<Drawable>() {
                override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                    chatLayout.background = resource
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    // Optional: Set a default background if image is cleared
                    chatLayout.setBackgroundResource(R.color.white)
                }
            })
    }
    private fun listenForUserStatusChanges() {
        chatRoomRef.child("users").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var otherUserLeft = false

                for (user in snapshot.children) {
                    val username = user.key
                    val status = user.getValue(String::class.java) ?: "active"

                    if (username != currentUserNickname && status == "left") {
                        otherUserLeft = true
                        break
                    }
                }

                runOnUiThread {
                    if (otherUserLeft) {
                        showWarningText("Your chat partner has left.")
                    } else {
                        hideWarningText()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ChatRoom", "Failed to listen for user status changes", error.toException())
            }
        })
    }

    private fun showWarningText(message: String) {
        val warningText: TextView = findViewById(R.id.warningText)
        warningText.text = message
        warningText.visibility = View.VISIBLE
    }

    private fun hideWarningText() {
        val warningText: TextView = findViewById(R.id.warningText)
        warningText.visibility = View.GONE
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
