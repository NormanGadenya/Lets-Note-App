package com.neuralbit.letsnote

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.lifecycle.ViewModelProvider
import java.text.SimpleDateFormat
import java.util.*

class AddEditNoteActivity : AppCompatActivity() {
    lateinit var noteTitleEdit : EditText
    lateinit var noteDescriptionEdit : EditText
    var noteID = -1
    lateinit var viewModal :NoteViewModel
    lateinit var noteType : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_note)
        noteTitleEdit = findViewById(R.id.noteEditTitle)
        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setDisplayShowCustomEnabled(true)
        supportActionBar?.setCustomView(R.layout.note_action_bar)
        noteDescriptionEdit = findViewById(R.id.noteEditDesc)
        viewModal = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        ).get(NoteViewModel::class.java)

        noteType = intent.getStringExtra("noteType") as String
        if (noteType.equals("Edit")) {
            val noteTitle = intent.getStringExtra("noteTitle")
            val noteDesc = intent.getStringExtra("noteDescription")
            noteID = intent.getIntExtra("noteID", -1)
            noteTitleEdit.setText(noteTitle)
            noteDescriptionEdit.setText(noteDesc)
        }

    }

    override fun onStop() {
        val noteTitle = noteTitleEdit.text.toString()
        val noteDescription = noteDescriptionEdit.text.toString()

        if( noteType.equals("Edit")){
            if(noteTitle.isNotEmpty() && noteDescription.isNotEmpty()){
                val sdf= SimpleDateFormat("dd MM, yyyy - HH:mm")
                val currentDate:String = sdf.format(Date())
                val updateNote = Note(noteTitle,noteDescription,currentDate)
                updateNote.id = noteID
                viewModal.updateNote(updateNote)
                Toast.makeText(this,"Note updated .. " , Toast.LENGTH_SHORT).show()
            }
        }else{
            if(noteTitle.isNotEmpty() && noteDescription.isNotEmpty()){
                val sdf= SimpleDateFormat("dd MM, yyyy - HH:mm")
                val currentDate:String = sdf.format(Date())
                viewModal.addNote((Note(noteTitle,noteDescription,currentDate)))
                Toast.makeText(this,"Note added .. " , Toast.LENGTH_SHORT).show()


            }
        }
        super.onStop()

    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this@AddEditNoteActivity,MainActivity::class.java)
        startActivity(intent)
    }
}