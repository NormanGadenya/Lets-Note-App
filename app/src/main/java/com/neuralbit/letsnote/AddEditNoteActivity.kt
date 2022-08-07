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
import android.os.Bundle
import android.speech.RecognizerIntent
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.util.Log
import android.util.SparseArray
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.HORIZONTAL
import com.flask.colorpicker.ColorPickerView
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.text.TextBlock
import com.google.android.gms.vision.text.TextRecognizer
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.neuralbit.letsnote.Services.DeleteReceiver
import com.neuralbit.letsnote.adapters.*
import com.neuralbit.letsnote.entities.NoteFireIns
import com.neuralbit.letsnote.entities.TodoItem
import com.neuralbit.letsnote.ui.label.LabelViewModel
import com.neuralbit.letsnote.utilities.*
import kotlinx.coroutines.launch
import java.util.*


class AddEditNoteActivity : AppCompatActivity() ,
    TagRVInterface,
    GetTimeFromPicker,
    GetDateFromPicker,
    GetTagFromDialog,
    LabelClickInterface,
    TodoItemInterface,
    OnStartDragListener,
    OnTodoListChangedListener
{
    private var mItemTouchHelper: ItemTouchHelper? = null
    private var deleted: Boolean = false
    private var reminderItem : MenuItem? = null
    private var lockNoteItem : MenuItem? = null
    private var restoreItem : MenuItem? = null
    private var pinItem: MenuItem? = null
    private var archiveItem: MenuItem? = null
    private var deleteItem: MenuItem? = null
    private lateinit var dismissTodoButton: ImageButton
    private lateinit var ocrButton: ImageButton
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
    private var oldLabel : Int = -1
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
    private var todoItemChecked: Boolean = false
    private lateinit var todoRVAdapter : TodoRVAdapter
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
    private val deletedTodos = ArrayList<TodoItem>()
    private lateinit var pref : SharedPreferences


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


        viewModal.allFireLabels().observe(lifecycleOwner){
            val labelColors = HashSet<Int>()
            for (l in it){
                labelColors.add(l.labelColor)
            }
            labelListAdapter.updateLabelIDList(labelColors)
        }

        viewModal.allTodoItems.observe(this){
            if (it.isNotEmpty()){
                todoRV.visibility = VISIBLE
            }
            viewModal.todoItems = LinkedList(it)
            todoRVAdapter.updateTodoItems(it)
            todoRVAdapter.notifyItemRangeChanged(0,it.size)
        }
        todoItemDescTV.setOnKeyListener { _, key, _ ->
            if (key == KeyEvent.KEYCODE_ENTER){

                val todoItemText = todoItemDescTV.text.toString()
                if(todoItemText.isNotEmpty()){
                    val todoItem = TodoItem(todoItemText,todoItemChecked)
                    viewModal.todoItems.add(todoItem)
                    todoCheckBox.isChecked = false
                    viewModal.noteChanged.value = true
                    todoRVAdapter.updateTodoItems(viewModal.todoItems)
                    todoRVAdapter.notifyDataSetChanged()

                    todoRV.scrollToPosition(viewModal.todoItems.size - 1)

                    todoItemDescTV.text.clear()
                }
                return@setOnKeyListener true
            }


            false
        }
        todoCheckBox.setOnCheckedChangeListener { _, b ->
            todoItemChecked = b
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
                    delLabelBtn.visibility = VISIBLE
                }else{
                    coordinatorlayout.setBackgroundColor(Color.TRANSPARENT)

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
                reminderItem?.setIcon(R.drawable.ic_baseline_add_alert_24)


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
                reminderItem?.setIcon(R.drawable.ic_outline_add_alert_24)
                reminderTV.visibility =  GONE
                reminderIcon.visibility = GONE
            }
        }

        viewModal.pinned.observe(lifecycleOwner){
            notePinned = it

            if (!it){
                pinItem?.setIcon(R.drawable.ic_outline_push_pin_24)
            }else{
                pinItem?.setIcon(R.drawable.ic_baseline_push_pin_24)
            }
        }

        viewModal.deletedNote.observe(lifecycleOwner){
            val editor: SharedPreferences.Editor = pref.edit()
            val noteUids = pref.getStringSet("noteUids",HashSet())
            val deletedNoteUids = HashSet<String>()
            if (it) {

                pinItem?.isVisible = false
                archiveItem?.isVisible = false
                restoreItem?.isVisible = true
                reminderItem?.isVisible = false
                noteDescriptionEdit.isEnabled = false
                noteTitleEdit.isEnabled = false
                infoContainer.visibility = GONE
                reminderTV.visibility =  GONE
                reminderIcon.visibility = GONE
                if (noteUids != null){
                    deletedNoteUids.addAll(noteUids)
                    noteUid?.let { deletedNoteUids.add(it) }
                }
                cancelAlarm(viewModal.reminderTime.toInt())
                editor.putStringSet("noteUids",deletedNoteUids)
                editor.apply()



            } else {
                pinItem?.isVisible = true
                archiveItem?.isVisible = true
                restoreItem?.isVisible = false
                reminderItem?.isVisible = true
                infoContainer.visibility = VISIBLE
                noteDescriptionEdit.isEnabled = true
                noteTitleEdit.isEnabled = true
                if (viewModal.reminderSet.value == true){
                    reminderTV.visibility =  VISIBLE
                    reminderIcon.visibility = VISIBLE
                }
                if (noteUids != null){
                    deletedNoteUids.addAll(noteUids)
                    noteUid?.let { uid -> deletedNoteUids.remove(uid) }
                }
                editor.putStringSet("noteUids",deletedNoteUids)
                editor.apply()

            }
        }
        viewModal.archived.observe(lifecycleOwner){
            archived = it

            if (viewModal.deletedNote.value!= true){
                if (it) {
                    pinItem?.isVisible = false
                    archiveItem?.isVisible = false
                    restoreItem?.isVisible = true
                    reminderItem?.isVisible = false
                    noteDescriptionEdit.isEnabled = false
                    noteTitleEdit.isEnabled = false
                    infoContainer.visibility = GONE
                } else {
                    pinItem?.isVisible = true
                    archiveItem?.isVisible = true
                    restoreItem?.isVisible = false
                    reminderItem?.isVisible = true

                    infoContainer.visibility = VISIBLE
                    noteDescriptionEdit.isEnabled = true
                    noteTitleEdit.isEnabled = true

                }
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

        val callback: ItemTouchHelper.Callback = SimpleItemTouchHelperCallback(todoRVAdapter)
        mItemTouchHelper = ItemTouchHelper(callback)
        mItemTouchHelper?.attachToRecyclerView(todoRV)
        todoRV.isNestedScrollingEnabled = false
//
//        val touchHelper = ItemTouchHelper(object  : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN,0){
//            override fun onMove(
//                recyclerView: RecyclerView,
//                viewHolder: RecyclerView.ViewHolder,
//                target: RecyclerView.ViewHolder
//            ): Boolean {
//                Log.d(TAG, "onSwiped:old pos ${viewHolder.oldPosition} new pos ${viewHolder.adapterPosition}")
//
//                return true
//            }
//
//            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
//
//                when (direction) {
//                    ItemTouchHelper.UP -> {
//
//                    }
//
//
//                    ItemTouchHelper.DOWN -> {
//
//                    }
//                }
//
//
//            }
//
//
//        })
//        touchHelper.attachToRecyclerView(todoRV)












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
        val helper = TextViewUndoRedo(noteDescriptionEdit)
        undoButton.setOnClickListener {
            helper.undo()
//            redoButton.isEnabled = true
        }
        viewModal.undoMode.value = false

        redoButton.setOnClickListener {
            helper.redo()
//            undoButton.isEnabled = true
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

            override fun onTextChanged(p0: CharSequence?, start: Int, before: Int, count: Int) {

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
                val todoDesc = todoItemDescTV.text.toString()
                val isItemChecked = todoCheckBox.isChecked
                val todoItem = TodoItem(todoDesc,isItemChecked)
                todoRV.visibility = VISIBLE
                viewModal.todoItems.add(todoItem)
                viewModal.noteChanged.value = true
                todoItemDescTV.text.clear()
                todoRVAdapter.updateTodoItems(ArrayList(viewModal.todoItems))

            }


        }
    }

    private fun deleteNote(){
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
        pref = applicationContext.getSharedPreferences("DeletedNotes", MODE_PRIVATE)
        noteTitleEdit = findViewById(R.id.noteEditTitle)
        layoutManager = LinearLayoutManager(applicationContext,LinearLayoutManager.HORIZONTAL,false)
        calendar = Calendar.getInstance()
        noteDescriptionEdit = findViewById(R.id.noteEditDesc)
        tvTimeStamp = findViewById(R.id.tvTimeStamp)
        tagListRV = findViewById(R.id.tagListRV)
        labelBtn = findViewById(R.id.labelBtn)
        coordinatorlayout = findViewById(R.id.coordinatorlayout)
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
        deleted = intent.getBooleanExtra("deleted",false)
        val protected = intent.getBooleanExtra("protected", false)
        val tagIntentList = intent.getStringArrayListExtra("tagList")
        oldLabel = intent.getIntExtra("labelColor",0)
        val reminderTime = intent.getLongExtra("reminder",0)
        val todoItemStr = intent.getStringExtra("todoItems")
        var todoDoItemList: ArrayList<TodoItem> = ArrayList()
        if (todoItemStr != null){
            todoDoItemList = Gson().fromJson(todoItemStr, object : TypeToken<List<TodoItem?>?>() {}.type)
        }
        supportActionBar?.title = ""

        noteTimeStamp = intent.getLongExtra("timeStamp",-1)
        ocrButton = findViewById(R.id.ocrButton)
        undoButton = findViewById(R.id.undoButton)
        redoButton = findViewById(R.id.redoButton)
        infoContainer = findViewById(R.id.infoContainer)
        alertBottomSheet =  BottomSheetDialog(this)
        labelBottomSheet = BottomSheetDialog(this)
        coordinatorlayout = findViewById(R.id.coordinatorlayout)
        layoutManager.orientation = HORIZONTAL
        tagListAdapter= AddEditTagRVAdapter(applicationContext,this)
        labelListAdapter= AddEditLabelAdapter(applicationContext,this)
        todoRVAdapter = TodoRVAdapter(applicationContext,this,this,this)
        labelBottomSheet.setContentView(R.layout.note_label_bottom_sheet)
        delLabelBtn = labelBottomSheet.findViewById(R.id.delLabel)!!
        tagListRV.layoutManager= layoutManager
        tagListRV.adapter = tagListAdapter
        addTodoButton = findViewById(R.id.addTodo)
        dismissTodoButton = findViewById(R.id.dismissTodoBtn)
        todoRV = findViewById(R.id.todoRV)
        val layoutManagerTodo = LinearLayoutManager(applicationContext,LinearLayoutManager.VERTICAL,false)
        todoRV.layoutManager = layoutManagerTodo
        todoRV.adapter = todoRVAdapter
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
        viewModal.allTodoItems.value = todoDoItemList

        viewModal.labelColor = oldLabel
        viewModal.archived.value = archived
        viewModal.deletedNote.value = deleted
        viewModal.noteLocked.value = protected
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.add_edit_menu, menu)
        restoreItem  = menu?.findItem(R.id.restoreButton)
        pinItem = menu?.findItem(R.id.pinButton)
        archiveItem = menu?.findItem(R.id.archiveButton)
        deleteItem = menu?.findItem(R.id.deleteButton)
        reminderItem = menu?.findItem(R.id.reminderButton)
        val shareItem = menu?.findItem(R.id.shareButton)
        lockNoteItem = menu?.findItem(R.id.lockButton)
        restoreItem?.isVisible = false
        if (archived || deleted){
            restoreItem?.isVisible = true
            reminderItem?.isVisible = false
            archiveItem?.isVisible = false
            pinItem?.isVisible = false
            shareItem?.isVisible = false
        }

        if (deleted){
            archiveItem?.isVisible = false
        }

        shareItem?.setOnMenuItemClickListener {
            shareNote()
            return@setOnMenuItemClickListener true
        }


        archiveItem?.setOnMenuItemClickListener {
            archiveNote()
            return@setOnMenuItemClickListener true
        }

        pinItem?.setOnMenuItemClickListener {
            pinOrUnPinNote()
            return@setOnMenuItemClickListener true
        }

        restoreItem?.setOnMenuItemClickListener {
            unArchiveNote()
            unDelete()
            return@setOnMenuItemClickListener true
        }

        deleteItem?.setOnMenuItemClickListener {
            deleteNote()
            return@setOnMenuItemClickListener true
        }
        reminderItem?.setOnMenuItemClickListener {
            val reminderTime = viewModal.reminderTime
            if (reminderTime == (0).toLong()){
                showAlertSheetDialog()

            }else{
                cancelAlarm(reminderTime.toInt())
                viewModal.reminderTime = 0
            }
            return@setOnMenuItemClickListener true
        }

        lockNoteItem?.setOnMenuItemClickListener {
            viewModal.lockChanged = !viewModal.lockChanged
            return@setOnMenuItemClickListener true
        }

        return super.onCreateOptionsMenu(menu)
    }

    private fun shareNote() {
        noteTitle = noteTitleEdit.text.toString()
        noteDesc = noteDescriptionEdit.text.toString()
        if (noteTitle?.isNotBlank() == true || noteDesc?.isNotBlank() == true){

            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND

                val textToShare : String ? = if (noteTitle!!.isNotBlank()){
                    noteTitle + "\n" + noteDesc
                }else{
                    noteDesc
                }

                putExtra(Intent.EXTRA_TEXT, textToShare)
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, null)
            startActivity(shareIntent)
        }

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

            }


        }

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

    private fun scheduleDelete( noteUid : String, tags : ArrayList<String>, label: Int , timeStamp : Long) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, DeleteReceiver::class.java)

        intent.putExtra("noteUid",noteUid)
        intent.putExtra("timeStamp",System.currentTimeMillis())
        intent.putExtra("labelColor", label)
        intent.putStringArrayListExtra("tagList", tags)
        val pendingIntent = PendingIntent.getBroadcast(this, timeStamp.toInt(), intent, PendingIntent.FLAG_IMMUTABLE)
        val timeToDelete = timeStamp + 6.048e+8 // 7 days from the time it is softly deleted
        alarmManager.set(AlarmManager.RTC_WAKEUP, timeToDelete.toLong(), pendingIntent)
    }

    private fun cancelDelete(timestamp : Int){
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, DeleteReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, timestamp, intent, PendingIntent.FLAG_IMMUTABLE)
        alarmManager.cancel(pendingIntent)
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
        if(viewModal.noteChanged.value == true){
            if (noteTitle.isNotEmpty() || noteDescription.isNotEmpty() || viewModal.todoItems.isNotEmpty()){

                val tags = ArrayList<String>()
                tags.addAll(viewModal.oldTagList)
                tags.addAll(viewModal.newTags)
                tags.removeAll(viewModal.deletedTags.toSet())
                if (viewModal.deletedNote.value != true){
                    if(noteType == "Edit"){
                        cancelDelete(viewModal.reminderTime.toInt())
                        val noteUpdate = HashMap<String,Any>()
                        noteUpdate["title"] = noteTitle
                        noteUpdate["description"] = noteDescription
                        noteUpdate["timeStamp"] = currentDate
                        noteUpdate["label"] = viewModal.labelColor
                        noteUpdate["pinned"] = notePinned
                        noteUpdate["archived"] = archived
                        noteUpdate["reminderDate"] = viewModal.reminderTime
                        noteUpdate["todoItems"] = viewModal.todoItems
                        noteUpdate["tags"] = tags
                        noteUid?.let { viewModal.updateFireNote(noteUpdate, it) }

                        if (!archived){
                            saveOtherEntities()
                            Toast.makeText(this,"Note updated .. " , Toast.LENGTH_SHORT).show()
                        }else{
                            cancelAlarm(viewModal.reminderTime.toInt())
                        }

                    }else{
                        lifecycleScope.launch {
                            val noteFire = NoteFireIns(noteTitle, noteDescription, currentDate)
                            noteFire.tags = ArrayList(tags)
                            noteFire.reminderDate = viewModal.reminderTime
                            noteFire.pinned = notePinned
                            noteFire.label = viewModal.labelColor
                            noteFire.todoItems = ArrayList(viewModal.todoItems)
                            noteUid =  viewModal.addFireNote(noteFire)
                            saveOtherEntities()

                        }
                        Toast.makeText(this,"Note added .. " , Toast.LENGTH_SHORT).show()

                    }
                }else{
                    noteUid?.let { scheduleDelete(it, tags,viewModal.labelColor,noteTimeStamp) }
                    Toast.makeText(this@AddEditNoteActivity,"Note Deleted",Toast.LENGTH_SHORT).show()

                }


            }


        }
    }




    private fun saveOtherEntities(){
        noteUid?.let {

            val newTagsAdded = viewModal.newTags
            val deletedTags = viewModal.deletedTags
            viewModal.addOrDeleteTags(newTagsAdded,deletedTags,it)
        }

        val labelColor = viewModal.labelColor
        if (viewModal.labelChanged){
            if (labelColor > 0){
                noteUid?.let { viewModal.addOrDeleteLabel(labelColor,oldLabel, it,true) }
            }else{
                noteUid?.let { viewModal.addOrDeleteLabel(labelColor, oldLabel, it, false) }

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
        timeTitleTV.text= "Time set:" + DateFormat.getTimeFormat(this).format(calendar.time)
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
        viewModal.labelChanged = true
        viewModal.noteChanged.value = true
        textChanged = true
        coordinatorlayout.setBackgroundColor(labelColor)

    }


    override fun onItemDelete(position: Int, todoItem: TodoItem) {
        viewModal.noteChanged.value = true
        viewModal.todoItems.remove(todoItem)
        todoRVAdapter.notifyItemRemoved(position)
        deletedTodos.add(todoItem)
        todoRVAdapter.updateTodoItems(viewModal.todoItems)
    }

    override fun onItemCheckChanged(position: Int, todoItem: TodoItem) {
        if (!deletedTodos.contains(todoItem)){
            viewModal.noteChanged.value = true
            val todoItems = viewModal.todoItems
            todoItems[position] = todoItem
            viewModal.todoItems = todoItems
//            todoRVAdapter.notifyItemChanged(position)

        }
    }

    override fun onItemDescChanged(position: Int, todoItem: TodoItem) {
        viewModal.noteChanged.value = true
        val todoItems = viewModal.todoItems
        todoItems[position] = todoItem
        viewModal.todoItems = todoItems
        todoRVAdapter.updateTodoItems(todoItems)
//        todoRVAdapter.notifyItemChanged(position)

        viewModal.updatedTodos.add(todoItem)
    }

    override fun onEnterKeyPressed(position: Int, todoItem: TodoItem) {
        viewModal.noteChanged.value = true
        val todoItems = viewModal.todoItems
        todoItems[position] = todoItem
        viewModal.todoItems = todoItems
        todoRVAdapter.notifyItemChanged(position)
        viewModal.updatedTodos.add(todoItem)


    }


    override fun onStartDrag(viewHolder: RecyclerView.ViewHolder?) {
        if (viewHolder != null) {
            mItemTouchHelper?.startDrag(viewHolder)
        }
    }

    override fun onTodoListChanged(todoItems: MutableList<TodoItem>?) {
        Log.d(TAG, "onTodoListChanged: $todoItems")
        viewModal.todoItems = todoItems?.let { LinkedList(it) }!!
    }


}