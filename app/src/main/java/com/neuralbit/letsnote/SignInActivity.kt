package com.neuralbit.letsnote

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

class SignInActivity : AppCompatActivity() {
    private var mGoogleSignInClient: GoogleSignInClient? = null
    private lateinit var mAuth: FirebaseAuth
    private var firebaseUser : FirebaseUser? = null
    private var TAG = "SIGNUPACTIVITY"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAuth = FirebaseAuth.getInstance()
        firebaseUser = mAuth.currentUser
        if (firebaseUser!=null){
            val intent = Intent(this@SignInActivity,MainActivity::class.java)
            startActivity(intent)
        }

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        window.statusBarColor = getColor(R.color.gunmetal)

        setContentView(R.layout.activity_sign_in)

        createRequest()
        findViewById<View>(R.id.signInWithGoogleBtn).setOnClickListener { signIn() }
        findViewById<View>(R.id.skipBtn).setOnClickListener {
            val intent = Intent(this@SignInActivity,MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun createRequest() {

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()


        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun signIn() {
        val signInIntent = mGoogleSignInClient!!.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
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
                Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun downloadDB() {
//        val firebaseStorage = FirebaseStorage.getInstance()
//        val mStorageReference= firebaseUser?.uid?.let { firebaseStorage.reference.child(it) }!!
//        val dbFileRef = mStorageReference.child("letsNoteDB1")
//        val dbShmFileRef = mStorageReference.child("letsNoteDB1-shm")
//        val dbWalFileRef = mStorageReference.child("letsNoteDB1-wal")
//
//        val database = NoteDatabase.getDatabase(application)
//        database.close()
//
//        val dir = File(getString(R.string.databaseLoc))
//        if (dir.isDirectory) {
//            val children = dir.list()
//            if (children!=null){
//                for (i in children.indices) {
//                    File(dir, children[i]).delete()
//                }
//            }
//        }
//        val dbPath = resources.getString(R.string.dbFileLoc)
//        val dbPathShm = resources.getString(R.string.dbShmFileLoc)
//        val dbPathWal = resources.getString(R.string.dbWalFileLoc)
//        val TEN_MEGABYTES: Long = 1024 * 1024 * 10
//        dbFileRef.getBytes(TEN_MEGABYTES).addOnSuccessListener {
//            try {
//                val outputStream: OutputStream = FileOutputStream(dbPath)
//                outputStream.write(it)
//                outputStream.close()
//
//            }catch (e : Exception){
//                e.localizedMessage
//            }
//        }
//        dbShmFileRef.getBytes(TEN_MEGABYTES).addOnSuccessListener {
//            try {
//                val outputStream: OutputStream = FileOutputStream(dbPathShm)
//                outputStream.write(it)
//                outputStream.close()
//
//            }catch (e : Exception){
//                e.localizedMessage
//            }
//        }
//        dbWalFileRef.getBytes(TEN_MEGABYTES).addOnSuccessListener {
//            try {
//                val outputStream: OutputStream = FileOutputStream(dbPathWal)
//                outputStream.write(it)
//                outputStream.close()
//            }catch (e : Exception){
//                e.localizedMessage
//            }
//        }
        val intent = Intent(this@SignInActivity,MainActivity::class.java)
        startActivity(intent)

    }
    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(
                this
            ) { task ->
                if (task.isSuccessful) {
                    firebaseUser = mAuth.currentUser
                    downloadDB()

                } else {
                    Toast.makeText(this@SignInActivity, "Sorry auth failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    companion object {
        private const val RC_SIGN_IN = 123
    }
}