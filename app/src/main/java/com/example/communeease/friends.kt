package com.example.communeease

import FriendAdapter
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class Friends : AppCompatActivity() {
    private lateinit var setting: ImageView
    private  var friendimage:Int = 0
    private lateinit var homes: ImageView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FriendAdapter
    private val friendsList = mutableListOf<Friend>()

    private lateinit var currentUserId: String
    private lateinit var currentUsername: String
    private var profileImageIndex: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friends)
        // Retrieve user ID and username from intent
        currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        currentUsername = intent.getStringExtra("USER_USERNAME") ?: ""
        profileImageIndex = intent.getIntExtra("PROFILE_IMAGE_INDEX", 0)

        Log.d("Friends", "Received UserID: $currentUserId, Username: $currentUsername, ProfileImageIndex: $profileImageIndex")

        setting = findViewById(R.id.setting)
        homes = findViewById(R.id.homes)
        recyclerView = findViewById(R.id.rvFriendsList)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = FriendAdapter(friendsList) { friend ->
            navigateToChat(friend)
        }
        recyclerView.adapter = adapter

        setting.setOnClickListener {
            startActivity(Intent(this, Setting::class.java))
        }
        homes.setOnClickListener {
            startActivity(Intent(this, Home::class.java))
        }

        // Load friends using userId instead of username
        loadFriends()
        val searchBox = findViewById<EditText>(R.id.searchBox)

        searchBox.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // No action needed here
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No action needed here
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter(s.toString()) // Filter friends as user types
            }
        })

    }

    private fun loadFriends() {
        val database = FirebaseDatabase.getInstance().reference
        val friendsRef = database.child("users").child(currentUserId).child("friendList")

        val currentTime = System.currentTimeMillis() / 1000
        friendsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                friendsList.clear()

                if (!snapshot.exists()) {
                    Log.d("Friends", "No friends found in Firebase")
                    adapter.notifyDataSetChanged()
                    return
                }

                for (friend in snapshot.children) {
                    val friendId = friend.key ?: continue
                    val friendUsername = friend.child("friendUsername").getValue(String::class.java) ?: "Unknown"
                    val friendImageIndex = friend.child("friendImageIndex").getValue(Long::class.java)?.toInt() ?: 0
                    val friendRoom = friend.child("friendRoom").getValue(String::class.java) ?: ""
                    val expiresAt = friend.child("expiresAt").getValue(Long::class.java) ?: 0L
                    friendimage = friendImageIndex

                    Log.d("Friends", "Checking Friend: $friendUsername ($friendId) | friendRoom: $friendRoom")

                    if (friendRoom.isEmpty()) {
                        Log.e("Friends", "⚠️ FriendRoom is empty for $friendUsername ($friendId)")
                        continue
                    }

                    friendsList.add(Friend(friendId, friendUsername, friendImageIndex.toString(), friendRoom))
                }

                adapter.updateFriends(friendsList)
                Log.d("Friends", "Adapter updated. Total friends: ${friendsList.size}")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Friends", "❌ Firebase error: ${error.message}", error.toException())
                Toast.makeText(this@Friends, "Error loading friends", Toast.LENGTH_SHORT).show()
            }
        })
    }



    private fun navigateToChat(friend: Friend) {
        Log.d("Friends", "Navigating to chat with Current User: $currentUsername")
        val intent = Intent(this, FriendChatPage::class.java).apply {
            putExtra("CurrentUserId", currentUserId)
            putExtra("CurrentUsername",currentUsername)
            putExtra("FRIEND_ID", friend.friendId)
            putExtra("FRIEND_PROFILE_IMAGE_INDEX", friendimage)
            putExtra("FRIEND_USERNAME", friend.username)
            putExtra("FRIEND_ROOM", friend.friendRoom)
        }
        startActivity(intent)
    }
}
