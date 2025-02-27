package com.example.communeease

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
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
    private lateinit var friendsButton: LinearLayout
    private lateinit var settingsButton: LinearLayout
    private lateinit var notifyButton: LinearLayout
    private lateinit var sharedPreferences: SharedPreferences

    private var notificationsEnabled: Boolean = true
    private var profileImageIndex: Int = 0
    private var username: String = "Unknown"
    private var email: String = "Unknown"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        Log.d("HomeActivity", "onCreate: Activity started")

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("ChatAppPrefs", MODE_PRIVATE)

        // UI elements
        settingsButton = findViewById(R.id.setting)
        notifyButton = findViewById(R.id.notify)
        usernameTextView = findViewById(R.id.usernameTextView)
        emailTextView = findViewById(R.id.emailTextView)
        chatNowButton = findViewById(R.id.chatNowButton)
        friendsButton = findViewById(R.id.friendsButton)

        Log.d("HomeActivity", "onCreate: UI elements initialized")

        // Retrieve user details from Firebase
        fetchUserDetails()

        // Retrieve user details from SharedPreferences
        loadUserDetailsFromPreferences()

        chatNowButton.setOnClickListener {
            val intent = Intent(this, Waitingpage::class.java).apply {
                putExtra("USER_USERNAME", username)
                putExtra("PROFILE_IMAGE_INDEX", profileImageIndex)
            }
            Log.d("HomeActivity", "Navigating to WaitingPage with USER_USERNAME: $username, PROFILE_IMAGE_INDEX: $profileImageIndex")
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
            val userId = currentUser.uid

            Log.d("HomeActivity", "Fetching user details for userId: $userId")

            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        username = document.getString("username") ?: "No username found"
                        email = document.getString("email") ?: "No email found"
                        profileImageIndex = document.getLong("profileImage")?.toInt() ?: 0
                        notificationsEnabled = document.getBoolean("notificationsEnabled") ?: true  // Default to true if not found

                        Log.d("HomeActivity", "User details retrieved: username=$username, email=$email, profileImageIndex=$profileImageIndex, notificationsEnabled=$notificationsEnabled")
                        // Update UI
                        usernameTextView.text = "Welcome, $username"
                        emailTextView.text = "Email: $email"

                        // Save to SharedPreferences
                        saveUserDetailsToPreferences(username, email, profileImageIndex)
                    } else {
                        Log.e("HomeActivity", "User data not found in Firestore")
                        Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("HomeActivity", "Error retrieving user data from Firestore", e)
                    Toast.makeText(this, "Error retrieving user data", Toast.LENGTH_SHORT).show()
                }
        } else {
            Log.e("HomeActivity", "User is not authenticated")
            Toast.makeText(this, "User is not authenticated", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveUserDetailsToPreferences(username: String, email: String, profileImageIndex: Int) {
        val editor = sharedPreferences.edit()
        editor.putString("username", username)
        editor.putString("email", email)
        editor.putInt("profileImageIndex", profileImageIndex)
        editor.putBoolean("notificationsEnabled",notificationsEnabled)
        val success = editor.commit()  // Use commit() for debugging instead of apply()

        if (success) {
            Log.d("HomeActivity", "User details saved to SharedPreferences: username=$username, email=$email, profileImageIndex=$profileImageIndex,notificationEnabled=$notificationsEnabled")
        } else {
            Log.e("HomeActivity", "Failed to save user details to SharedPreferences")
        }
    }

    private fun loadUserDetailsFromPreferences() {
        username = sharedPreferences.getString("username", "Unknown") ?: "Unknown"
        email = sharedPreferences.getString("email", "Unknown") ?: "Unknown"
        profileImageIndex = sharedPreferences.getInt("profileImageIndex", 0)

        Log.d("HomeActivity", "User details loaded from SharedPreferences: username=$username, email=$email, profileImageIndex=$profileImageIndex")

        usernameTextView.text = "Welcome, $username"
        emailTextView.text = "Email: $email"
    }

    private fun navigateTo(destination: Class<*>) {
        val intent = Intent(this, destination)
        intent.putExtra("USER_USERNAME", username)
        intent.putExtra("PROFILE_IMAGE_INDEX", profileImageIndex)
        intent.putExtra("notificationsEnabled", notificationsEnabled)

        Log.d("HomeActivity", "Navigating to ${destination.simpleName} with USER_USERNAME: $username, PROFILE_IMAGE_INDEX: $profileImageIndex,notificationenabled=$notificationsEnabled")

        startActivity(intent)
    }
}
