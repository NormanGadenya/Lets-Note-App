package com.neuralbit.letsnote

import android.Manifest
import android.R.attr
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.speech.RecognizerIntent
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.util.Log
import android.util.SparseArray
import android.view.KeyEvent
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.HORIZONTAL
import com.flask.colorpicker.ColorPickerView
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.text.TextBlock
import com.google.android.gms.vision.text.TextRecognizer
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.neuralbit.letsnote.adapters.AddEditLabelAdapter
import com.neuralbit.letsnote.adapters.LabelClickInterface
import com.neuralbit.letsnote.entities.*
import com.neuralbit.letsnote.ui.label.LabelViewModel
import com.neuralbit.letsnote.utilities.*
import com.teamwork.autocomplete.MultiAutoComplete
import com.teamwork.autocomplete.adapter.AutoCompleteTypeAdapter
import com.teamwork.autocomplete.tokenizer.PrefixTokenizer
import com.teamwork.autocomplete.view.MultiAutoCompleteEditText
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.coroutines.launch
import java.io.IOException
import java.security.Key
import java.util.*


class AddEditNoteActivity : AppCompatActivity() ,
    TagRVInterface,
    GetTimeFromPicker,
    GetDateFromPicker,
    GetTagFromDialog,
    LabelClickInterface
{
    private lateinit var restoreButton: ImageButton
    private lateinit var archiveButton: ImageButton
    private lateinit var optionsButton: FloatingActionButton
    private lateinit var deleteButton: ImageButton
    private lateinit var backButton: ImageButton
    private lateinit var pinButton: ImageButton
    private lateinit var alertButton: ImageButton
    private lateinit var ocrButton: ImageButton
    private lateinit var sttButton: ImageButton
    private lateinit var noteTitleEdit : EditText
    private lateinit var noteDescriptionEdit : MultiAutoCompleteEditText
    private lateinit var addTagBtn : ImageButton
    private lateinit var delLabelBtn : ImageButton
    private lateinit var reminderIcon : ImageView
    private lateinit var reminderTV : TextView
    private var noteID : Long= -1
    private lateinit var viewModal : NoteViewModel
    private lateinit var labelViewModel : LabelViewModel
    private lateinit var noteType : String
    private val TAG = "AddNoteActivity"
    private var deletable : Boolean = false
    private lateinit var tvTimeStamp : TextView
    private var textChanged : Boolean = false
    private var archived = false
    private lateinit var cm : Common
    private lateinit var noteDesc : String
    private lateinit var noteTitle : String
    private var noteTimeStamp : Long = 0
    private lateinit var coordinatorlayout : View
    private var wordStart = 0
    private var wordEnd = 0
    private var tagString : String ? = null
    private var newTagTyped = false
    private var backPressed  = false
    private lateinit var tagListRV : RecyclerView
    private lateinit var labelListRV : RecyclerView
    private var isKeyBoardShowing = false
    private var tagDeleted = false
    private lateinit var tagListAdapter : AddEditTagRVAdapter
    private lateinit var labelListAdapter : AddEditLabelAdapter
    private lateinit var alertBottomSheet : BottomSheetDialog
    private lateinit var labelBottomSheet : BottomSheetDialog
    private lateinit var labelBtn : ImageButton
    private  var reminder: Reminder? = null
    private  var label: Label? = null
    private val REQUEST_CAMERA_CODE = 100
    private lateinit var lifecycleOwner : LifecycleOwner
    private lateinit var calendar: Calendar
    private lateinit var timeTitleTV :TextView
    private lateinit var dateTitleTV :TextView
    private lateinit var layoutManager : LinearLayoutManager
    private var tagList = ArrayList<String>()
    private var pinnedNote : PinnedNote ? = null
    private var archivedNote : ArchivedNote ? = null
    private var notePinned = false
    private var reminderNoteSet = false
    private var labelNoteSet = false
    private lateinit var infoContainer : View
    private lateinit var bottomSheet : BottomSheetBehavior<View>
    private var bitmap : Bitmap? = null
    private val REQUEST_CODE_SPEECH_INPUT = 1
    private lateinit var colorPickerView: ColorPickerView

    override fun onCreate(savedInstanceState: Bundle?)  {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_note)

        initControllers()


        checkCameraPermission()
        if(intent?.action == Intent.ACTION_SEND){
            if("text/plain" == intent.type){
                intent.getStringExtra(Intent.EXTRA_TEXT)?.let {
                    noteDesc = it
                    viewModal.texChanged.value = true
                    noteDescriptionEdit.setText(it)
                }
            }
        }
        viewModal.getNoteLabel(noteID).observe(this){
            label = it

            viewModal.labelSet.value = it !=null
        }

        manipulateNoteDescLines()
        val labelIDs = HashSet<Int>()


        labelViewModel.getAllNotes().observe(lifecycleOwner){ list ->
            for (lwn in list){
                val label = lwn.label.labelID
                labelIDs.add(label)
            }
            labelListAdapter.updateLabelIDList(labelIDs)

        }
        viewModal.getArchivedNote(noteID).observe(this){
            viewModal.archived.value = it!=null
            archivedNote = it

        }

//        KeyboardUtils.addKeyboardToggleListener(this) { onKeyboardVisibilityChanged(it) }

        viewModal.allTags.observe(lifecycleOwner){
            
            for (tag in it){
                if (!tagList.contains(tag.tagTitle)){
                    tagList.add(tag.tagTitle)

                }
            }

        }


        addTagBtn.setOnClickListener {
            val addTagDialog = AddTagDialog(this,applicationContext)
            addTagDialog.tagList = tagList
            addTagDialog.show(supportFragmentManager,"addTagDialog")

        }

        when (noteType) {
            "Edit" -> {
                viewModal.getNote(noteID).observe(this){
                    if(it!=null){
                        noteTitle = it.title!!
                        noteDesc = it.description!!
                        noteTimeStamp = it.timeStamp
                        tvTimeStamp.text= getString(R.string.timeStamp,cm.convertLongToTime(noteTimeStamp)[0],cm.convertLongToTime(noteTimeStamp)[1])
                        tvTimeStamp.visibility =VISIBLE
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

                        }
                    }

                }

            }
            else -> {
                tvTimeStamp.visibility =GONE
            }
        }


        viewModal.getReminder(noteID).observe(this) {
            reminder = it
            viewModal.reminderSet.value = it !=null
        }

        viewModal.labelSet.observe(lifecycleOwner){
            labelNoteSet = it
            if(labelNoteSet){
                if (label != null){

                    coordinatorlayout.setBackgroundColor(label!!.labelID)
                    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                    window.statusBarColor = label!!.labelID

                }
                delLabelBtn.visibility = VISIBLE
            }else{
                coordinatorlayout.setBackgroundColor(Color.TRANSPARENT)
                window.statusBarColor = getColor(R.color.gunmetal)
                delLabelBtn.visibility = GONE

            }

        }

        delLabelBtn.setOnClickListener {
            viewModal.labelSet.value = false
            viewModal.texChanged.value = true

            if (label!=null && noteID>=0){

                viewModal.deleteNoteLabel(noteID)
            }
        }

        optionsButton.setOnClickListener {
            bottomSheet.state = BottomSheetBehavior.STATE_EXPANDED
        }

        viewModal.reminderSet.observe(lifecycleOwner){
            reminderNoteSet = it
            if(it){
                bottomSheet.state = BottomSheetBehavior.STATE_EXPANDED

                alertButton.setImageResource(R.drawable.ic_baseline_add_alert_24)
                reminderTV.visibility =  VISIBLE
                reminderIcon.visibility = VISIBLE

                reminderTV.text = resources.getString(R.string.reminder,cm.convertLongToTime(reminder?.dateTime!!)[0],cm.convertLongToTime(reminder?.dateTime!!)[1])
                val c = Calendar.getInstance()
                if (c.timeInMillis > reminder?.dateTime!!){
                    cancelAlarm()
                    viewModal.deleteReminder(noteID)
                }
            }else{
                alertButton.setBackgroundResource(R.drawable.ic_outline_add_alert_24)
                reminderTV.visibility =  GONE
                reminderIcon.visibility = GONE
            }
        }

        viewModal.getPinnedNote(noteID).observe(this){ pN->
            viewModal.pinned.value = pN!=null
            pinnedNote = pN

        }
        viewModal.pinned.observe(lifecycleOwner){
            notePinned = it

            if (!it){
                pinButton.setImageResource(R.drawable.ic_outline_push_pin_24)
            }else{
                pinButton.setImageResource(R.drawable.ic_baseline_push_pin_24)
            }
        }

        viewModal.archived.observe(lifecycleOwner){
            archived = it
            if (it) {
                pinButton.visibility = GONE
                archiveButton.visibility = GONE
                alertButton.visibility = GONE
                restoreButton.visibility = VISIBLE
            } else {
                pinButton.visibility = VISIBLE
                archiveButton.visibility = VISIBLE
                alertButton.visibility = VISIBLE
                restoreButton.visibility = GONE
            }
        }

        alertButton.setOnClickListener {
            if (reminder==null){
                showAlertSheetDialog()

            }else{
                cancelAlarm()

                viewModal.deleteReminder(noteID)
                reminder = null
            }
        }


        viewModal.wordEnd.observe(this) { wordEnd = it }

        viewModal.backPressed.observe(this) { backPressed = it }

        viewModal.wordStart.observe(this) { wordStart = it }

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
            }

        }


        labelBtn.setOnClickListener {
            showLabelBottomSheetDialog()
        }

        noteTitleEdit.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(p3>0){

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
                if(p0?.length!! > 0){




                    getTagFromString(p0,p3)

                }

            }

            override fun afterTextChanged(p0: Editable?) {

            }
        })


        noteDescriptionEdit.setOnKeyListener { _, p1, _ ->
            viewModal.noteChanged(true)
            viewModal.backPressed.value = p1 == KeyEvent.KEYCODE_DEL
            viewModal.enterPressed.value = p1 == KeyEvent.KEYCODE_ENTER
            false
        }

        noteTitleEdit.setOnKeyListener { _, _, _ ->
            viewModal.noteChanged(true)

            false
        }

        viewModal.texChanged.observe(this) {
            textChanged = it
        }
        viewModal.deleted.observe(this) {
            deletable = it
        }
        if (bottomSheet.state == BottomSheetBehavior.STATE_HIDDEN){
            optionsButton.visibility = VISIBLE
        }else{
            optionsButton.visibility = GONE

        }
        bottomSheet.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // handle onSlide
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> optionsButton.visibility = VISIBLE
                    BottomSheetBehavior.STATE_EXPANDED -> optionsButton.visibility = GONE
                    BottomSheetBehavior.STATE_COLLAPSED -> optionsButton.visibility = GONE
                }
            }
        })
        viewModal.enterPressed.observe(this){
            
            val noteContent = noteDescriptionEdit.text
            val noteContentSplit = noteContent.split("\n")
            var lineIndex = 0
            if (noteContentSplit.size>2){
                lineIndex = noteContentSplit.lastIndex-1
            }
            if (it){

                if(noteContentSplit.isNotEmpty()){
                        val prefix = listOf(" ","->","-","+","*",">")
                        for (p in prefix){
                            addBulletin(noteContentSplit,lineIndex,noteContent, p)
                        }
                        if (noteContentSplit[lineIndex].endsWith(":")) {
                            if (noteContent.endsWith("\n")) {
                                if (!backPressed){
                                    noteDescriptionEdit.append("-> ")

                                }
                            }
                        }


                    }

            }
        }



        backButton.setOnClickListener {
            Log.d(TAG, "onCreate: presseed")
            goToMain()
            
        }

        deleteButton.setOnClickListener {
            if(noteType == "Edit"){

                viewModal.removeArchive(ArchivedNote(noteID))

                viewModal.removePin(PinnedNote(noteID))

                label?.let { viewModal.deleteNoteLabel(noteID) }

                reminder?.let {
                    cancelAlarm()
                    viewModal.deleteReminder(noteID) }

                for(tag in viewModal.tagList){
                    viewModal.deleteNoteTagCrossRef(NoteTagCrossRef(noteID,tag.tagTitle))
                }
                viewModal.getNote(noteID).observe(this){
                    if (it!=null){
                        viewModal.deleteNote(it)

                    }
                }


                Toast.makeText(this,"Note deleted",Toast.LENGTH_SHORT).show()
                viewModal.deleted.value = true
                goToMain()
            }

        }

        ocrButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this@AddEditNoteActivity,
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestCameraPermission()

            } else {
                CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).start(this@AddEditNoteActivity)

            }
        }

        sttButton.setOnClickListener {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)

            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to text")

            try {
                startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT)
            } catch (e: Exception) {
                Toast
                    .makeText(
                        this@AddEditNoteActivity, e.message,
                        Toast.LENGTH_SHORT
                    )
                    .show()
            }
        }



        archiveButton.setOnClickListener { archiveNote() }

        pinButton.setOnClickListener { pinOrUnPinNote() }

        restoreButton.setOnClickListener { unArchiveNote() }
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                this@AddEditNoteActivity,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestCameraPermission()

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            val result = CropImage.getActivityResult(data)
            if(resultCode == RESULT_OK){
                val resultUri : Uri = result.uri
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(contentResolver,resultUri)
                    recognizeText()
                }catch (e : IOException){
                    e.printStackTrace()
                }
            }
        }
        else if (requestCode == REQUEST_CODE_SPEECH_INPUT) {
            if (resultCode == RESULT_OK && data != null) {
                val result: ArrayList<String> = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)!!
                val noteDesc = result[0]
                noteDescriptionEdit.append("\n")

                noteDescriptionEdit.append(noteDesc)
                viewModal.texChanged.value = true
            }

        }
    }
    private fun requestCameraPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.CAMERA
            )
        ) {
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Permission needed")
                .setMessage("This permission is needed because we need to access your camera")
                .setPositiveButton(
                    "ok"
                ) { _: DialogInterface?, _: Int ->
                    ActivityCompat.requestPermissions(
                        this@AddEditNoteActivity, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_CODE)
                    CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).start(this@AddEditNoteActivity)

                }
                .setNegativeButton(
                    "cancel"
                ) { dialog: DialogInterface, _: Int -> dialog.dismiss() }
                .create().show()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_CAMERA_CODE
            )
        }
    }

    private fun recognizeText(){
        val recognizer = TextRecognizer.Builder(applicationContext).build()
        if (!recognizer.isOperational){
            Toast.makeText(applicationContext,"Sorry recognizer is unavailable",Toast.LENGTH_SHORT).show()
        }else{
            if (bitmap!=null){
                val frame = Frame.Builder().setBitmap(bitmap).build()
                val sparseArray = recognizer.detect(frame) as SparseArray<TextBlock>
                val stringBuilder = StringBuilder()
                for (i in 0 until sparseArray.size()){
                    var textBlock = sparseArray.valueAt(i)
                    stringBuilder.append(textBlock.value)
                    stringBuilder.append("\n")
                }
                noteDescriptionEdit.append(stringBuilder.toString())
            }
        }
    }

    private fun archiveNote(){
        archivedNote = ArchivedNote(noteID)
        viewModal.texChanged.value = true

        viewModal.archived.value = true
        var snackbar = Snackbar.make(coordinatorlayout,"Note Achieved",Snackbar.LENGTH_LONG)
        snackbar.setAction("UNDO"
        ) {
            viewModal.archived.value= false
            snackbar = Snackbar.make(coordinatorlayout,"Note unarchived",Snackbar.LENGTH_SHORT)
        }
        snackbar.show()


    }

    private fun addBulletin(noteContentSplit : List<String>, lineIndex : Int ,noteContent : Editable, prefix: String){
        if (noteContentSplit[lineIndex].startsWith(prefix)){
            if(noteContent.endsWith("\n")){
                noteDescriptionEdit.append("$prefix ")
            }
        }
    }

    private fun unArchiveNote(){
        viewModal.texChanged.value = true

        viewModal.archived.value = false
        var snackbar = Snackbar.make(coordinatorlayout,"Note unarchived",Snackbar.LENGTH_LONG)
        snackbar.setAction("UNDO"
        ) {
            viewModal.archived.value= true
            snackbar = Snackbar.make(coordinatorlayout,"Note archived",Snackbar.LENGTH_SHORT)
        }
        snackbar.show()

    }

    private fun initControllers(){
        cm= Common()
        noteTitleEdit = findViewById(R.id.noteEditTitle)

        layoutManager = LinearLayoutManager(applicationContext,LinearLayoutManager.HORIZONTAL,false)
        calendar = Calendar.getInstance()
        noteDescriptionEdit = findViewById(R.id.noteEditDesc)
        tvTimeStamp = findViewById(R.id.tvTimeStamp)
        tagListRV = findViewById(R.id.tagListRV)
        labelBtn = findViewById(R.id.labelBtn)
        coordinatorlayout = findViewById(R.id.coordinatorlayout)
        alertButton = findViewById(R.id.alertButton)
        addTagBtn = findViewById(R.id.addTagBtn)
        reminderTV = findViewById(R.id.reminderTV)
        reminderIcon = findViewById(R.id.reminderIcon)
        noteID = intent.getLongExtra("noteID",-1)
        noteType = intent.getStringExtra("noteType").toString()
        deleteButton = findViewById(R.id.deleteButton)
        backButton = findViewById(R.id.backButton)
        archiveButton = findViewById(R.id.archiveButton)
        restoreButton = findViewById(R.id.restoreButton)
        ocrButton = findViewById(R.id.ocrButton)
        sttButton = findViewById(R.id.sttButton)
        infoContainer = findViewById(R.id.infoContainer)
        bottomSheet= BottomSheetBehavior.from(infoContainer)
        bottomSheet.state = BottomSheetBehavior.STATE_HIDDEN
        bottomSheet.peekHeight = 72
        optionsButton = findViewById(R.id.optionsFB)
        alertBottomSheet =  BottomSheetDialog(this)
        labelBottomSheet = BottomSheetDialog(this)
        coordinatorlayout = findViewById(R.id.coordinatorlayout)
        pinButton = findViewById(R.id.pinButton)
        layoutManager.orientation = HORIZONTAL
        tagListAdapter= AddEditTagRVAdapter(applicationContext,this)
        labelListAdapter= AddEditLabelAdapter(applicationContext,this)
        labelBottomSheet.setContentView(R.layout.note_label_bottom_sheet)
        delLabelBtn = labelBottomSheet.findViewById(R.id.delLabel)!!
        tagListRV.layoutManager= layoutManager
        tagListRV.adapter = tagListAdapter
        lifecycleOwner = this
        viewModal = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        ).get(NoteViewModel::class.java)

        labelViewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        ).get(LabelViewModel::class.java)

    }

    private fun pinOrUnPinNote(){
        pinnedNote = PinnedNote(noteID)
        viewModal.texChanged.value = true
            if(notePinned){
                viewModal.pinned.value = false
                var snackbar = Snackbar.make(coordinatorlayout,"Note unpinned",Snackbar.LENGTH_LONG)
                snackbar.setAction("UNDO"
                ) {
                    pinnedNote = PinnedNote(noteID)
                    viewModal.pinned.value = true

                    snackbar = Snackbar.make(coordinatorlayout,"Note pinned",Snackbar.LENGTH_SHORT)
                    snackbar.show()
                }
                snackbar.show()
            }else{
                pinnedNote = PinnedNote(noteID)
                viewModal.pinned.value = true

                var snackbar = Snackbar.make(coordinatorlayout,"Note pinned",Snackbar.LENGTH_LONG)
                snackbar.setAction("UNDO"
                ) {
                    viewModal.pinned.value = false

                    snackbar = Snackbar.make(coordinatorlayout,"Note unpinned",Snackbar.LENGTH_SHORT)
                    snackbar.show()
                }
                snackbar.show()
            }



    }

    private fun getTagFromString(p0: CharSequence?, p3: Int) {
        if(p3>0){
            if(!tagListAdapter.deleteIgnored){
                tagListAdapter.deleteIgnored = true
                tagListAdapter.notifyDataSetChanged()

            }

        }
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




    private fun showLabelBottomSheetDialog() {
        labelBottomSheet.show()
        val addNewLabelBtn = labelBottomSheet.findViewById<ImageButton>(R.id.addNewLabel)
        labelListRV = labelBottomSheet.findViewById(R.id.labelRV)!!
        val layoutManager = LinearLayoutManager(applicationContext,LinearLayoutManager.HORIZONTAL,false)

        labelListRV.layoutManager =layoutManager
        labelListRV.adapter = labelListAdapter
        addNewLabelBtn?.setOnClickListener {
            val labelDialog: AlertDialog = this.let {
                val builder = AlertDialog.Builder(it)
                builder.apply {
                    setPositiveButton("ok"
                    ) { _, _ ->

                    }
                    setNegativeButton("cancel"
                    ) { _, _ ->

                    }
                    setView(R.layout.add_label_dialog)
                    setTitle("Choose a label color")
                }
                builder.create()

            }

            labelDialog.show()
            colorPickerView = labelDialog.findViewById<ColorPickerView>(R.id.colorPicker)
            colorPickerView.addOnColorSelectedListener{
                val hex = ColorTransparentUtils.transparentColor(it,30)
                label = Label(noteID,Color.parseColor(hex))
                viewModal.labelSet.value = true
                viewModal.texChanged.value = true
                coordinatorlayout.setBackgroundColor(Color.parseColor(hex))
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                window.statusBarColor = Color.parseColor(hex)
                bottomSheet.state = BottomSheetBehavior.STATE_HIDDEN
            }


        }

    }

    private fun manipulateNoteDescLines() {
        coordinatorlayout.viewTreeObserver.addOnGlobalLayoutListener { ViewTreeObserver.OnGlobalLayoutListener {
            val r = Rect()
            coordinatorlayout.getWindowVisibleDisplayFrame(r)
            val screenHeight =  coordinatorlayout.rootView.height
            val keypadHeight = screenHeight - r.bottom
            if (keypadHeight > screenHeight * 0.15) { // 0.15 ratio is perhaps enough to determine keypad height.
                if (!isKeyBoardShowing) {
                    isKeyBoardShowing = true
                    bottomSheet.state = BottomSheetBehavior.STATE_HIDDEN
                }
            }
            else {
                if (isKeyBoardShowing) {

                    isKeyBoardShowing = false

                }
            }

        } }
    }

    private fun showAlertSheetDialog() {
        alertBottomSheet.setContentView(R.layout.alert_bottom_sheet)
        alertBottomSheet.show()
        val currentHr = calendar.get(Calendar.HOUR_OF_DAY)
        val opt1 = alertBottomSheet.findViewById<View>(R.id.auto1)
        val opt2 = alertBottomSheet.findViewById<View>(R.id.auto2)
        val opt3 = alertBottomSheet.findViewById<View>(R.id.auto3)
        val opt1Desc = alertBottomSheet.findViewById<TextView>(R.id.date1)
        val opt2Desc = alertBottomSheet.findViewById<TextView>(R.id.date2)
        val opt3Desc = alertBottomSheet.findViewById<TextView>(R.id.date3)
        val customDT = alertBottomSheet.findViewById<View>(R.id.customDateTime)
        opt2Desc?.text = resources.getString(R.string.opt2n3Desc,"morning","8:00am")
        opt3Desc?.text = resources.getString(R.string.opt2n3Desc,"evening","6:00pm")


        customDT?.setOnClickListener {
            openDateTimeDialog()
        }
        when(currentHr){
            in 0..4 -> {
                opt1Desc?.text = resources.getString(R.string.opt1Desc,"8:00am")
                opt1?.visibility = VISIBLE

            }
            in 5..8 -> {
                opt1Desc?.text = resources.getString(R.string.opt1Desc,"2:00pm")

                opt1?.visibility = VISIBLE

            }
            in 9..14 ->{
                opt1Desc?.text = resources.getString(R.string.opt1Desc,"6:00pm")
                opt1?.visibility = VISIBLE

            }
            in 15..18 -> {
                opt1Desc?.text = resources.getString(R.string.opt1Desc,"8:00pm")
                opt1?.visibility = VISIBLE

            }
            in 19..23->  opt1?.visibility = GONE

        }

        opt1?.setOnClickListener {
            if(noteDescriptionEdit.length() > 0 || noteTitleEdit.length() >0 ){
                viewModal.texChanged.value = true

            }
            alertBottomSheet.dismiss()
            when(currentHr){
                in 0..4 -> {
                    calendar[Calendar.HOUR_OF_DAY] = 8
                    calendar[Calendar.MINUTE] = 0
                    Toast.makeText(this, "Reminder set for today at 8:00 am", Toast.LENGTH_SHORT).show()

                }
                in 5..8 -> {
                    calendar[Calendar.HOUR_OF_DAY] = 14
                    calendar[Calendar.MINUTE] = 0
                    Toast.makeText(this, "Reminder set for today at 2:00 pm", Toast.LENGTH_SHORT).show()

                }
                in 9..14 ->{
                    calendar[Calendar.HOUR_OF_DAY] = 18
                    calendar[Calendar.MINUTE] = 0
                    Toast.makeText(this, "Reminder set for today at 6:00 pm", Toast.LENGTH_SHORT).show()

                }
                in 15..18 -> {
                    calendar[Calendar.HOUR_OF_DAY] = 20
                    calendar[Calendar.MINUTE] = 0
                    Toast.makeText(this, "Reminder set for today at 8:00 pm", Toast.LENGTH_SHORT).show()

                }
            }
            reminder = Reminder(noteID, calendar.timeInMillis)
            viewModal.reminderSet.value = true

//            viewModal.insertReminder(Reminder(noteID, c.timeInMillis))
        }

        opt2?.setOnClickListener {
            alertBottomSheet.dismiss()
            opt2Desc?.text = resources.getString(R.string.opt2n3Desc,"morning","8:00am")
            calendar.add(Calendar.DAY_OF_MONTH,1)
            calendar[Calendar.HOUR_OF_DAY] = 8
            calendar[Calendar.MINUTE] = 0
            if(noteDescriptionEdit.length() > 0 || noteTitleEdit.length() >0 ){
                viewModal.texChanged.value = true

            }
            Toast.makeText(this, "Reminder set for tomorrow at 8:00am", Toast.LENGTH_SHORT).show()

//            viewModal.insertReminder(Reminder(noteID, c.timeInMillis))
            reminder = Reminder(noteID, calendar.timeInMillis)
            viewModal.reminderSet.value = true


        }
        opt3?.setOnClickListener {
            alertBottomSheet.dismiss()
            calendar.add(Calendar.DAY_OF_MONTH,1)
            calendar[Calendar.HOUR_OF_DAY] = 18
            calendar[Calendar.MINUTE] = 0
            if(noteDescriptionEdit.length() > 0 || noteTitleEdit.length() >0 ){
                viewModal.texChanged.value = true

            }
            reminder = Reminder(noteID, calendar.timeInMillis)
            viewModal.reminderSet.value = true

            Toast.makeText(this, "Reminder set for tomorrow at 6:00pm", Toast.LENGTH_SHORT).show()

        }
    }




    private fun openDateTimeDialog(){
        val alertDialog: AlertDialog? = this?.let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setPositiveButton("ok"
                ) { _, _ ->
                    // User clicked OK button
                    if(noteDescriptionEdit.length() > 0 || noteTitleEdit.length() >0 ){
                        viewModal.texChanged.value = true

                    }
                    Toast.makeText(context, "Reminder set", Toast.LENGTH_SHORT).show()
                    reminder = Reminder(noteID, calendar.timeInMillis)
                    viewModal.reminderSet.value = true
                    alertBottomSheet.dismiss()

                }
                setNegativeButton("cancel"
                ) { _, _ ->
                    alertBottomSheet.dismiss()

                }
                setView(R.layout.custom_datetime_dialog)
                setTitle("Choose date and time")
            }
            builder.create()
        }
        alertDialog?.show()
        val timePickerBtn=alertDialog?.findViewById<View>(R.id.timePickButton)
        val datePickerBtn = alertDialog?.findViewById<ImageButton>(R.id.datePickButton)
        timeTitleTV = alertDialog?.findViewById(R.id.timeTitle)!!
        dateTitleTV = alertDialog.findViewById(R.id.dateTitle)

        timePickerBtn?.setOnClickListener {
            TimePickerFragment(this).show(supportFragmentManager,"timePicker")
        }
        datePickerBtn?.setOnClickListener {
            val newFragment = DatePickerFragment(this,this)
            newFragment.show(supportFragmentManager, "datePicker")
        }



    }

    private fun startAlarm() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlertReceiver::class.java)
        viewModal.texChanged.value = true
        val noteTitle = noteTitleEdit.text.toString()
        val noteDescription = noteDescriptionEdit.text.toString()
        if (noteTitle.isNotEmpty()){
            intent.putExtra("noteTitle",noteTitle)
        }
        if (noteDescription.isNotEmpty()){
            intent.putExtra("noteDesc",noteDescription)
        }
        intent.putExtra("noteID",noteID)
        intent.putExtra("noteType","Edit")

        val pendingIntent = PendingIntent.getBroadcast(this, noteID.toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT)
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
    }

    private fun cancelAlarm(){
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlertReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, noteID.toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT)
        alarmManager.cancel(pendingIntent)
    }

    private fun saveNote(){
        val noteTitle = noteTitleEdit.text.toString()
        val noteDescription = noteDescriptionEdit.text.toString()
        val currentDate= cm.currentTimeToLong()
        if(!deletable){
            if(textChanged){
                if (noteTitle.isNotEmpty() || noteDescription.isNotEmpty()){
                    Log.d(TAG, "saveNote: $noteID")
                    val note = Note(noteTitle,noteDescription,currentDate)
                    if(noteType == "Edit"){
                        note.noteID = noteID
                        viewModal.updateNote(note)
                        saveOtherEntities()
                        Toast.makeText(this,"Note updated .. " , Toast.LENGTH_SHORT).show()

                    }else{
                        lifecycleScope.launch {
                           noteID =  viewModal.addNote(note)
                            saveOtherEntities()

                        }


                        Toast.makeText(this,"Note added .. " , Toast.LENGTH_SHORT).show()
                        
                    }


                }


            }
        }
        val intent = Intent(this@AddEditNoteActivity, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP;

        startActivity(intent)

        finish()
    }


    private fun saveOtherEntities(){
        if (pinnedNote!=null){
            pinnedNote?.noteID = noteID
            if (notePinned){
                viewModal.pinNote(pinnedNote!!)
            }else{
                viewModal.removePin(pinnedNote!!)
            }
        }
        if (label!=null){
            label?.noteID = noteID
            if (labelNoteSet){
                viewModal.insertLabel(label!!)
            }else{
                viewModal.deleteNoteLabel(noteID)
            }
        }
        if (archivedNote!=null){
            archivedNote?.noteID = noteID

            if (archived){
                viewModal.archiveNote(archivedNote!!)
            }else{
                viewModal.removeArchive(archivedNote!!)
            }
        }
        if (reminder!=null){
            reminder?.noteID = noteID

            if (reminderNoteSet){
                Log.d(TAG, "saveNote: Reminder set")
                startAlarm()
                viewModal.insertReminder(reminder!!)
            }else{
                viewModal.deleteReminder(noteID)
                cancelAlarm()
            }
        }

        for(tag in viewModal.tagList){

            val crossRef = NoteTagCrossRef(noteID,tag.tagTitle)
            viewModal.insertNoteTagCrossRef(crossRef)
        }


    }


    private fun goToMain() {
        
        saveNote()


    }

    override fun onBackPressed() {

        super.onBackPressed()
        goToMain()

    }

    override fun deleteTag(tag: Tag) {
        viewModal.tagList.remove(tag)
        if (noteType == "Edit"){
            viewModal.deleteNoteTagCrossRef(NoteTagCrossRef(noteID,tag.tagTitle))
        }
        tagListAdapter.updateList(viewModal.tagList)

    }

    override fun getTimeInfo(calendar : Calendar) {

        this.calendar[Calendar.HOUR]= calendar[Calendar.HOUR]
        this.calendar[Calendar.MINUTE]= calendar[Calendar.MINUTE]
        this.calendar[Calendar.SECOND]= calendar[Calendar.SECOND]
        timeTitleTV.text="Time set:" + DateFormat.getTimeFormat(this).format(calendar.time)
    }

    override fun getDateInfo(calendar : Calendar) {
        this.calendar[Calendar.DAY_OF_MONTH] = calendar[Calendar.DAY_OF_MONTH]
        this.calendar[Calendar.MONTH] = calendar[Calendar.MONTH]
        this.calendar[Calendar.YEAR] = calendar[Calendar.YEAR]
        dateTitleTV.text="Date set:" + DateFormat.getDateFormat(this).format(calendar.time)


    }

    override fun getTag(tag: Tag) {
        viewModal.addTag(tag)
        viewModal.addTagToList(tag)
        tagListAdapter.updateList(viewModal.tagList)
        viewModal.noteChanged(true)

    }

    override fun onLabelItemClick(labelID: Int) {
        label = Label(noteID,labelID)
        viewModal.labelSet.value = true
        viewModal.texChanged.value = true
        coordinatorlayout.setBackgroundColor(labelID)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.statusBarColor = labelID
    }


}