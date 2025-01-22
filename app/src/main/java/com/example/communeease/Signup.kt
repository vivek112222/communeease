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

class Signup : AppCompatActivity() {

    private lateinit var edtEmail: EditText
    private lateinit var edtPassword: EditText
    private lateinit var btnVerify: Button
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_signup)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        mAuth=FirebaseAuth.getInstance()
        edtEmail = findViewById(R.id.email)
        edtPassword = findViewById(R.id.password)
        btnVerify = findViewById(R.id.register)

        btnVerify.setOnClickListener {
            val email=edtEmail.text.toString()
            val password = edtPassword.text.toString()

            sign(email,password);

        }
    }
    private fun sign(email:String ,password:String){

        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val intent=Intent(this@Signup, Login::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(this@Signup,"Some error occured",Toast.LENGTH_SHORT).show()
                }
            }
    }
}