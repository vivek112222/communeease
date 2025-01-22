package com.example.communeease

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Waitingpage : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var currentUserId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_waitingpage)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        database = FirebaseDatabase.getInstance().reference
        currentUserId = generateUserId()

        addToWaitingList()
        checkForMatch()

    }
    private fun generateUserId(): String {
        return System.currentTimeMillis().toString() // Use a timestamp or unique identifier
    }
    private fun addToWaitingList() {
        val waitingListRef = database.child("waitingUsers").child(currentUserId)
        waitingListRef.setValue(true).addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(this, "Added to waiting list.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to add to waiting list.", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun checkForMatch() {
        val waitingListRef = database.child("waitingUsers")

        waitingListRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val waitingUsers = snapshot.children.map { it.key!! }

                // Check if there is another user to match with
                if (waitingUsers.size > 1) {
                    val partnerId = waitingUsers.first { it != currentUserId }
                    startChatWithPartner(partnerId)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Waitingpage, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun startChatWithPartner(partnerId: String) {
        // Remove both users from the waiting list
        database.child("waitingUsers").child(currentUserId).removeValue()
        database.child("waitingUsers").child(partnerId).removeValue()

        // Start the chat interface and pass partnerId to it
        val intent = Intent(this, random::class.java)
        intent.putExtra("PARTNER_ID", partnerId)
        startActivity(intent)
        finish() // Close the waiting page
    }
    override fun onDestroy() {
        super.onDestroy()
        // Remove the current user from the waiting list when leaving the page
        database.child("waitingUsers").child(currentUserId).removeValue()
    }
}