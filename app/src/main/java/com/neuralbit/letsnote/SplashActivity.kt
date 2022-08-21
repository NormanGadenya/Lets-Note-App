package com.neuralbit.letsnote

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

    }

    override fun onStart() {
        super.onStart()
        val mAuth = FirebaseAuth.getInstance()
        val firebaseUser = mAuth.currentUser
        val settingsPref : SharedPreferences = getSharedPreferences("Settings", MODE_PRIVATE)
        when (settingsPref.getString("mode","default")) {
            "Dark mode" -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            "Light mode" -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            else -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }

        if (firebaseUser!=null){
            val intent = Intent(this@SplashActivity,MainActivity::class.java)
            startActivity(intent)
        }else{
            val intent = Intent(this@SplashActivity,SignInActivity::class.java)
            startActivity(intent)
        }
    }

}