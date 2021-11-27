package com.neuralbit.letsnote

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.ActionBar
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import java.util.*
import androidx.core.graphics.drawable.DrawableCompat

import androidx.appcompat.content.res.AppCompatResources
import androidx.room.util.StringUtil
import com.neuralbit.letsnote.databinding.ActivityAddEditNoteBinding
import java.lang.StringBuilder


class AddEditNoteActivity : AppCompatActivity() {
    private lateinit var actionBarIcons: List<Int>
    private lateinit var restoreButton: ImageButton
    private lateinit var archiveButton: ImageButton
    private lateinit var deleteButton: ImageButton
    private lateinit var backButton: ImageButton
    private lateinit var pinButton: ImageButton
    private lateinit var noteTitleEdit : EditText
    private lateinit var noteDescriptionEdit : EditText
    private var noteID = -1
    private lateinit var viewModal :NoteViewModel
    private lateinit var noteType : String
    private lateinit var allNotes : List<Note>
    private lateinit var archivedNotes : List<Note>
    private lateinit var pinnedNotes : List<Note>
    private var noteColor : String ? = null
    private val TAG = "AddNoteActivity"
    private var deletable : Boolean = false
    private lateinit var tvTimeStamp : TextView
    private var textChanged : Boolean = false
    private var archived = false
    private lateinit var cm : Common
    private lateinit var noteDesc : String
    private lateinit var coordinatorlayout : View
    private var wordStart = 0
    private var wordEnd = 0
    private var tagString : String ? = null
    private lateinit var newTagButton : Button
    private var newTagTyped = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_note)
        noteTitleEdit = findViewById(R.id.noteEditTitle)
        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setDisplayShowCustomEnabled(true)
        actionBarIcons = listOf(R.drawable.ic_baseline_arrow_back_24,R.drawable.ic_baseline_archive_24,R.drawable.ic_baseline_restore_24)
        cm= Common()
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
        viewModal.pinnedNotes.observe(this ,{
            pinnedNotes = it
        })

        deleteButton = findViewById(R.id.deleteButton)
        backButton = findViewById(R.id.backButton)
        archiveButton = findViewById(R.id.archiveButton)
        restoreButton = findViewById(R.id.restoreButton)
        newTagButton = findViewById(R.id.newTagBtn)
//        val evColor = findViewById<ImageButton>(R.id.evBackground)
//        val woColor = findViewById<ImageButton>(R.id.wOrchidBackground)
//        val cColor = findViewById<ImageButton>(R.id.celadonBackground)
//        val hdColor = findViewById<ImageButton>(R.id.HoneydrewBackground)
//        val aColor = findViewById<ImageButton>(R.id.apricotBackground)
//        val whColor = findViewById<ImageButton>(R.id.whiteBackground)
        val tagContainer = findViewById<View>(R.id.tagContainer)
        coordinatorlayout = findViewById(R.id.coordinatorlayout)
        pinButton = findViewById(R.id.pinButton)


        //TODO fix spinner and add tag functionality
        noteType = intent.getStringExtra("noteType").toString()
        archived = intent.getBooleanExtra("archivedNote",false)
        viewModal.Archive(archived)
        val pinnedNote = intent.getBooleanExtra("pinnedNote",false)
        noteColor = intent.getStringExtra("noteColor")
        if(noteColor!=null){
            setBgColor()
        }
        when (noteType) {
            "Edit" -> {
                val noteTitle = intent.getStringExtra("noteTitle")
                noteDesc = intent.getStringExtra("noteDescription").toString()
                val noteTimeStamp = intent.getLongExtra("noteTimeStamp",0)
                tvTimeStamp.text= getString(R.string.timeStamp,cm.convertLongToTime(noteTimeStamp)[0],cm.convertLongToTime(noteTimeStamp)[1])
                tvTimeStamp.visibility =VISIBLE
                noteID = intent.getIntExtra("noteID", -1)
                noteTitleEdit.setText(noteTitle)
                noteDescriptionEdit.setText(noteDesc)
                if(archived) {
                    archiveButton.visibility = GONE
                    restoreButton.visibility = VISIBLE
                }
            }
            else -> {

                tvTimeStamp.visibility =GONE

            }
        }
        viewModal.newTagTyped.observe(this,{
            newTagTyped = it
        })
        viewModal.wordEnd.observe(this,{
            wordEnd = it
//            if(it!=0){
//                newTagButton.visibility = VISIBLE
//            }else{
//                newTagButton.visibility = GONE
//
//            }
        })

        viewModal.wordStart.observe(this,{
            wordStart = it

        })
        viewModal.noteDescString.observe(this,{
            if(newTagTyped){
                if(it!=null){
                    newTagButton.text=it
                    newTagButton.visibility = VISIBLE
                }
            }else{
                newTagButton.visibility = GONE

            }

        })

        noteTitleEdit.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(p3>0){
                    viewModal.noteChanged(true)
                }
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })
        noteDescriptionEdit.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val changedString =p0.toString()


            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(p3>0){
                    viewModal.noteChanged(true)
                }


                if(p0?.get(p0.length - 1) == '#'){
                    viewModal.wordStart.value = p0.length
                    viewModal.newTagTyped.value = true
                }

                if(wordStart> 0) {
                    viewModal.wordEnd.value = p0?.length
                    viewModal.getTagString(p0.toString())


                    if(p0?.get(p0.length - 1) == ' '){
                        viewModal.newTagTyped.value = false

                    }

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
            if(archived){
                pinButton.visibility = GONE
                archiveButton.visibility = GONE
                restoreButton.visibility = VISIBLE
                tagContainer.visibility = GONE
            }else{
                pinButton.visibility = VISIBLE
                archiveButton.visibility = VISIBLE
                restoreButton.visibility = GONE
                tagContainer.visibility = VISIBLE
            }
        })


        backButton.setOnClickListener {
            goToMain()
        }

        deleteButton.setOnClickListener {
            if(noteType == "Edit"){
                if(archived){
                    val archivedNote = ArchivedNote(noteID)
                    viewModal.removeArchive(archivedNote)
                }
                if(pinnedNote){
                    val pinnedNote = PinnedNote(noteID)
                    viewModal.removePin(pinnedNote)
                }
                for ( note in allNotes) {
                    if (note.id == noteID) {
                        viewModal.deleteNote(note)
                    }
                }

                Toast.makeText(this,"Note deleted",Toast.LENGTH_SHORT).show()
                viewModal.Delete(true)
                goToMain()
            }

        }
        archiveButton.setOnClickListener {
            viewModal.Archive(true)
            val archivedNote = ArchivedNote(noteID)
            viewModal.archiveNote(archivedNote)
            val snackbar = Snackbar.make(coordinatorlayout,"Note Achieved",Snackbar.LENGTH_LONG)
            snackbar.setAction("UNDO"
            ) {
                viewModal.Archive(false)
                viewModal.removeArchive(archivedNote)
                Toast.makeText(this,"Note Unarchived", Toast.LENGTH_SHORT).show()
            }
            snackbar.show()
            

        }
        pinButton.setOnClickListener {
            viewModal.Pinned(true)
            val pinnedNote = PinnedNote(noteID)
            viewModal.pinNote(pinnedNote)
            val snackbar = Snackbar.make(coordinatorlayout,"Note pinned",Snackbar.LENGTH_LONG)
            snackbar.setAction("UNDO"
            ) {
                viewModal.Pinned(false)
                viewModal.removePin(pinnedNote)
                Toast.makeText(this,"Note unpinned", Toast.LENGTH_SHORT).show()
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
        val window = window

        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        var colorID= R.color.white
        var textColorID =R.color.black
        var buttonColorID = R.color.white
        when(noteColor) {
            "White" -> {
                colorID = R.color.white
                buttonColorID = Color.BLACK
            }
            "English_violet" -> {
                colorID = R.color.English_violet
                textColorID = Color.WHITE

            }
            "Wild_orchid" -> { colorID = R.color.Wild_orchid }
            "Celadon" -> { colorID = R.color.Celadon }
            "Honeydew" -> { colorID = R.color.Honeydew }
            "Apricot" -> { colorID = R.color.Apricot }
        }

        noteTitleEdit.setTextColor(textColorID)
        noteDescriptionEdit.setTextColor(textColorID)
        tvTimeStamp.setTextColor(textColorID)
//            for ( drawable in actionBarIcons){
//                changeIconColor(buttonColorID,drawable)
//            }
//            window.statusBarColor = resources.getColor(colorID)
//            supportActionBar?.setBackgroundDrawable(ColorDrawable(resources.getColor(colorID)))
        coordinatorlayout.setBackgroundColor(resources.getColor(colorID))
    }

    private fun changeIconColor(iconColorID : Int, drawable : Int){
        val unwrappedDrawable = AppCompatResources.getDrawable(applicationContext, drawable)
        val wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable!!)
        DrawableCompat.setTint(wrappedDrawable, iconColorID)
    }

    private fun saveNote(){
        val noteTitle = noteTitleEdit.text.toString()
        val noteDescription = noteDescriptionEdit.text.toString()

        val currentDate= cm.currentTimeToLong()
        if(!deletable){
            if(textChanged){
                if(noteType == "Edit"){
                    if(noteTitle.isNotEmpty() && noteDescription.isNotEmpty()){
                        val updateNote = Note(noteTitle,noteDescription,currentDate)
                        updateNote.id = noteID
                        viewModal.updateNote(updateNote)
                        Toast.makeText(this,"Note updated .. " , Toast.LENGTH_SHORT).show()
                    }
                }else{
                    if(noteTitle.isNotEmpty() && noteDescription.isNotEmpty()){
                        viewModal.addNote((Note(noteTitle,noteDescription,currentDate )))
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

        val intent = Intent(this@AddEditNoteActivity,MainActivity::class.java)
        startActivity(intent)
    }


}