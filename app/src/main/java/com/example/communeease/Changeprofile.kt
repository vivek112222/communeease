package com.example.communeease

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Changeprofile : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var profileImageView: ImageView
    private lateinit var usernameEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var viewPager: ViewPager2

    // List of profile images
    private val profileImages = listOf(
        R.drawable.profile1, R.drawable.profile2, R.drawable.profile3,
        R.drawable.profile4, R.drawable.profile5
    )

    private var selectedProfileIndex = 0  // Index of selected profile image

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_changeprofile)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Find Views
        profileImageView = findViewById(R.id.profileImageView)
        usernameEditText = findViewById(R.id.usernameEditText)
        saveButton = findViewById(R.id.saveButton)
        viewPager = findViewById(R.id.viewPager)

        // Load user data
        loadUserProfile()

        // Set up ViewPager for profile image selection
        val adapter = ImageAdapter(profileImages)
        viewPager.adapter = adapter

        // Handle profile image selection (update the profileImageView)
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                selectedProfileIndex = position
                profileImageView.setImageResource(profileImages[position]) // Update the main profile image
            }
        })

        // Save button action
        saveButton.setOnClickListener {
            saveProfileChanges()
            val intent = Intent(this, Profile::class.java)
            startActivity(intent)
        }
    }

    private fun loadUserProfile() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val username = document.getString("username") ?: "Unknown"
                    val profileImageIndex = document.getLong("profileImage")?.toInt() ?: 0

                    usernameEditText.setText(username) // Set current username
                    profileImageView.setImageResource(profileImages[profileImageIndex]) // Set current profile image
                    viewPager.setCurrentItem(profileImageIndex, false) // Show current profile image in ViewPager
                    selectedProfileIndex = profileImageIndex
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load profile.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveProfileChanges() {
        val userId = auth.currentUser?.uid ?: return
        val newUsername = usernameEditText.text.toString().trim()

        if (newUsername.isEmpty()) {
            Toast.makeText(this, "Username cannot be empty!", Toast.LENGTH_SHORT).show()
            return
        }

        val userUpdates = mapOf(
            "username" to newUsername,
            "profileImage" to selectedProfileIndex
        )

        db.collection("users").document(userId)
            .update(userUpdates)
            .addOnSuccessListener {
                Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                finish() // Close activity and go back
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to update profile.", Toast.LENGTH_SHORT).show()
            }
    }
}
