package com.neuralbit.letsnote

import android.Manifest
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
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
import androidx.core.view.isVisible
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
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.neuralbit.letsnote.adapters.AddEditLabelAdapter
import com.neuralbit.letsnote.adapters.AddEditTagRVAdapter
import com.neuralbit.letsnote.adapters.LabelClickInterface
import com.neuralbit.letsnote.adapters.TagRVInterface
import com.neuralbit.letsnote.entities.NoteFireIns
import com.neuralbit.letsnote.ui.label.LabelViewModel
import com.neuralbit.letsnote.utilities.*
import kotlinx.coroutines.launch
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
    private lateinit var deleteButton: ImageButton
    private lateinit var backButton: ImageButton
    private lateinit var pinButton: ImageButton
    private lateinit var alertButton: ImageButton
    private lateinit var dismissTodoButton: ImageButton
    private lateinit var ocrButton: ImageButton
    private lateinit var sttButton: ImageButton
    private lateinit var addTodoButton: ImageButton
    private lateinit var undoButton: ImageButton
    private lateinit var redoButton: ImageButton
    private lateinit var noteTitleEdit : EditText
    private lateinit var noteDescriptionEdit : MultiAutoCompleteTextView
    private lateinit var addTagBtn : ImageButton
    private lateinit var delLabelBtn : ImageButton
    private lateinit var reminderIcon : ImageView
    private lateinit var reminderTV : TextView
    private var noteID : Long= -1
    private var noteUid : String? = null
    private lateinit var viewModal : NoteViewModel
    private lateinit var labelViewModel : LabelViewModel
    private var noteDescOrig : String? = null
    private var noteDescOrigList = ArrayList<String>()
    private var noteDescNew : String? = null
    private lateinit var noteType : String
    private lateinit var tvTimeStamp : TextView
    private var textChanged : Boolean = false
    private var archived = false
    private lateinit var cm : Common
    private var noteDesc : String? = null
    private var noteTitle : String? = null
    private var noteTimeStamp : Long = 0
    private lateinit var coordinatorlayout : View
    private var backPressed  = false
    private lateinit var tagListRV : RecyclerView
    private lateinit var labelListRV : RecyclerView
    private lateinit var todoRV : RecyclerView
    private lateinit var todoCheckBox: CheckBox
    private lateinit var todoItemDescTV : EditText
    private var isKeyBoardShowing = false
    private lateinit var tagListAdapter : AddEditTagRVAdapter
    private lateinit var labelListAdapter : AddEditLabelAdapter
    private lateinit var alertBottomSheet : BottomSheetDialog
    private lateinit var labelBottomSheet : BottomSheetDialog
    private lateinit var labelBtn : ImageButton
    var TAG = "AddEditNoteActivity"
    private val REQUEST_CAMERA_CODE = 100
    private lateinit var lifecycleOwner : LifecycleOwner
    private lateinit var calendar: Calendar
    private lateinit var timeTitleTV :TextView
    private lateinit var dateTitleTV :TextView
    private lateinit var layoutManager : LinearLayoutManager
    private var notePinned = false
    private var reminderNoteSet = false
    private lateinit var infoContainer : View
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
                    viewModal.noteChanged.value = true
                    noteDescriptionEdit.setText(it)
                }
            }
        }

        manipulateNoteDescLines()
        viewModal.allFireLabels().observe(lifecycleOwner){
            val labelColors = HashSet<Int>()
            for (l in it){
                labelColors.add(l.labelColor)
            }
            labelListAdapter.updateLabelIDList(labelColors)
        }




        addTagBtn.setOnClickListener {
            val addTagDialog = AddTagDialog(this,applicationContext)
            addTagDialog.tagList = viewModal.oldTagList
            addTagDialog.show(supportFragmentManager,"addTagDialogs")

        }
        when (noteType) {
            "Edit" -> {
                noteTitleEdit.setText(noteTitle)
                viewModal.pinned.value = notePinned

                noteDescriptionEdit.setText(noteDesc)
                tvTimeStamp.text= getString(R.string.timeStamp,cm.convertLongToTime(noteTimeStamp)[0],cm.convertLongToTime(noteTimeStamp)[1])
                tvTimeStamp.visibility =VISIBLE
                val labelColor = viewModal.labelColor
                if(labelColor > 0){
                    coordinatorlayout.setBackgroundColor(labelColor)
                    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                    window.statusBarColor = labelColor
                    delLabelBtn.visibility = VISIBLE
                }else{
                    coordinatorlayout.setBackgroundColor(Color.TRANSPARENT)
                    window.statusBarColor = getColor(R.color.gunmetal)
                    delLabelBtn.visibility = GONE
                }
                tagListAdapter.updateList(viewModal.oldTagList)
            }
            else -> {

                tvTimeStamp.visibility =GONE
            }
        }

        delLabelBtn.setOnClickListener {
            viewModal.noteChanged.value = true
            viewModal.labelChanged = true
            viewModal.labelColor = 0


        }



        viewModal.reminderSet.observe(lifecycleOwner){
            val reminderTime = viewModal.reminderTime
            reminderNoteSet = it
            if(it){
                alertButton.setImageResource(R.drawable.ic_baseline_add_alert_24)


                reminderTV.text = resources.getString(R.string.reminder,cm.convertLongToTime(reminderTime)[0],cm.convertLongToTime(reminderTime)[1])
                val c = Calendar.getInstance()
                if (c.timeInMillis > reminderTime){
                    cancelAlarm(viewModal.reminderTime.toInt())
                    reminderTV.visibility =  GONE
                    reminderIcon.visibility = GONE
                }else{
                    reminderTV.visibility =  VISIBLE
                    reminderIcon.visibility = VISIBLE
                }
            }else{
                alertButton.setBackgroundResource(R.drawable.ic_outline_add_alert_24)
                reminderTV.visibility =  GONE
                reminderIcon.visibility = GONE
            }
        }

        viewModal.pinned.observe(lifecycleOwner){
            notePinned = it

            if (!it){
                pinButton.setImageResource(R.drawable.ic_outline_push_pin_24)
            }else{
                pinButton.setImageResource(R.drawable.ic_baseline_push_pin_24)
            }
        }

        viewModal.deletedNote.observe(lifecycleOwner){
            if (it) {

                pinButton.visibility = GONE
                archiveButton.visibility = GONE
                alertButton.visibility = GONE
                restoreButton.visibility = VISIBLE
                noteDescriptionEdit.isEnabled = false
                noteTitleEdit.isEnabled = false
                infoContainer.visibility = GONE
                reminderTV.visibility =  GONE
                reminderIcon.visibility = GONE
            } else {
                pinButton.visibility = VISIBLE
                archiveButton.visibility = VISIBLE
                alertButton.visibility = VISIBLE
                restoreButton.visibility = GONE
                infoContainer.visibility = VISIBLE
                noteDescriptionEdit.isEnabled = true
                noteTitleEdit.isEnabled = true
                if (viewModal.reminderSet.value == true){
                    reminderTV.visibility =  VISIBLE
                    reminderIcon.visibility = VISIBLE
                }

            }
        }
        viewModal.archived.observe(lifecycleOwner){
            archived = it

            if (viewModal.deletedNote.value != true){
                if (it) {

                    pinButton.visibility = GONE
                    archiveButton.visibility = GONE
                    alertButton.visibility = GONE
                    restoreButton.visibility = VISIBLE
                    noteDescriptionEdit.isEnabled = false
                    noteTitleEdit.isEnabled = false
                    infoContainer.visibility = GONE
                } else {
                    pinButton.visibility = VISIBLE
                    archiveButton.visibility = VISIBLE
                    alertButton.visibility = VISIBLE
                    restoreButton.visibility = GONE
                    infoContainer.visibility = VISIBLE
                    noteDescriptionEdit.isEnabled = true
                    noteTitleEdit.isEnabled = true

                }
            }
        }

        alertButton.setOnClickListener {
            val reminderTime = viewModal.reminderTime
            if (reminderTime == (0).toLong()){
                showAlertSheetDialog()

            }else{
                cancelAlarm(reminderTime.toInt())
                viewModal.reminderTime = 0
            }
        }
        
        val tagListStr = ArrayList<String>()
        viewModal.allFireTags().observe(this){
            tagListStr.clear()
            for (tag in it){
                tagListStr.add(tag.tagName)
            }
            val adapter = ArrayAdapter(applicationContext,android.R.layout.simple_dropdown_item_1line,tagListStr)
            noteDescriptionEdit.setAdapter(adapter)
            noteDescriptionEdit.setTokenizer(SpaceTokenizer())
            
        }


        viewModal.backPressed.observe(this) { 
            backPressed = it 
            viewModal.undoMode.value = it
        }   


        viewModal.newTagTyped.observe(this){
            val tags = HashSet<String>()
            tags.addAll(viewModal.oldTagList)
            tags.addAll(viewModal.newTags)
            tagListAdapter.updateList(ArrayList(tags))


        }



        labelBtn.setOnClickListener {
            showLabelBottomSheetDialog()
        }
        undoButton.setOnClickListener {
            noteDescNew = noteDescriptionEdit.text.toString()
            noteDescriptionEdit.setText(noteDescOrig)
            viewModal.undoMode.value = false
            redoButton.isEnabled = true

        }
        viewModal.undoMode.value = false
        viewModal.undoMode.observe(lifecycleOwner){
            if (it){
                undoButton.isEnabled = true
                redoButton.isEnabled = false
            }else{
                undoButton.isEnabled = false
                redoButton.isEnabled = false
            }
        }
        redoButton.setOnClickListener {
            noteDescriptionEdit.setText(noteDescNew)
            redoButton.isEnabled = false
            undoButton.isEnabled = true
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

                if (!backPressed ) {
                    noteDescOrigList.clear()
                    noteDescOrig = p0?.toString()
                    noteDescOrig?.split(" ")?.let { noteDescOrigList.addAll(it) }
                }

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(p0?.length!! > 0){

                    noteListBullet()

                    getTagFromString(p0)

                }

            }

            override fun afterTextChanged(p0: Editable?) {

            }
        })


        noteDescriptionEdit.setOnKeyListener { _, key, _ ->
            viewModal.noteChanged.value = true
            viewModal.backPressed.value = key == KeyEvent.KEYCODE_DEL

            false
        }

        noteTitleEdit.setOnKeyListener { _, _, _ ->
            viewModal.noteChanged.value = true

            false
        }

        viewModal.noteChanged.observe(this) {
            textChanged = it
            if(it){
                tvTimeStamp.visibility = GONE
            }else{
                tvTimeStamp.visibility = VISIBLE

            }
        }



        backButton.setOnClickListener { goToMain() }

        deleteButton.setOnClickListener {

            val alertDialog: AlertDialog? = this.let {
                val builder = AlertDialog.Builder(this)
                builder.apply {
                    setPositiveButton("ok"
                    ) { _, _ ->
                        viewModal.deletedNote.value = true
                        viewModal.noteChanged.value = true
                        goToMain()
                        }

                    setNegativeButton("cancel"
                    ) { _, _ ->
                    }
                    setTitle("Delete Note")

                }
                builder.create()
            }

            alertDialog?.show()

        }

        ocrButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this@AddEditNoteActivity,
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestCameraPermission()

            } else {
                //TODO FIX OCR FUNCTION
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

        restoreButton.setOnClickListener {
            unArchiveNote()
            unDelete()
        }
        dismissTodoButton.setOnClickListener {
            todoItemDescTV.text.clear()
            todoItemDescTV.visibility = GONE
            todoCheckBox.visibility = GONE
            dismissTodoButton.visibility = GONE
        }
        addTodoButton.setOnClickListener {
            if (!todoItemDescTV.isVisible){
                todoRV.visibility = VISIBLE
                todoItemDescTV.visibility = VISIBLE
                todoCheckBox.visibility = VISIBLE
                dismissTodoButton.visibility = VISIBLE
            }else{
//                val todoDesc = todoItemDescTV.text.toString()
//                val isItemChecked = todoCheckBox.isChecked
//                viewModal.noteChanged.value = true
//                todoItemDescTV.text.clear()
//                todoAdapter.getTodoItems(todoItems)

            }


        }
    }

    private fun noteListBullet() {
        val noteContent = noteDescriptionEdit.text
        val noteContentSplit = noteContent.split("\n")
        var lineIndex = 0
        if (noteContentSplit.size > 2) {
            lineIndex = noteContentSplit.lastIndex - 1
        }

        if (noteContentSplit.size > 1) {

            val prefix = listOf(" ", "->", "-", "+", "*", ">")
            for (p in prefix) {
                if (!backPressed) {
                    addBulletin(noteContentSplit, lineIndex, noteContent, p)

                }
            }
            if (noteContentSplit[lineIndex].endsWith(":")) {
                if (noteContent.endsWith("\n")) {
                    if (!backPressed) {
                        noteDescriptionEdit.append("-> ")

                    }
                }
            }




        }
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
//        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
//            val result = CropImage.getActivityResult(data)
//            if(resultCode == RESULT_OK){
//                val resultUri : Uri = result.uri
//                try {
//                    bitmap = MediaStore.Images.Media.getBitmap(contentResolver,resultUri)
//                    recognizeText()
//                }catch (e : IOException){
//                    e.printStackTrace()
//                }
//            }
//        }
        if (requestCode == REQUEST_CODE_SPEECH_INPUT) {
            if (resultCode == RESULT_OK && data != null) {
                val result: ArrayList<String> = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)!!
                val noteDesc = result[0]
                noteDescriptionEdit.append("\n")

                noteDescriptionEdit.append(noteDesc)
                viewModal.noteChanged.value = true
            }

        }
    }

    private fun requestCameraPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.CAMERA
            )
        ) {
            AlertDialog.Builder(this)
                .setTitle("Permission needed")
                .setMessage("This permission is needed because we need to access your camera")
                .setPositiveButton(
                    "ok"
                ) { _: DialogInterface?, _: Int ->
                    ActivityCompat.requestPermissions(this@AddEditNoteActivity, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_CODE)

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
                    val textBlock = sparseArray.valueAt(i)
                    stringBuilder.append(textBlock.value)
                    stringBuilder.append("\n")
                }
                noteDescriptionEdit.append(stringBuilder.toString())
            }
        }
    }

    private fun archiveNote(){
        viewModal.noteChanged.value = true

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
        viewModal.noteChanged.value = true
        if(viewModal.archived.value == true) {
            viewModal.archived.value = false
            var snackbar = Snackbar.make(coordinatorlayout, "Note unarchived", Snackbar.LENGTH_LONG)
            snackbar.setAction(
                "UNDO"
            ) {
                viewModal.archived.value = true
                snackbar = Snackbar.make(coordinatorlayout, "Note archived", Snackbar.LENGTH_SHORT)
            }
            snackbar.show()
        }
    }
    private fun unDelete(){
        viewModal.noteChanged.value = true
        if(viewModal.deletedNote.value == true) {
            viewModal.deletedNote.value = false
            var snackbar = Snackbar.make(coordinatorlayout, "Note recovered", Snackbar.LENGTH_LONG)
            snackbar.setAction(
                "UNDO"
            ) {
                viewModal.deletedNote.value = true
                snackbar = Snackbar.make(coordinatorlayout, "Note deleted", Snackbar.LENGTH_SHORT)
            }
            snackbar.show()
        }
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
        intent.putExtra("noteType","Edit")
        noteTitle = intent.getStringExtra("noteTitle")
        noteUid = intent.getStringExtra("noteUid")
        noteDesc = intent.getStringExtra("noteDescription")
        notePinned = intent.getBooleanExtra("pinned",false)
        archived = intent.getBooleanExtra("archieved",false)
        val deleted = intent.getBooleanExtra("deleted",false)
        val tagIntentList = intent.getStringArrayListExtra("tagList")
        val labelColor = intent.getIntExtra("labelColor",0)
        val reminderTime = intent.getLongExtra("reminder",0)
        noteTimeStamp = intent.getLongExtra("timeStamp",-1)
        deleteButton = findViewById(R.id.deleteButton)
        backButton = findViewById(R.id.backButton)
        archiveButton = findViewById(R.id.archiveButton)
        restoreButton = findViewById(R.id.restoreButton)
        ocrButton = findViewById(R.id.ocrButton)
        sttButton = findViewById(R.id.sttButton)
        undoButton = findViewById(R.id.undoButton)
        redoButton = findViewById(R.id.redoButton)
        infoContainer = findViewById(R.id.infoContainer)
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
        addTodoButton = findViewById(R.id.addTodo)
        dismissTodoButton = findViewById(R.id.dismissTodoBtn)
        todoRV = findViewById(R.id.todoRV)
        val layoutManagerTodo = LinearLayoutManager(applicationContext,LinearLayoutManager.VERTICAL,false)
        todoRV.layoutManager = layoutManagerTodo
        todoCheckBox = findViewById(R.id.todoCheckBox)
        todoItemDescTV = findViewById(R.id.todoItemDescTV)
        lifecycleOwner = this
        viewModal = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        ).get(NoteViewModel::class.java)

        labelViewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        ).get(LabelViewModel::class.java)
        if (tagIntentList != null){
            viewModal.oldTagList = tagIntentList
        }
        viewModal.reminderTime = reminderTime
        if (reminderTime > (0).toLong()){
            viewModal.reminderSet.value = true
        }
        viewModal.labelColor = labelColor
        viewModal.archived.value = archived
        viewModal.deletedNote.value = deleted
    }

    private fun pinOrUnPinNote(){
        viewModal.noteChanged.value = true
        if(notePinned){
            viewModal.pinned.value = false
            var snackbar = Snackbar.make(coordinatorlayout,"Note unpinned",Snackbar.LENGTH_LONG)
            snackbar.setAction("UNDO"
            ) {
                viewModal.pinned.value = true

                snackbar = Snackbar.make(coordinatorlayout,"Note pinned",Snackbar.LENGTH_SHORT)
                snackbar.show()
            }
            snackbar.show()
        }else{
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

    private fun getTagFromString(p0: CharSequence?) {
        val strL = p0?.toString()?.split(" ")
        if (!backPressed) {
            if (strL != null && strL.size > 1) {
                val word = strL[strL.lastIndex - 1]
                if (word.contains("#")) {
                    if (!viewModal.oldTagList.contains(word)){
                        viewModal.newTags.add(word)
                        viewModal.newTagTyped.value = true
                    }
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
            colorPickerView = labelDialog.findViewById(R.id.colorPicker)
            colorPickerView.addOnColorSelectedListener{
                val hex = ColorTransparentUtils.transparentColor(it,30)
                viewModal.labelColor = Color.parseColor(hex)
                viewModal.labelChanged = true
                viewModal.noteChanged.value = true
                coordinatorlayout.setBackgroundColor(Color.parseColor(hex))
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                window.statusBarColor = Color.parseColor(hex)
            }


        }

    }

    private fun manipulateNoteDescLines() {
        coordinatorlayout.viewTreeObserver.addOnGlobalLayoutListener { ViewTreeObserver.OnGlobalLayoutListener {
            val r = Rect()
            coordinatorlayout.getWindowVisibleDisplayFrame(r)
            val screenHeight =  coordinatorlayout.rootView.height
            val keypadHeight = screenHeight - r.bottom
            if (keypadHeight > screenHeight * 0.15) {
                if (!isKeyBoardShowing) {
                    isKeyBoardShowing = true
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
                viewModal.noteChanged.value = true
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
            viewModal.reminderTime = calendar.timeInMillis
            viewModal.reminderSet.value = true

        }

        opt2?.setOnClickListener {
            alertBottomSheet.dismiss()
            opt2Desc?.text = resources.getString(R.string.opt2n3Desc,"morning","8:00am")
            calendar.add(Calendar.DAY_OF_MONTH,1)
            calendar[Calendar.HOUR_OF_DAY] = 8
            calendar[Calendar.MINUTE] = 0
            if(noteDescriptionEdit.length() > 0 || noteTitleEdit.length() >0 ){
                viewModal.noteChanged.value = true

            }
            Toast.makeText(this, "Reminder set for tomorrow at 8:00am", Toast.LENGTH_SHORT).show()

            viewModal.reminderTime = calendar.timeInMillis
            viewModal.reminderSet.value = true


        }
        opt3?.setOnClickListener {
            alertBottomSheet.dismiss()
            calendar.add(Calendar.DAY_OF_MONTH,1)
            calendar[Calendar.HOUR_OF_DAY] = 18
            calendar[Calendar.MINUTE] = 0
            if(noteDescriptionEdit.length() > 0 || noteTitleEdit.length() >0 ){
                viewModal.noteChanged.value = true

            }
            viewModal.reminderTime = calendar.timeInMillis
            viewModal.reminderSet.value = true

            Toast.makeText(this, "Reminder set for tomorrow at 6:00pm", Toast.LENGTH_SHORT).show()

        }
    }




    private fun openDateTimeDialog(){
        val alertDialog: AlertDialog? = this.let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setPositiveButton("ok"
                ) { _, _ ->
                    if(noteDescriptionEdit.length() > 0 || noteTitleEdit.length() >0 ){
                        viewModal.noteChanged.value = true

                    }
                    Toast.makeText(context, "Reminder set", Toast.LENGTH_SHORT).show()
                    viewModal.reminderTime = calendar.timeInMillis
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

    private fun startAlarm(requestCode: Int) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlertReceiver::class.java)
        viewModal.noteChanged.value = true
        val noteTitle = noteTitleEdit.text.toString()
        val noteDescription = noteDescriptionEdit.text.toString()
        if (noteTitle.isNotEmpty()){
            intent.putExtra("noteTitle",noteTitle)
        }
        if (noteDescription.isNotEmpty()){
            intent.putExtra("noteDesc",noteDescription)
        }
        intent.putExtra("noteUid",noteUid)
        intent.putExtra("timeStamp",System.currentTimeMillis())
        intent.putExtra("labelColor",viewModal.labelColor)
        intent.putExtra("pinned",viewModal.pinned.value)
        intent.putExtra("archieved",viewModal.archived.value)
        val tags = ArrayList<String>()
        tags.addAll(viewModal.oldTagList)
        tags.addAll(viewModal.newTags)
        tags.removeAll(viewModal.deletedTags.toSet())
        intent.putStringArrayListExtra("tagList", tags)
        intent.putExtra("noteType","Edit")

        val pendingIntent = PendingIntent.getBroadcast(this, requestCode, intent, PendingIntent.FLAG_IMMUTABLE)
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
    }

    private fun cancelAlarm(requestCode: Int){
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlertReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this,requestCode , intent, PendingIntent.FLAG_IMMUTABLE)
        alarmManager.cancel(pendingIntent)
    }

    private fun saveNote(){
        val noteTitle = noteTitleEdit.text.toString()
        val noteDescription = noteDescriptionEdit.text.toString()
        val currentDate= cm.currentTimeToLong()
        val pref = applicationContext.getSharedPreferences("DeletedNotes", MODE_PRIVATE)


        if(textChanged){
            if (noteTitle.isNotEmpty() || noteDescription.isNotEmpty()){
                Log.d(TAG, "saveNote: ${viewModal.deletedNote.value}")
                if (viewModal.deletedNote.value != true){
                    if(noteType == "Edit"){
                        val noteUpdate = HashMap<String,Any>()
                        noteUpdate["title"] = noteTitle
                        noteUpdate["description"] = noteDescription
                        noteUpdate["timeStamp"] = currentDate
                        if (viewModal.labelChanged){
                            noteUpdate["label"] = viewModal.labelColor
                        }
                        noteUpdate["pinned"] = notePinned
                        noteUpdate["archived"] = archived
                        noteUpdate["reminderDate"] = viewModal.reminderTime
                        val tags = ArrayList<String>()
                        tags.addAll(viewModal.oldTagList)
                        tags.addAll(viewModal.newTags)
                        tags.removeAll(viewModal.deletedTags.toSet())
                        noteUpdate["tags"] = tags
                        noteUid?.let { viewModal.updateFireNote(noteUpdate, it) }

                        if (!archived){
                            saveOtherEntities()

                            Toast.makeText(this,"Note updated .. " , Toast.LENGTH_SHORT).show()
                        }
                        val editor: SharedPreferences.Editor = pref.edit()
                        val noteUids = pref.getStringSet("noteUids",HashSet())
                        val deletedNoteUids = HashSet<String>()
                        if (noteUids != null){
                            deletedNoteUids.addAll(noteUids)
                            noteUid?.let { deletedNoteUids.remove(it) }
                        }else{
                            noteUid?.let { deletedNoteUids.remove(it) }
                        }
                        editor.putStringSet("noteUids",deletedNoteUids)
                        editor.apply()

                    }else{
                        lifecycleScope.launch {
                            val noteFire = NoteFireIns(noteTitle, noteDescription, currentDate)
                            val tags = ArrayList<String>()
                            tags.addAll(viewModal.oldTagList)
                            tags.addAll(viewModal.newTags)
                            tags.removeAll(viewModal.deletedTags.toSet())
                            noteFire.tags = ArrayList(tags)
                            noteFire.reminderDate = viewModal.reminderTime
                            noteFire.pinned = notePinned
                            noteFire.label = viewModal.labelColor
                            noteUid =  viewModal.addFireNote(noteFire)
                            saveOtherEntities()


                        }
                        Toast.makeText(this,"Note added .. " , Toast.LENGTH_SHORT).show()

                    }
                }else{
                    val editor: SharedPreferences.Editor = pref.edit()
                    val noteUids = pref.getStringSet("noteUids",HashSet())
                    val deletedNoteUids = HashSet<String>()
                    if (noteUids != null){
                        deletedNoteUids.addAll(noteUids)
                        noteUid?.let { deletedNoteUids.add(it) }
                    }else{
                        noteUid?.let { deletedNoteUids.add(it) }
                    }
                    editor.putStringSet("noteUids",deletedNoteUids)
                    editor.apply()

                    Toast.makeText(this@AddEditNoteActivity,"Note Deleted",Toast.LENGTH_SHORT).show()

                }


            }


        }
    }




    private fun saveOtherEntities(){

        noteUid?.let {

            val newTagsAdded = viewModal.newTags
            val deletedTags = viewModal.deletedTags
            Log.e(TAG, "saveOtherEntities: $newTagsAdded" )
            viewModal.addOrDeleteTags(newTagsAdded,deletedTags,it)
        }

        val labelColor = viewModal.labelColor
        if (viewModal.labelChanged){
            if (labelColor > 0){
                noteUid?.let { viewModal.addOrDeleteLabel(labelColor, it,true) }
            }else{
                noteUid?.let { viewModal.addOrDeleteLabel(labelColor, it,false) }

            }
        }



        if (viewModal.reminderTime > 0){
            if (reminderNoteSet){
                if (viewModal.deletedNote.value != true){
                    startAlarm(viewModal.reminderTime.toInt())
                }

            }else{
                cancelAlarm(viewModal.reminderTime.toInt())
            }
        }

    }


    private fun goToMain() {
        
        saveNote()
        val intent = Intent(this@AddEditNoteActivity, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP;
        startActivity(intent)
        finish()
    }




    override fun onBackPressed() {

        super.onBackPressed()
        goToMain()

    }

    override fun deleteTag(tag: String) {
        if (noteType == "Edit"){
            viewModal.noteChanged.value = true
        }
        viewModal.newTags.remove(tag)
        viewModal.deletedTags.add(tag)
        val tags = ArrayList<String>()
        tags.addAll(viewModal.newTags)
        tags.addAll(viewModal.oldTagList)
        tags.remove(tag)
        tagListAdapter.updateList(tags)
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

    override fun getTag(tag: String) {
        viewModal.newTags.add(tag)
        tagListAdapter.updateList(viewModal.oldTagList)
        viewModal.noteChanged.value = true

    }

    override fun onLabelItemClick(labelColor: Int) {
        viewModal.labelColor = labelColor
        viewModal.labelSet.value = true
        viewModal.noteChanged.value = true
        coordinatorlayout.setBackgroundColor(labelColor)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.statusBarColor = labelColor
    }




}