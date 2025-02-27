package com.example.communeease

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat

class AccountSettings : AppCompatActivity() {


    private lateinit var homes:ImageView
    private lateinit var switchNotifications: SwitchCompat
    private lateinit var switchDarkMode: SwitchCompat
    private lateinit var btnDeleteAccount: Button
    private lateinit var account:TextView
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_accountsettings)

        // Find views
        switchNotifications = findViewById(R.id.switch_notifications)
        switchDarkMode = findViewById(R.id.switch_dark_mode)
        btnDeleteAccount = findViewById(R.id.btn_delete_account)
        account=findViewById(R.id.aboutus)
        homes =findViewById(R.id.home)

        // Set up notification switch
        homes.setOnClickListener {
            val intent=Intent(this,Home::class.java)
        }
        account.setOnClickListener{
            val intent=Intent(this,Aboutus::class.java)
            startActivity(intent)
        }
        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            // Save the notification setting (could be saved in Firebase or SharedPreferences)
            saveNotificationSetting(isChecked)
        }

        // Set up dark mode switch
        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            // Toggle dark mode
            toggleDarkMode(isChecked)
        }

        // Set up delete account button
        btnDeleteAccount.setOnClickListener {
            // Show confirmation dialog for deleting the account
            deleteAccount()
        }
    }

    // Function to save the notification setting (could be saved in Firebase)
    private fun saveNotificationSetting(isEnabled: Boolean) {
        val user = auth.currentUser
        user?.let {
            // Save this setting to Firebase or SharedPreferences
            FirebaseDatabase.getInstance().getReference("users").child(it.uid)
                .child("notificationsEnabled").setValue(isEnabled)
            Toast.makeText(this, "Notification settings updated", Toast.LENGTH_SHORT).show()
        }
    }

    // Function to toggle dark mode
    private fun toggleDarkMode(isDarkMode: Boolean) {
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        // Save this setting in SharedPreferences if needed
    }

    // Function to delete the user's account
    private fun deleteAccount() {
        val user = auth.currentUser
        user?.let {
            // First, delete the user's data from the Firebase Realtime Database
            FirebaseDatabase.getInstance().getReference("users").child(it.uid).removeValue()

            // Then, delete the user from Firebase Authentication
            it.delete().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Show success message
                    Toast.makeText(this, "Account deleted successfully", Toast.LENGTH_SHORT).show()

                    // After deletion, navigate to the login or start screen
                    val intent = Intent(this, Signin::class.java)
                    startActivity(intent)
                    finish() // Close the settings activity
                } else {
                    // Handle failure if deletion from Firebase Authentication fails
                    Toast.makeText(this, "Account deletion failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()

                    // Clear Firebase Authentication session (relogin required)
                    FirebaseAuth.getInstance().signOut()  // This signs the user out

                    // Show a dialog prompting the user to re-login
                    showReLoginDialog()
                }
            }
        }
    }

    private fun showReLoginDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Account deletion failed. Please re-login and try again.")
            .setPositiveButton("Re-login") { _, _ ->
                // Redirect to Signin screen for re-login
                val intent = Intent(this, Signin::class.java)
                startActivity(intent)
                finish() // Close the settings activity
            }
            .setCancelable(false) // Prevent dismissing dialog without action
            .show()
    }


}
