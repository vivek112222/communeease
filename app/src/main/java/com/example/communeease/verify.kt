package com.example.communeease

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.random.Random

class verify : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var registerButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_verify)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()

        // Initialize UI elements
        emailInput = findViewById(R.id.email)
        passwordInput = findViewById(R.id.password)
        registerButton = findViewById(R.id.register)

        registerButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                registerUser(email, password)
            } else {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun registerUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Registration successful
                    val userId = auth.currentUser?.uid ?: return@addOnCompleteListener

                    // Generate random username and profile image
                    val randomUsername = "User${Random.nextInt(1000, 9999)}"
                    val randomProfileImage = Random.nextInt(1, 6)  // Random profile image from 1 to 5

                    // Default personality description
                    val defaultDescription = "A user of CommuneEase."

                    // Create an empty friends list
                    val friends = hashMapOf<String, Boolean>()

                    // Create user object
                    val userMap = hashMapOf(
                        "username" to randomUsername,
                        "email" to email,
                        "profileImage" to randomProfileImage,
                        "friends" to friends,  // Empty friends collection
                        "description" to defaultDescription // Add default description
                    )

                    // Get Firestore instance and store user data
                    val db = FirebaseFirestore.getInstance()
                    db.collection("users").document(userId)
                        .set(userMap)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Verification email sent!", Toast.LENGTH_SHORT).show()

                            // Send email verification
                            auth.currentUser?.sendEmailVerification()

                            // Navigate to Home Activity after email verification
                            val intent = Intent(this, Home::class.java) // Replace with your home page activity
                            startActivity(intent)
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Failed to save user data: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    // Registration failed
                    Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
