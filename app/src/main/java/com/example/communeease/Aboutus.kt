package com.example.communeease

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Aboutus : AppCompatActivity() {
    private lateinit var backbutton: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge layout
        enableEdgeToEdge()

        // Set content view
        setContentView(R.layout.activity_aboutus)

        // Set up window insets listener for padding adjustments
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize the back button and set its click listener
        backbutton = findViewById(R.id.back)
        backbutton.setOnClickListener {
            startActivity(Intent(this, Setting::class.java))
        }

        // Key Features section
        val keyFeaturesHeader: TextView = findViewById(R.id.keyFeaturesHeader)
        val keyFeaturesContent: TextView = findViewById(R.id.keyFeaturesContent)
        keyFeaturesHeader.setOnClickListener {
            toggleVisibility(keyFeaturesContent)
        }

        // How It Works section
        val howItWorksHeader: TextView = findViewById(R.id.howItWorksHeader)
        val howItWorksContent: TextView = findViewById(R.id.howItWorksContent)
        howItWorksHeader.setOnClickListener {
            toggleVisibility(howItWorksContent)
        }

        // Community Guidelines section
        val communityGuidelinesHeader: TextView = findViewById(R.id.communityGuidelinesHeader)
        val communityGuidelinesContent: TextView = findViewById(R.id.communityGuidelinesContent)
        communityGuidelinesHeader.setOnClickListener {
            toggleVisibility(communityGuidelinesContent)
        }

        // Privacy & Security section
        val privacySecurityHeader: TextView = findViewById(R.id.privacySecurityHeader)
        val privacySecurityContent: TextView = findViewById(R.id.privacySecurityContent)
        privacySecurityHeader.setOnClickListener {
            toggleVisibility(privacySecurityContent)
        }
    }

    // Function to toggle visibility of content
    private fun toggleVisibility(contentView: TextView) {
        if (contentView.visibility == View.VISIBLE) {
            contentView.visibility = View.GONE
        } else {
            contentView.visibility = View.VISIBLE
        }
    }
}
