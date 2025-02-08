package com.example.communeease

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

class Setting : AppCompatActivity() {
    private lateinit var home: ImageView
    private lateinit var profile: TextView
    private lateinit var logout: TextView  // Declare logout TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_setting)

        // Apply edge-to-edge layout
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        home = findViewById(R.id.home)
        profile = findViewById(R.id.profile)
        logout = findViewById(R.id.logout) // Initialize logout TextView


        home.setOnClickListener {
            startActivity(Intent(this, Home::class.java))
        }

        profile.setOnClickListener {
            startActivity(Intent(this, Profile::class.java))
        }


        logout.setOnClickListener {
            startActivity(Intent(this, Signin::class.java))
        }
    }

    private fun logoutUser() {
        FirebaseAuth.getInstance().signOut()

        Toast.makeText(this, "Logged out successfully!", Toast.LENGTH_SHORT).show()

        startActivity(Intent(this, Signin::class.java)) // Replace with your login screen
        finish()
    }
}
