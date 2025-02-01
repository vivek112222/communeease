package com.example.communeease

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Home : AppCompatActivity() {

    private lateinit var usernameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var chatNowButton: Button
    private lateinit var friendsButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        // Initialize views
        usernameTextView = findViewById(R.id.usernameTextView)
        emailTextView = findViewById(R.id.emailTextView)
        chatNowButton = findViewById(R.id.chatNowButton)
        friendsButton = findViewById(R.id.friendsButton)

        // Get the current logged-in user's UID
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        if (currentUserId != null) {
            // Fetch user details from Firestore
            fetchUserDetails(currentUserId)
        } else {
            Toast.makeText(this, "User is not authenticated", Toast.LENGTH_SHORT).show()
        }
        chatNowButton.setOnClickListener {
            val username = usernameTextView.text.toString().removePrefix("Welcome, ")

            // Pass the username to the Waiting page
            val intent = Intent(this, Waitingpage::class.java)
            intent.putExtra("USER_USERNAME", username)
            startActivity(intent)
        }
        friendsButton.setOnClickListener {
            val username = usernameTextView.text.toString() // Retrieve the username from the TextView
            val intent = Intent(this, Friends::class.java)
            intent.putExtra("USER_USERNAME", username) // Pass the username to the Friends page
            startActivity(intent)
        }
    }

    private fun fetchUserDetails(userId: String) {
        val db = FirebaseFirestore.getInstance()

        // Retrieve user data from Firestore using the userId
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val username = document.getString("username") ?: "No username found"
                    val email = document.getString("email") ?: "No email found"

                    usernameTextView.text = "Welcome, $username"
                    emailTextView.text = "Email: $email"
                } else {
                    Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error retrieving user data", Toast.LENGTH_SHORT).show()
            }
    }
}
