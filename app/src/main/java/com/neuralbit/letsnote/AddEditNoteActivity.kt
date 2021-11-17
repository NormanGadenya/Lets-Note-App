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
import androidx.coordinatorlayout.widget.CoordinatorLayout
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
    private lateinit var archivedNotes : List<Note>
    private lateinit var noteColor : String
    private val TAG = "AddNoteActivity"
    private var deletable : Boolean = false
    private lateinit var tvTimeStamp : TextView
    private var textChanged : Boolean = false
    private var archived : Boolean = false
    private lateinit var cm : Common
    private lateinit var coordinatorlayout : View


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_note)
        noteTitleEdit = findViewById(R.id.noteEditTitle)
        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setDisplayShowCustomEnabled(true)
        cm= Common()
        //TODO fix background color
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
        viewModal.archivedNote.observe(this,{
            archivedNotes = it
        })
        val archiveButton = findViewById<ImageButton>(R.id.archiveButton)
        val restoreButton = findViewById<ImageButton>(R.id.restoreButton)
        val evColor = findViewById<ImageButton>(R.id.evBackground)
        val woColor = findViewById<ImageButton>(R.id.wOrchidBackground)
        val cColor = findViewById<ImageButton>(R.id.celadonBackground)
        val hdColor = findViewById<ImageButton>(R.id.HoneydrewBackground)
        val aColor = findViewById<ImageButton>(R.id.apricotBackground)
        val whColor = findViewById<ImageButton>(R.id.whiteBackground)

        evColor.setOnClickListener {
            noteColor = "English_violet"
        }
        woColor.setOnClickListener {
            noteColor = "Wild_orchid"
        }
        cColor.setOnClickListener {
            noteColor = "Celadon"
        }
        hdColor.setOnClickListener {
            noteColor = "Honeydew"
        }
        aColor.setOnClickListener {
            noteColor = "Apricot"
        }
        whColor.setOnClickListener {
            noteColor = "White"
        }
        noteType = intent.getStringExtra("noteType").toString()
        val archivedNote = intent.getBooleanExtra("archivedNote",false)
        noteColor = intent.getStringExtra("noteColor").toString()
        setBgColor()
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
            if(noteType == "Edit"){


                for (note in archivedNotes) {
                    Log.d(TAG, "onCreate: ${note.id} $noteID")

                    if(note.id == noteID){
                        viewModal.deleteNote(note)
                    }
                }
                if(archivedNote){
                    val archivedNote = ArchivedNote(noteID)
                    viewModal.removeArchive(archivedNote)
                }
            }
            Toast.makeText(this,"Note deleted",Toast.LENGTH_SHORT).show()
            viewModal.Delete(true)
            goToMain()
        }
        archiveButton.setOnClickListener {
            viewModal.Archive(true)
            val archivedNote = ArchivedNote(noteID)
            viewModal.archiveNote(archivedNote)
            coordinatorlayout = findViewById(R.id.coordinatorlayout)
            val snackbar = Snackbar.make(coordinatorlayout,"Note Achieved",Snackbar.LENGTH_LONG)
            snackbar.setAction("UNDO"
            ) {
                viewModal.Archive(false)
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

    private fun setBgColor(){
        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            val window = window

            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            var colorID= R.color.white
            when(noteColor) {
                "White" -> { colorID = R.color.white }
                "English_violet" -> { colorID = R.color.English_violet }
                "Wild_orchid" -> { colorID = R.color.Wild_orchid }
                "Celadon" -> { colorID = R.color.Celadon }
                "Honeydew" -> { colorID = R.color.Honeydew }
                "Apricot" -> { colorID = R.color.Apricot }
            }

            window.statusBarColor = resources.getColor(colorID)
            supportActionBar?.setBackgroundDrawable(ColorDrawable(resources.getColor(colorID)))
            coordinatorlayout.setBackgroundColor(resources.getColor(colorID))
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
                        val updateNote = Note(noteTitle,noteDescription,currentDate, noteColor)
                        updateNote.id = noteID
                        viewModal.updateNote(updateNote)
                        Toast.makeText(this,"Note updated .. " , Toast.LENGTH_SHORT).show()
                    }
                }else{
                    if(noteTitle.isNotEmpty() && noteDescription.isNotEmpty()){
                        viewModal.addNote((Note(noteTitle,noteDescription,currentDate, noteColor )))
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


    private fun goToMain() {
        saveNote()

        val intent = Intent(this@AddEditNoteActivity,MainActivity2::class.java)
        startActivity(intent)
    }


}