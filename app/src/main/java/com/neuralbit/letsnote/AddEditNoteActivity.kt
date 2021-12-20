package com.neuralbit.letsnote

import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.HORIZONTAL
import com.google.android.material.snackbar.Snackbar
import com.teamwork.autocomplete.MultiAutoComplete
import com.teamwork.autocomplete.adapter.AutoCompleteTypeAdapter
import com.teamwork.autocomplete.tokenizer.PrefixTokenizer
import com.teamwork.autocomplete.view.MultiAutoCompleteEditText
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList


class AddEditNoteActivity : AppCompatActivity() ,TagRVInterface {
    private lateinit var actionBarIcons: List<Int>
    private lateinit var restoreButton: ImageButton
    private lateinit var archiveButton: ImageButton
    private lateinit var deleteButton: ImageButton
    private lateinit var backButton: ImageButton
    private lateinit var pinButton: ImageButton
    private lateinit var noteTitleEdit : EditText
    private lateinit var noteDescriptionEdit : MultiAutoCompleteEditText
    private var noteID : Long= -1
    private var tagID = -1
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
//    private lateinit var newTagButton : Button
    private var newTagTyped = false
    private var backPressed  = false
    private var tagList : ArrayList<Tag> = ArrayList()
    private lateinit var tagListRV : RecyclerView
    private var isKeyBoardShowing = false
    private var deletedTag = ArrayList<String>()
    private var tagDeleted = false
    private lateinit var tagListAdapter : TagRVAdapter


    override fun onCreate(savedInstanceState: Bundle?)  {
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
        tagListRV = findViewById(R.id.tagListRV)
        coordinatorlayout = findViewById(R.id.coordinatorlayout)
        val layoutManager = LinearLayoutManager(applicationContext,LinearLayoutManager.HORIZONTAL,false)

        layoutManager.orientation = HORIZONTAL
         tagListAdapter= TagRVAdapter(applicationContext,this)
        tagListRV.layoutManager= layoutManager
        tagListRV.adapter = tagListAdapter

        viewModal = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        ).get(NoteViewModel::class.java)
        viewModal.allNotes.observe(this) { list ->
            list?.let {
                allNotes = it
                
            }
        }

        coordinatorlayout.viewTreeObserver.addOnGlobalLayoutListener { ViewTreeObserver.OnGlobalLayoutListener {
          val r = Rect()
          coordinatorlayout.getWindowVisibleDisplayFrame(r)
          val screenHeight =  coordinatorlayout.rootView.height
          val keypadHeight = screenHeight - r.bottom
          if (keypadHeight > screenHeight * 0.15) { // 0.15 ratio is perhaps enough to determine keypad height.
            // keyboard is opened
            if (!isKeyBoardShowing) {
                isKeyBoardShowing = true
                Log.d(TAG, "onCreate: opened")
                onKeyboardVisibilityChanged(true)
            }
        }
          else {
            // keyboard is closed
            if (isKeyBoardShowing) {
                Log.d(TAG, "onCreate: closed")

                isKeyBoardShowing = false
                onKeyboardVisibilityChanged(false)
            }
        }

        } }
        viewModal.archivedNote.observe(this) {
            archivedNotes = it
        }
        viewModal.pinnedNotes.observe(this) {
            pinnedNotes = it
        }
        KeyboardUtils.addKeyboardToggleListener(this,KeyboardUtils.SoftKeyboardToggleListener {
            onKeyboardVisibilityChanged(it)
        })

        deleteButton = findViewById(R.id.deleteButton)
        backButton = findViewById(R.id.backButton)
        archiveButton = findViewById(R.id.archiveButton)
        restoreButton = findViewById(R.id.restoreButton)
//        newTagButton = findViewById(R.id.newTagBtn)
//        tagSpinner = findViewById(R.id.tagSpinner)
//        tagSpinner.onItemSelectedListener = this

//        val evColor = findViewById<ImageButton>(R.id.evBackground)
//        val woColor = findViewById<ImageButton>(R.id.wOrchidBackground)
//        val cColor = findViewById<ImageButton>(R.id.celadonBackground)
//        val hdColor = findViewById<ImageButton>(R.id.HoneydrewBackground)
//        val aColor = findViewById<ImageButton>(R.id.apricotBackground)
//        val whColor = findViewById<ImageButton>(R.id.whiteBackground)
        coordinatorlayout = findViewById(R.id.coordinatorlayout)
        pinButton = findViewById(R.id.pinButton)

        noteType = intent.getStringExtra("noteType").toString()
        archived = intent.getBooleanExtra("archivedNote",false)
        viewModal.archived.value = archived
        val pinnedNote = intent.getBooleanExtra("pinnedNote",false)
        viewModal.pinned.value = pinnedNote
        noteColor = intent.getStringExtra("noteColor")
        if(noteColor!=null){
            setBgColor()
         }
        viewModal.pinned.observe(this){
            if (it){
                pinButton.setImageResource(R.drawable.ic_baseline_push_pin_24)
            }else{
                pinButton.setImageResource(R.drawable.ic_outline_push_pin_24)
            }
        }
        viewModal.archived.observe(this){
            if (it){
                archiveButton.setImageResource(R.drawable.ic_baseline_unarchive_24)
            }else{
                archiveButton.setImageResource(R.drawable.ic_outline_archive_24)
            }
        }

        when (noteType) {
            "Edit" -> {
                val noteTitle = intent.getStringExtra("noteTitle")
                noteDesc = intent.getStringExtra("noteDescription").toString()
                val noteTimeStamp = intent.getLongExtra("noteTimeStamp",0)
                tvTimeStamp.text= getString(R.string.timeStamp,cm.convertLongToTime(noteTimeStamp)[0],cm.convertLongToTime(noteTimeStamp)[1])
                tvTimeStamp.visibility =VISIBLE
                noteID = intent.getLongExtra("noteID", -1)
                noteTitleEdit.setText(noteTitle)
                noteDescriptionEdit.setText(noteDesc)
                if(archived) {
                    archiveButton.visibility = GONE
                    restoreButton.visibility = VISIBLE
                }
                lifecycleScope.launch {
                    for (tag in viewModal.getTagsWithNote(noteID).last().tags){
                        viewModal.addTagToList(tag)
                    }
                    tagListAdapter.updateList(viewModal.tagList)

//                    (viewModal.getTagsWithNote(noteID).first().tags)
                }
            }
            else -> {

                tvTimeStamp.visibility =GONE

            }
        }

        viewModal.newTagTyped.observe(this) {
            newTagTyped = it
        }
        viewModal.wordEnd.observe(this) {
            wordEnd = it
        }



        viewModal.backPressed.observe(this) {
            backPressed = it
        }

        viewModal.wordStart.observe(this) {
            wordStart = it
        }


        viewModal.noteDescString.observe(this) { noteDescStr ->
            if (newTagTyped) {

                viewModal.allTags.observe(this) {

                    val adapter: AutoCompleteTypeAdapter<Tag> =
                        AutoCompleteTypeAdapter.Build.from(TagViewBinder(), TagTokenFilter())
                    it?.let { adapter.setItems(it) }
                    val multiAutoComplete = MultiAutoComplete.Builder()
                        .tokenizer(PrefixTokenizer('#'))
                        .addTypeAdapter(adapter)
                        .build()
                    multiAutoComplete.onViewAttached(noteDescriptionEdit)

                    if (noteDescStr.isNotEmpty()) {
                        if (noteDescStr.length >= 2) {

                            if (noteDescStr[noteDescStr.length - 1] == ' ') {
                                val tag = Tag(noteDescStr.substring(0, noteDescStr.length - 1))
                                if (tag !in it) {
                                    viewModal.addTag(tag)
                                }
                                viewModal.addTagToList(tag)
                                tagListAdapter.updateList(viewModal.tagList)
                            }

                        }
                    }


                }


                if (noteDescStr != null) {
                    tagString = noteDescStr

                }
            } else {
//                newTagButton.visibility = GONE

            }

        }


        noteTitleEdit.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(p3>0){
                    viewModal.noteChanged(true)
                    if(!tagListAdapter.deleteIgnored){
                        tagListAdapter.deleteIgnored = true
                        tagListAdapter.notifyDataSetChanged()

                    }
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
                    viewModal.noteChanged(true)
                    if(!tagListAdapter.deleteIgnored){
                        tagListAdapter.deleteIgnored = true
                        tagListAdapter.notifyDataSetChanged()

                    }

                }
                var str : List<String> = ArrayList<String>()
                if(!backPressed){
                    tagDeleted = false
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


            }

            override fun afterTextChanged(p0: Editable?) {

            }
        })

        noteDescriptionEdit.setOnKeyListener { p0, p1, p2 ->
            viewModal.backPressed.value = p1 == KeyEvent.KEYCODE_DEL
            false
        }

//        newTagButton.setOnClickListener {
//            if (tagString != null) {
//                viewModal.addTag(Tag(tagString!!))
//                viewModal.newTagTyped.value =false
//            }
//        }

        viewModal.texChanged.observe(this) {
            textChanged = it
        }
        viewModal.deleted.observe(this) {
            deletable = it
        }
        viewModal.archived.observe(this) {
            if (archived) {
                pinButton.visibility = GONE
                archiveButton.visibility = GONE
                restoreButton.visibility = VISIBLE
            } else {
                pinButton.visibility = VISIBLE
                archiveButton.visibility = VISIBLE
                restoreButton.visibility = GONE
            }
        }


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
                    if (note.noteID == noteID) {
                        viewModal.deleteNote(note)
                    }
                }

                Toast.makeText(this,"Note deleted",Toast.LENGTH_SHORT).show()
                viewModal.deleted.value = true
                goToMain()
            }

        }
        archiveButton.setOnClickListener {
            viewModal.archived.value = true
            val archivedNote = ArchivedNote(noteID)
            viewModal.archiveNote(archivedNote)
            val snackbar = Snackbar.make(coordinatorlayout,"Note Achieved",Snackbar.LENGTH_LONG)
            snackbar.setAction("UNDO"
            ) {
                viewModal.archived.value = false
                viewModal.removeArchive(archivedNote)
                Toast.makeText(this,"Note Unarchived", Toast.LENGTH_SHORT).show()
            }
            snackbar.show()
            

        }
        pinButton.setOnClickListener {
            viewModal.pinned.value = !pinnedNote
            val pN=PinnedNote(noteID)
            viewModal.pinNote(pN)
            val snackbar = Snackbar.make(coordinatorlayout,"Note pinned",Snackbar.LENGTH_LONG)
            snackbar.setAction("UNDO"
            ) {
                viewModal.pinned.value = pinnedNote
                viewModal.removePin(pN)
                Toast.makeText(this,"Note unpinned", Toast.LENGTH_SHORT).show()
            }
            snackbar.show()

        }

        restoreButton.setOnClickListener {
            val archivedNote = ArchivedNote(noteID)
            viewModal.removeArchive(archivedNote)
            viewModal.archived.value = false
            Toast.makeText(this,"Note Unarchived", Toast.LENGTH_SHORT).show()


        }
    }

    private fun onKeyboardVisibilityChanged(b: Boolean) {
        if(b){
            noteDescriptionEdit.maxLines = 11
        }else{
            noteDescriptionEdit.maxLines = 23

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
                if (noteTitle.isNotEmpty() || noteDescription.isNotEmpty()){
                    val note = Note(noteTitle,noteDescription,currentDate)
                    if(noteType == "Edit"){
                        note.noteID = noteID
                        viewModal.updateNote(note)
                        Toast.makeText(this,"Note updated .. " , Toast.LENGTH_SHORT).show()

                    }else{
                        noteID = allNotes.size.toLong() + 1
                        viewModal.addNote(note)
                        Toast.makeText(this,"Note added .. " , Toast.LENGTH_SHORT).show()
                        
                    }
                    for(tag in viewModal.tagList){

                        val crossRef = NoteTagCrossRef(noteID,tag.tagTitle)
                        viewModal.insertNoteTagCrossRef(crossRef)
                    }

                    Log.d(TAG, "saveNote: $deletedTag")

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

    

    override fun deleteTag(tag: Tag) {
        viewModal.tagList.remove(tag)
        if (noteType == "Edit"){
            viewModal.deleteNoteTagCrossRef(NoteTagCrossRef(noteID,tag.tagTitle))
        }
        tagListAdapter.updateList(viewModal.tagList)

    }


}