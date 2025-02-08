package com.example.communeease

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Endpage : AppCompatActivity() {
    private lateinit var next:Button
    private lateinit var settings:Button
    private lateinit var hom:Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_endpage)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        hom=findViewById(R.id.settingsButton)
        settings=findViewById(R.id.homeButton)
        next=findViewById(R.id.nextchat)
        next.setOnClickListener {
            val intent = Intent(this, Waitingpage::class.java)
            startActivity(intent)
        }
        hom.setOnClickListener {
            val intent =Intent(this,Home::class.java)
            startActivity(intent)
        }
        settings.setOnClickListener {
            startActivity(Intent(this,Setting::class.java))
        }

    }
}