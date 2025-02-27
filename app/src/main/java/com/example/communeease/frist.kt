package com.example.communeease

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class frist : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_frist) // Make sure this matches your XML file name

        val btnGetStarted: Button = findViewById(R.id.btn_get_started)
        val tvSignIn: TextView = findViewById(R.id.Signin)

        // Navigate to Signup Page when "Get Started" is clicked
        btnGetStarted.setOnClickListener {
            val intent = Intent(this, verify::class.java)
            startActivity(intent)
        }

        // Navigate to Signin Page when "Already have an account? Sign in" is clicked
        tvSignIn.setOnClickListener {
            val intent = Intent(this, Signin::class.java)
            startActivity(intent)
        }
    }
}
