package com.example.communeease

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Home : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val chatnow =findViewById<Button>(R.id.chatNowButton)
        val friends =findViewById<Button>(R.id.friendsButton)
        chatnow.setOnClickListener {
            val intent= Intent(this ,Waitingpage::class.java)
            startActivity(intent)
        }
        friends.setOnClickListener {
            val intent= Intent(this ,friends::class.java)
            startActivity(intent)
        }

    }
}