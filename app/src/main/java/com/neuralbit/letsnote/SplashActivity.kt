package com.neuralbit.letsnote

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)



    }

    override fun onStart() {
        super.onStart()
        val mAuth = FirebaseAuth.getInstance()
        val firebaseUser = mAuth.currentUser

        if (firebaseUser!=null){
            val intent = Intent(this@SplashActivity,MainActivity::class.java)
            startActivity(intent)
        }else{
            val intent = Intent(this@SplashActivity,SignInActivity::class.java)
            startActivity(intent)
        }
    }

}