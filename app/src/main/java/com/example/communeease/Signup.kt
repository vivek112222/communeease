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
import java.security.MessageDigest

class Signup : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var usernameInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var confirmPasswordInput: EditText
    private lateinit var completeSignupButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_signup)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        usernameInput = findViewById(R.id.username)
        passwordInput = findViewById(R.id.password)
        confirmPasswordInput = findViewById(R.id.confirmPassword)
        completeSignupButton = findViewById(R.id.completeSignup)

        val email = intent.getStringExtra("email")

        completeSignupButton.setOnClickListener {
            val username = usernameInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            val confirmPassword = confirmPasswordInput.text.toString().trim()

            if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || email == null) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val hashedPassword = hashPassword(password)
            saveUserDetails(email, username, hashedPassword)
        }
    }

    private fun saveUserDetails(email: String, username: String, hashedPassword: String) {
        val userId = auth.currentUser?.uid ?: return
        val userMap = hashMapOf(
            "email" to email,
            "username" to username,
            "password" to hashedPassword // Store hashed password
        )

        db.collection("users").document(userId).set(userMap)
            .addOnSuccessListener {
                Toast.makeText(this, "User details saved!", Toast.LENGTH_SHORT).show()
                // Navigate to SignIn page
                val intent = Intent(this, Signin::class.java)
                startActivity(intent)
                finish() // Close the signup activity
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error saving details: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }


    // Function to hash password using SHA-256
    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(password.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) } // Convert to hex string
    }
}
