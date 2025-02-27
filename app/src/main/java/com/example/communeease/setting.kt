package com.example.communeease

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import androidx.appcompat.widget.SwitchCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.firebase.firestore.FirebaseFirestore

class Setting : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var home: ImageView
    private lateinit var profile: LinearLayout
    private lateinit var logout: LinearLayout
    private lateinit var presssback: ImageView
    private lateinit var profileImage: ImageView
    private lateinit var profileName: TextView
    private lateinit var about:LinearLayout
    private lateinit var email: TextView
    private lateinit var switchNotifications: SwitchCompat
    private lateinit var btnDeleteAccount: Button
    private lateinit var btnSelectBackground: LinearLayout
    private val auth = FirebaseAuth.getInstance()
    private val PICK_IMAGE_REQUEST = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings2)

        // Apply edge-to-edge layout
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Find UI elements
        about=findViewById(R.id.aboutus)
        presssback = findViewById(R.id.back)
        home = findViewById(R.id.home)
        profile = findViewById(R.id.profile)
        logout = findViewById(R.id.logout)

        profileImage = findViewById(R.id.profileim)
        profileName = findViewById(R.id.profilename)
        email = findViewById(R.id.email)


        switchNotifications = findViewById(R.id.switch_notifications)
        btnDeleteAccount = findViewById(R.id.btn_delete_account)
        btnSelectBackground = findViewById(R.id.backgroundimage)

        val sharedPref = getSharedPreferences("ChatAppPrefs", Context.MODE_PRIVATE)

        val username = sharedPref.getString("username", "Unknown User") ?: "Unknown User"
        val profileImageIndex = sharedPref.getInt("profileImageIndex", 0)
        val isNotificationEnabled = sharedPref.getBoolean("notificationsEnabled", true);
        switchNotifications.setChecked(isNotificationEnabled);


        profileName.text = username
        email.text = FirebaseAuth.getInstance().currentUser?.email ?: "No Email Linked"
        setProfileImage(profileImageIndex)


        btnSelectBackground.setOnClickListener {
            openGallery()
        }

        // Button Click Listeners
        about.setOnClickListener{
            startActivity(Intent(this,Aboutus::class.java))
        }
        presssback.setOnClickListener {
            startActivity(Intent(this, Home::class.java))
        }

        home.setOnClickListener {
            startActivity(Intent(this, Home::class.java))
        }

        profile.setOnClickListener {
            startActivity(Intent(this, Profile::class.java))
        }

        logout.setOnClickListener {
            logoutUser()
        }

        // Notification switch logic
        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            saveNotificationSetting(isChecked)
        }

        // Commenting out the dark mode logic temporarily
        // Dark mode switch logic
        // switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
        //     toggleDarkMode(isChecked)
        // }

        // Delete account button logic
        btnDeleteAccount.setOnClickListener {
            showDeleteAccountDialog()
        }
    }
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            val imageUri: Uri? = data.data
            if (imageUri != null) {
                saveImageUri(imageUri.toString()) // Save selected image URI
                Toast.makeText(this, "Background updated!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Save image URI in SharedPreferences
    private fun saveImageUri(uri: String) {
        val sharedPref = getSharedPreferences("ChatSettings", Context.MODE_PRIVATE)
        sharedPref.edit().putString("chat_bg", uri).apply()
    }

    // Function to set profile image using index
    private fun setProfileImage(index: Int) {
        val images = arrayOf(
            R.drawable.profile,
            R.drawable.profile1,  // Example profile images
            R.drawable.profile2,
            R.drawable.profile3,
            R.drawable.profile4,
            R.drawable.profile5
        )

        if (index in images.indices) {
            profileImage.setImageResource(images[index])
        } else {
            profileImage.setImageResource(R.drawable.profile)
        }
    }

    // Function to save the notification setting
    private fun saveNotificationSetting(isEnabled: Boolean) {
        val user = auth.currentUser
        user?.let {
            val userRef = FirebaseFirestore.getInstance().collection("users").document(it.uid)

            userRef.update("notificationsEnabled", isEnabled)
                .addOnSuccessListener {
                    Toast.makeText(this, "Notification settings updated", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to update notification settings: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }


    // Function to delete the user's account
    private fun showDeleteAccountDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Are you sure you want to delete your account?")
            .setPositiveButton("Yes") { _, _ ->
                deleteAccount()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun deleteAccount() {
        val user = auth.currentUser
        user?.let {
            FirebaseDatabase.getInstance().getReference("users").child(it.uid).removeValue()

            it.delete().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Account deleted successfully", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, Signin::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Account deletion failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    FirebaseAuth.getInstance().signOut()
                    showReLoginDialog()
                }
            }
        }
    }

    // Dialog to prompt re-login if account deletion fails
    private fun showReLoginDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Account deletion failed. Please re-login and try again.")
            .setPositiveButton("Re-login") { _, _ ->
                val intent = Intent(this, Signin::class.java)
                startActivity(intent)
                finish()
            }
            .setCancelable(false)
            .show()
    }

    // Logout user and navigate to Signin screen
    private fun logoutUser() {
        FirebaseAuth.getInstance().signOut()
        Toast.makeText(this, "Logged out successfully!", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, frist::class.java))
        finish()
    }
    private fun loadBackgroundImage(uri: Uri) {
        val wallpaperLayout: ImageView = findViewById(R.id.wallpaper)

        Glide.with(this)
            .load(uri)
            .into(object : CustomTarget<Drawable>() {
                override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                    wallpaperLayout.background = resource  // ✅ Set the image as the background
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    wallpaperLayout.setBackgroundResource(R.color.white)  // ✅ Set fallback background
                }
            })
    }


}
