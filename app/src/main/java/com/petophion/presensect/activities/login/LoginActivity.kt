package com.petophion.presensect.activities.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.petophion.presensect.R
import com.petophion.presensect.activities.main.MainActivity
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    var emailEditText: EditText? = null
    var passwordEditText: EditText? = null

    var email: String? = null
    var password: String? = null

    val auth = Firebase.auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE);//will hide the title
        getSupportActionBar()?.hide(); //hide the title bar
        setContentView(R.layout.activity_login)

        emailEditText = findViewById(R.id.etEmail)
        passwordEditText = findViewById(R.id.etPassword)

        btnLogin.setOnClickListener {
            email = emailEditText?.text.toString()
            password = passwordEditText?.text.toString()

            if (!email.equals("") && !password.equals("")){
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            Log.d("Login", "signInWithEmail:success")
                            logIn()
                        }
                    }.addOnFailureListener {
                        Toast.makeText(
                            baseContext, "Login Failed! ${it.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
            } else if ((email.equals("") && password.equals(""))) {
                Toast.makeText(this, "Please enter your email and password", Toast.LENGTH_LONG).show()
            } else if (email.equals("")) {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_LONG).show()
            } else if (password.equals("")) {
                Toast.makeText(this, "Please enter your password", Toast.LENGTH_LONG).show()
            }


        }

        btnSignup.setOnClickListener {
            val userSignupFragment = CreateUserFragment()

            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.clLoginLayout, userSignupFragment, "Signup")
            transaction.commit()
            transaction.addToBackStack("Signup")
        }
    }

    private fun logIn() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}