package com.neuralbit.letsnote.ui.addEditNote

import android.content.Intent
import android.graphics.PorterDuff
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.neuralbit.letsnote.R
import com.neuralbit.letsnote.ui.main.MainActivity

class Fingerprint : AppCompatActivity() {

    private lateinit var retryTV: TextView
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
        val pinned = intent.getBooleanExtra("pinned",false)
        val archived = intent.getBooleanExtra("archieved",false)
        val protected = intent.getBooleanExtra("protected",false)
        val tagList = intent.getStringArrayListExtra("tagList")
        val todoList = intent.getStringExtra("todoItems")
        val noteChanged = intent.getBooleanExtra("noteChanged",false)
        val reminder = intent.getStringExtra("reminder")

        val noteTitleTV = findViewById<TextView>(R.id.textView)
        noteTitleTV.text = "Unlock to view the note \n $noteTitle"
        val fingerprintIV = findViewById<ImageView>(R.id.imageView)
        retryTV = findViewById(R.id.retryTV)

        fingerprintIV.setOnClickListener{
            initializeFingerprint(
                fingerprintIV,
                noteTitle,
                noteDesc,
                noteUid,
                timeStamp,
                labelColor,
                pinned,
                archived,
                protected,
                reminder,
                noteChanged,
                todoList,
                tagList
            )
        }
        initializeFingerprint(
            fingerprintIV,
            noteTitle,
            noteDesc,
            noteUid,
            timeStamp,
            labelColor,
            pinned,
            archived,
            protected,
            reminder,
            noteChanged,
            todoList,
            tagList
        )

    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun initializeFingerprint(
        fingerprint: ImageView,
        noteTitle: String?,
        noteDesc: String?,
        noteUid: String?,
        timeStamp: Long,
        labelColor: Int,
        pinned: Boolean?,
        archived: Boolean,
        protected: Boolean,
        reminder: String?,
        noteChanged : Boolean,
        todoList: String?,
        tagList: ArrayList<String>?
    ) {

        authenticationCallback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                retryTV.visibility = VISIBLE
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
                val settingsPref = applicationContext.getSharedPreferences("Settings", MODE_PRIVATE)
                val useLocalStorage = settingsPref.getBoolean("useLocalStorage",false)
                if (user != null || useLocalStorage) {
                    retryTV.visibility = GONE

                    fingerprint.setColorFilter(
                        ContextCompat.getColor(
                            applicationContext,
                            R.color.teal_200
                        ), PorterDuff.Mode.SRC_IN
                    )
                    try {

                        val addEditIntent = Intent(this@Fingerprint, AddEditNoteActivity::class.java)
                        addEditIntent.putExtra("noteType", "Edit")
                        addEditIntent.putExtra("noteTitle", noteTitle)
                        addEditIntent.putExtra("noteDescription", noteDesc)
                        addEditIntent.putExtra("noteUid", noteUid)
                        addEditIntent.putExtra("timeStamp", timeStamp)
                        addEditIntent.putExtra("labelColor", labelColor)
                        addEditIntent.putExtra("pinned", pinned)
                        addEditIntent.putExtra("archieved", archived)
                        addEditIntent.putExtra("protected", protected)
                        addEditIntent.putExtra("reminder", reminder)
                        addEditIntent.putExtra("todoItems", todoList)
                        addEditIntent.putExtra("noteChanged",noteChanged)
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


        val biometricPrompt = BiometricPrompt.Builder(this)
            .setTitle("Let's Note")
            .setSubtitle("Authentication is required")
            .setDescription("Fingerprint Authentication")
            .setNegativeButton(
                "Cancel", mainExecutor
            ) { _, _ ->
                val intent = Intent(this@Fingerprint, MainActivity::class.java)
                startActivity(intent)
                finish()
            }.build()
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
            val intent = Intent(this@Fingerprint, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        return cancellationSignal
    }
}