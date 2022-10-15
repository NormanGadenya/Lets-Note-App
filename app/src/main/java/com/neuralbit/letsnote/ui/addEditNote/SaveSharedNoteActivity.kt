package com.neuralbit.letsnote.ui.addEditNote

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.neuralbit.letsnote.R
import com.neuralbit.letsnote.firebase.entities.NoteFireIns
import com.neuralbit.letsnote.ui.signIn.SignInActivity
import com.neuralbit.letsnote.utilities.Common
import kotlin.system.exitProcess

class SaveSharedNoteActivity : AppCompatActivity() {
    private var noteDescEdit : EditText? = null
    private var noteTitleEdit : EditText? = null
    private var noteDesc : String? = null
    private var noteTitle : String? = null
    private lateinit var viewModal : NoteViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModal = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[NoteViewModel::class.java]
        val settingsPref =  getSharedPreferences("Settings", MODE_PRIVATE)
        val useLocalStorage = settingsPref.getBoolean("useLocalStorage",false)
        val fUser = FirebaseAuth.getInstance().currentUser
        if (fUser == null && !useLocalStorage ) {
            val intent = Intent(applicationContext, SignInActivity::class.java)
            startActivity(intent)
        }
        viewModal.useLocalStorage = useLocalStorage

        if(intent?.action == Intent.ACTION_SEND){
            if("text/plain" == intent.type){
                intent.getStringExtra(Intent.EXTRA_TEXT)?.let {
                    noteDesc = it
                }
            }
        }
        val layoutView = layoutInflater.inflate(R.layout.save_shared_note_fragment,null)

        val saveNoteDialog: AlertDialog = this.let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setView(layoutView)
            }
            builder.create()
        }
        saveNoteDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        saveNoteDialog.setOnDismissListener {
            dismissApp()
        }
        noteDescEdit = layoutView.findViewById(R.id.noteEditDesc)
        noteTitleEdit = layoutView.findViewById(R.id.noteEditTitle)
        val cancelBtn = layoutView.findViewById<Button>(R.id.cancelButton)
        val saveNoteBtn = layoutView.findViewById<Button>(R.id.saveNoteButton)
        cancelBtn.setOnClickListener {
            dismissApp()
        }
        saveNoteBtn.setOnClickListener {
            noteDesc = noteDescEdit?.text.toString()
            noteTitle = noteTitleEdit?.text.toString()

            saveNote()

        }

        noteDescEdit?.setText(noteDesc)
        saveNoteDialog.show()
    }

    private fun saveNote() {
        val cm = Common()
        viewModal.addFireNote(NoteFireIns(noteTitle,noteDesc, timeStamp = cm.currentTimeToLong()))
        Thread.sleep(500)
        Toast.makeText(applicationContext,"Note saved",Toast.LENGTH_SHORT ).show()
        dismissApp()

    }

    private fun dismissApp(){
        finishAffinity()
        exitProcess(0)
    }

}