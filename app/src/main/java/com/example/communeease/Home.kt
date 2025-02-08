package com.example.communeease

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Home : AppCompatActivity() {

    private lateinit var usernameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var chatNowButton: Button
    private lateinit var friendsButton: Button
    private lateinit var settingsButton: ImageView
    private lateinit var notifyButton: ImageView

    private var profileImageIndex: Int = 0 // Store profile image index
    private var username: String = "Unknown" // Store username

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        // Initialize views
        settingsButton = findViewById(R.id.setting)
        notifyButton = findViewById(R.id.notify)
        usernameTextView = findViewById(R.id.usernameTextView)
        emailTextView = findViewById(R.id.emailTextView) // Ensure ImageView is in XML
        chatNowButton = findViewById(R.id.chatNowButton)
        friendsButton = findViewById(R.id.friendsButton)

        // Fetch user details including profile image
        fetchUserDetails()

        // Set click listeners for navigation
        chatNowButton.setOnClickListener {
            val intent = Intent(this, Waitingpage::class.java).apply {
                putExtra("USER_USERNAME", username)  // Pass username
                putExtra("PROFILE_IMAGE_INDEX", profileImageIndex)  // Pass image index
            }
            startActivity(intent)
        }

        friendsButton.setOnClickListener {
            navigateTo(Friends::class.java)
        }
        settingsButton.setOnClickListener {
            navigateTo(Setting::class.java)
        }
        notifyButton.setOnClickListener {
            navigateTo(notifications::class.java)
        }
    }

    private fun fetchUserDetails() {
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()

        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid  // Get UID from FirebaseAuth

            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        username = document.getString("username") ?: "No username found"
                        val email = document.getString("email") ?: "No email found"
                        profileImageIndex = document.getLong("profileImage")?.toInt() ?: 0

                        // Update UI
                        usernameTextView.text = "Welcome, $username"
                        emailTextView.text = "Email: $email"

                    } else {
                        Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error retrieving user data", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "User is not authenticated", Toast.LENGTH_SHORT).show()
        }
    }



    private fun navigateTo(destination: Class<*>) {
        val intent = Intent(this, destination)
        intent.putExtra("USER_USERNAME", username)
        intent.putExtra("PROFILE_IMAGE_INDEX", profileImageIndex) // Pass profile image index
        Log.d("IntentDebug", "Sending Intent - N  ProfileImageIndex: $profileImageIndex")
        startActivity(intent)
    }
}
