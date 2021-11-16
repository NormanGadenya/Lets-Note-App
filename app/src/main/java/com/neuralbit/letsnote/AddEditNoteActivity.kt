package com.neuralbit.letsnote

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.ActionBar
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import java.util.*

class AddEditNoteActivity : AppCompatActivity() {
    private lateinit var noteTitleEdit : EditText
    private lateinit var noteDescriptionEdit : EditText
    private var noteID = -1
    private lateinit var viewModal :NoteViewModel
    private lateinit var noteType : String
    private lateinit var allNotes : List<Note>
    private val TAG = "AddNoteActivity"
    private var deletable : Boolean = false
    private lateinit var tvTimeStamp : TextView
    private var textChanged : Boolean = false
    private var archived : Boolean = false
    private lateinit var cm : Common
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor : SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_note)
        noteTitleEdit = findViewById(R.id.noteEditTitle)
        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setDisplayShowCustomEnabled(true)
        cm= Common()
        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            var window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor= Color.GRAY
            supportActionBar?.setBackgroundDrawable(ColorDrawable(Color.GRAY))
        }
        sharedPreferences = getSharedPreferences("ArchivedNotes",MODE_PRIVATE)
        editor = sharedPreferences.edit()
        supportActionBar?.setCustomView(R.layout.note_action_bar)
        noteDescriptionEdit = findViewById(R.id.noteEditDesc)
        tvTimeStamp = findViewById(R.id.tvTimeStamp)
        viewModal = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        ).get(NoteViewModel::class.java)
        viewModal.allNotes.observe(this,{ list->
            list?.let {
                allNotes=it
            }
        })
        val archiveButton = findViewById<ImageButton>(R.id.archiveButton)
        val restoreButton = findViewById<ImageButton>(R.id.restoreButton)

        noteType = intent.getStringExtra("noteType").toString()
        val archivedNote = intent.getBooleanExtra("archivedNote",false)
        when (noteType) {
            "Edit" -> {
                val noteTitle = intent.getStringExtra("noteTitle")
                val noteDesc = intent.getStringExtra("noteDescription")
                val noteTimeStamp = intent.getLongExtra("noteTimeStamp",0)
                tvTimeStamp.text= getString(R.string.timeStamp,cm.convertLongToTime(noteTimeStamp)[0],cm.convertLongToTime(noteTimeStamp)[1])
                tvTimeStamp.visibility =VISIBLE
                noteID = intent.getIntExtra("noteID", -1)
                noteTitleEdit.setText(noteTitle)
                noteDescriptionEdit.setText(noteDesc)
                if(archivedNote) {
                    archiveButton.visibility = GONE
                    restoreButton.visibility = VISIBLE
                }
            }
            else -> {

                tvTimeStamp.visibility =GONE

            }
        }

        noteTitleEdit.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(p3>0){
                    viewModal.TextChanged(true)
                }
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })
        noteDescriptionEdit.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(p3>0){
                    viewModal.TextChanged(true)
                }
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })
        viewModal.texChanged.observe(this,{
            textChanged= it
        })
        viewModal.delete.observe(this, {
            deletable = it
        })
        viewModal.archive.observe(this, {
            archived = it
            if(archived){
                archiveButton.visibility = GONE
                restoreButton.visibility = VISIBLE
            }else{
                archiveButton.visibility = VISIBLE
                restoreButton.visibility = GONE
            }
        })


        val backButton = findViewById<ImageButton>(R.id.backButton)
        backButton.setOnClickListener {
            goToMain()
        }

        val deleteButton = findViewById<ImageButton>(R.id.deleteButton)
        deleteButton.setOnClickListener {
            if( noteType.equals("Edit")){
                for (note in allNotes) {
                    if(note.id == noteID){
                        viewModal.deleteNote(note)
                    }
                }
            }
            Toast.makeText(this,"Note deleted",Toast.LENGTH_SHORT).show()
            viewModal.Delete(true)
            goToMain()
        }
        archiveButton.setOnClickListener {
            viewModal.Archive(true)
            val archivedNotes =sharedPreferences.getStringSet("archivedNotes", null)
            val set = mutableSetOf<String>()
            if (archivedNotes != null){
                for ( noteId in archivedNotes ){
                    set.add(noteId)
                }
            }
            val archivedNote = ArchivedNote(noteID)
            viewModal.archiveNote(archivedNote)
            set.add(noteID.toString())
            editor.putStringSet("archivedNotes",set)

            val coordinatorlayout = findViewById<View>(R.id.coordinatorlayout)
            val snackbar = Snackbar.make(coordinatorlayout,"Note Achieved",Snackbar.LENGTH_LONG)
            snackbar.setAction("UNDO"
            ) {
                viewModal.Archive(false)

                set.remove(noteID.toString())
                viewModal.removeArchive(archivedNote)
                Toast.makeText(this,"Note Unarchived", Toast.LENGTH_SHORT).show()
            }
            snackbar.show()

        }

        restoreButton.setOnClickListener {
            val archivedNote = ArchivedNote(noteID)

            viewModal.removeArchive(archivedNote)
            viewModal.Archive(false)
            Toast.makeText(this,"Note Unarchived", Toast.LENGTH_SHORT).show()


        }
    }
    private fun saveNote(){
        val noteTitle = noteTitleEdit.text.toString()
        val noteDescription = noteDescriptionEdit.text.toString()

        val currentDate= cm.currentTimeToLong()
        if(!deletable){
            if(textChanged){
                if( noteType.equals("Edit")){
                    if(noteTitle.isNotEmpty() && noteDescription.isNotEmpty()){
                        val updateNote = Note(noteTitle,noteDescription,currentDate)
                        updateNote.id = noteID
                        viewModal.updateNote(updateNote)
                        Toast.makeText(this,"Note updated .. " , Toast.LENGTH_SHORT).show()
                    }
                }else{
                    if(noteTitle.isNotEmpty() && noteDescription.isNotEmpty()){
                        viewModal.addNote((Note(noteTitle,noteDescription,currentDate)))
                        Toast.makeText(this,"Note added .. " , Toast.LENGTH_SHORT).show()


                    }
                }
            }
        }

    }



    override fun onBackPressed() {
        super.onBackPressed()
        goToMain()

    }

    override fun onStop() {
        if (archived){
            editor.apply()
        }
        super.onStop()

    }

    private fun goToMain() {
        saveNote()

        val intent = Intent(this@AddEditNoteActivity,MainActivity2::class.java)
        startActivity(intent)
    }


}