package com.neuralbit.letsnote.ui.signIn

import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.WindowManager
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.neuralbit.letsnote.R
import com.neuralbit.letsnote.ui.main.MainActivity
import kotlinx.coroutines.launch

class SignInActivity : AppCompatActivity() {
    private var mGoogleSignInClient: GoogleSignInClient? = null
    private lateinit var mAuth: FirebaseAuth
    private var firebaseUser : FirebaseUser? = null
    private lateinit var progressBar : ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAuth = FirebaseAuth.getInstance()
        firebaseUser = mAuth.currentUser


        val settingsPref : SharedPreferences = getSharedPreferences("Settings", MODE_PRIVATE)
        val useLocalStorage = settingsPref.getBoolean("useLocalStorage",false)
        if (firebaseUser!=null || useLocalStorage){
            val intent = Intent(this@SignInActivity, MainActivity::class.java)
            startActivity(intent)
        }
        lifecycleScope.launch {
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
        }
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.statusBarColor = getColor(R.color.gunmetal)

        setContentView(R.layout.activity_sign_in)

        createRequest()
        progressBar = findViewById(R.id.sign_in_progress_bar)

        findViewById<View>(R.id.signInWithGoogleBtn).setOnClickListener { signInGoogle() }
        findViewById<View>(R.id.signInWithAnnoneBtn).setOnClickListener {
            val editor :SharedPreferences.Editor = settingsPref.edit()
            editor.putBoolean("useLocalStorage", true)
            editor.apply()
            val intent = Intent(this@SignInActivity, MainActivity::class.java)
            intent.putExtra("Signed in", true)
            startActivity(intent)
//            signInAnon()
        }
        val termsAndConditions = findViewById<View>(R.id.termsAndConditionTV)
        termsAndConditions.setOnClickListener {
            val i = Intent(applicationContext, TermsAndConditions::class.java)
            startActivity(i)
        }
        val privacyPolicyTV = findViewById<View>(R.id.privacyPolicyTv)
        privacyPolicyTV.setOnClickListener {
            val i = Intent(applicationContext, PrivacyPolicy::class.java)
            startActivity(i)
        }

    }

    private fun createRequest() {

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()


        mGoogleSignInClient = GoogleSignIn.getClient(applicationContext, gso)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        val a = Intent(Intent.ACTION_MAIN)
        a.addCategory(Intent.CATEGORY_HOME)
        a.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(a)
    }

    private fun signInGoogle() {
        if (!isNetworkConnected()){
            Toast.makeText(applicationContext,resources.getString(R.string.no_internet_connection_init),Toast.LENGTH_SHORT).show()
        }else{
            progressBar.visibility = View.VISIBLE

            val signInIntent = mGoogleSignInClient!!.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
    }

//    private fun signInAnon(){
//        if (!isNetworkConnected()){
//            Toast.makeText(applicationContext,resources.getString(R.string.no_internet_connection_init),Toast.LENGTH_SHORT).show()
//        }else{
//            progressBar.visibility = View.VISIBLE
//
//            mAuth.signInAnonymously()
//                .addOnCompleteListener(this) { task ->
//                    if (task.isSuccessful) {
//
//                        progressBar.visibility = GONE
//                        // Sign in success, update UI with the signed-in user's information
//                        val intent = Intent(this@SignInActivity, MainActivity::class.java)
//                        intent.putExtra("Signed in", true)
//                        startActivity(intent)
//                    } else {
//                        progressBar.visibility = GONE
//
//                        // If sign in fails, display a message to the user.
//                        Toast.makeText(applicationContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
//                    }
//                }
//        }
//    }

    private fun isNetworkConnected(): Boolean {
        val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo != null && cm.activeNetworkInfo!!.isConnected
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                // ...
                e.fillInStackTrace()
            }
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(
                this
            ) { task ->
                if (task.isSuccessful) {
                    progressBar.visibility = GONE


                    val intent = Intent(this@SignInActivity, MainActivity::class.java)
                    startActivity(intent)
                } else {
                    progressBar.visibility = GONE

                    Toast.makeText(this@SignInActivity, "Sorry auth failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    companion object {
        private const val RC_SIGN_IN = 123
    }
}