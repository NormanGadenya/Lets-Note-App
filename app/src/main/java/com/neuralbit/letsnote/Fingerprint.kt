package com.neuralbit.letsnote

import android.content.Intent
import android.graphics.PorterDuff
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth

class Fingerprint : AppCompatActivity() {

    private var authenticationCallback: BiometricPrompt.AuthenticationCallback? = null
    private var cancellationSignal: CancellationSignal? = null

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fingerprint)
        val noteTitle = intent.getStringExtra("noteTitle")
        val noteDesc = intent.getStringExtra("noteDescription")
        val noteUid = intent.getStringExtra("noteUid")
        val timeStamp = intent.getLongExtra("timeStamp",0)
        val labelColor = intent.getIntExtra("labelColor",0)
        val pinned = intent.getStringExtra("pinned")
        val archived = intent.getBooleanExtra("archieved",false)
        val protected = intent.getBooleanExtra("protected",false)
        val tagList = intent.getStringArrayListExtra("tagList")
        val todoList = intent.getStringExtra("todoItems")
        val reminder = intent.getStringExtra("reminder")

        val noteTitleTV = findViewById<TextView>(R.id.noteTitleTV)
        noteTitleTV.text = noteTitle
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val fingerprint = findViewById<ImageView>(R.id.imageView)
            authenticationCallback = object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    fingerprint.setColorFilter(
                        ContextCompat.getColor(
                            applicationContext,
                            R.color.Red
                        ), PorterDuff.Mode.SRC_IN
                    )
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    val user = FirebaseAuth.getInstance().currentUser
                    if (user != null) {
                        fingerprint.setColorFilter(
                            ContextCompat.getColor(
                                applicationContext,
                                R.color.teal_200
                            ), PorterDuff.Mode.SRC_IN
                        )
                        try {
                            val addEditIntent = Intent(this@Fingerprint, AddEditNoteActivity::class.java)
                            addEditIntent.putExtra("noteType","Edit")
                            addEditIntent.putExtra("noteTitle",noteTitle)
                            addEditIntent.putExtra("noteDescription",noteDesc)
                            addEditIntent.putExtra("noteUid",noteUid)
                            addEditIntent.putExtra("timeStamp",timeStamp)
                            addEditIntent.putExtra("labelColor",labelColor)
                            addEditIntent.putExtra("pinned",pinned)
                            addEditIntent.putExtra("archieved",archived)
                            addEditIntent.putExtra("protected",protected)
                            addEditIntent.putExtra("reminder",reminder)
                            addEditIntent.putExtra("todoItems", todoList)
                            addEditIntent.putStringArrayListExtra("tagList", tagList)
                            startActivity(addEditIntent)
                            finish()
                        } catch (e: ClassNotFoundException) {
                            e.printStackTrace()
                        }
                    } else {
                        finishAndRemoveTask()
                    }
                }
            }
        }

        val biometricPrompt = BiometricPrompt.Builder(this)
            .setTitle("Let's Note")
            .setSubtitle("Authentication is required")
            .setDescription("Fingerprint Authentication")
            .setNegativeButton(
                "Cancel", mainExecutor
            ) { _, _ -> }.build()
        authenticationCallback?.let {
            biometricPrompt.authenticate(
                getCancellationSignal()!!,
                mainExecutor, it
            )
        }

    }


    private fun getCancellationSignal(): CancellationSignal? {
        cancellationSignal = CancellationSignal()
        cancellationSignal?.setOnCancelListener {
            Toast.makeText(applicationContext, "Authentication cancelled", Toast.LENGTH_SHORT)
                .show()
        }
        return cancellationSignal
    }
}