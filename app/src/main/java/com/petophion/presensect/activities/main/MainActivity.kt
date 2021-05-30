package com.petophion.presensect.activities.main

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.Window
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.petophion.presensect.R
import com.petophion.presensect.activities.login.LoginActivity

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE) //will hide the title
        supportActionBar?.hide() //hide the title bar
        setContentView(R.layout.activity_main)

        val navView: BottomNavigationView = findViewById(R.id.navView)

        val navController = findNavController(R.id.navHostFragment)

        val appBarConfig = AppBarConfiguration(setOf(
            R.id.navigation_feed,
            R.id.navigation_new_post,
            R.id.navigation_profile))
        setupActionBarWithNavController(navController, appBarConfig)
        navView.setupWithNavController(navController)

        checkLoggedIn()

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.app_bar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.mi_logout -> confirmLogout()
        }
        return true
    }

    private fun confirmLogout() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Are you sure you want to logout?")
            .setCancelable(true)
            .setPositiveButton("Yes") { dialog, id ->
                Firebase.auth.signOut()
                reLogIn()
            }
            .setNegativeButton("No") { dialog, id ->
                dialog.dismiss()
            }
        val alert = builder.create()
        alert.show()
    }

    private fun checkLoggedIn() {
        if (FirebaseAuth.getInstance().currentUser == null) {
            reLogIn()
        }
    }

    private fun reLogIn() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}