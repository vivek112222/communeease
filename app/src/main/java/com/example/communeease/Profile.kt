package com.example.communeease

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Profile : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var usernameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var profileImageView: ImageView
    private lateinit var home: ImageView
    private lateinit var settings: ImageView
    private lateinit var changeButton: Button

    // List of drawable profile images
    private val profileImages = listOf(
        R.drawable.profile,R.drawable.profile1, R.drawable.profile2, R.drawable.profile3,
        R.drawable.profile4, R.drawable.profile5
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)
        loadNotificationStatus()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Find Views
        home=findViewById(R.id.homes)
        settings=findViewById(R.id.setting)
        usernameTextView = findViewById(R.id.username)
        emailTextView = findViewById(R.id.emailText)
        profileImageView = findViewById(R.id.profileImage)
        changeButton = findViewById(R.id.changeprofile)

        // Load user profile data
        loadUserProfile()

        home.setOnClickListener {
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
        }
        settings.setOnClickListener {
            val intent = Intent(this, Setting::class.java)
            startActivity(intent)
        }
        changeButton.setOnClickListener {
            val intent = Intent(this, Changeprofile::class.java)
            startActivity(intent)
        }
    }
    private fun loadNotificationStatus() {
        val sharedPref = getSharedPreferences("ChatAppPrefs", Context.MODE_PRIVATE)
        val isEnabled = sharedPref.getBoolean("notificationsEnabled", false) // Default is false

        val notificationTextView: TextView = findViewById(R.id.notification)
        notificationTextView.text = if (isEnabled) "Notifications: Enabled" else "Notifications: Disabled"
    }


    private fun loadUserProfile() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val username = document.getString("username") ?: "Unknown"
                    val email = document.getString("email") ?: "No email"
                    val profileImageIndex = document.getLong("profileImage")?.toInt() ?: 0

                    // Update UI
                    usernameTextView.text = "$username"
                    emailTextView.text = "${maskEmail(email)}"
                    profileImageView.setImageResource(profileImages.getOrElse(profileImageIndex) { R.drawable.profile1 })
                } else {
                    Toast.makeText(this, "User data not found!", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load profile: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Function to mask email (Show first 4 and last 10 characters)
    private fun maskEmail(email: String): String {
        if (email.length <= 14) return email // Show full email if too short

        val firstPart = email.take(4) // First 4 letters
        val lastPart = email.takeLast(10) // Last 10 letters
        return "$firstPart***$lastPart" // Masked email format
    }
    private fun saveNotificationStatus(enabled: Boolean) {
        val sharedPref = getSharedPreferences("ChatSettings", Context.MODE_PRIVATE)
        sharedPref.edit().putBoolean("notificationsEnabled", enabled).apply()
    }

}
