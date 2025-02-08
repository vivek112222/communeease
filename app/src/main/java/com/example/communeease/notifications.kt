package com.example.communeease

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class notifications : AppCompatActivity() {
    private lateinit var setting:ImageView
    private lateinit var homes:ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_notifications)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setting =findViewById(R.id.setting)
        homes = findViewById(R.id.homes)

        setting.setOnClickListener {
            val intent = Intent(this, com.example.communeease.Setting::class.java)
            startActivity((intent))
        }
        homes.setOnClickListener {
            val intent = Intent(this, Home::class.java)
            startActivity((intent))
        }
    }
}