package com.neuralbit.letsnote

import android.Manifest
import android.annotation.TargetApi
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.pm.ShortcutManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.hardware.fingerprint.FingerprintManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.util.Log
import android.util.SparseArray
import android.util.TypedValue
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.HORIZONTAL
import com.canhub.cropper.CropImage
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
import com.neuralbit.letsnote.entities.LabelFire
import com.neuralbit.letsnote.entities.NoteFireIns
import com.neuralbit.letsnote.entities.TodoItem
import com.neuralbit.letsnote.ui.label.LabelViewModel
import com.neuralbit.letsnote.utilities.*
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.*


class AddEditNoteActivity : AppCompatActivity() ,
    TagRVInterface,
    GetTimeFromPicker,
    GetDateFromPicker,
    GetTagFromDialog,
    LabelClickInterface,
    TodoItemInterface

{
    private var fontMultiplier: Int = 2
    private var deleted: Boolean = false
    private var reminderItem : MenuItem? = null
    private var lockNoteItem : MenuItem? = null
    private var restoreItem : MenuItem? = null
    private var pinItem: MenuItem? = null
    private var archiveItem: MenuItem? = null
    private val GALLERY_REQUEST = 100
    private var deleteItem: MenuItem? = null
    private lateinit var dismissTodoButton: ImageButton
    private lateinit var ocrButton: ImageButton
    private lateinit var addTodoButton: ImageButton
    private lateinit var undoButton: ImageButton
    private lateinit var noteDescScrollView : NestedScrollView
    private lateinit var redoButton: ImageButton
    private lateinit var noteTitleEdit : EditText
    private lateinit var noteDescriptionEdit : MultiAutoCompleteTextView
    private lateinit var addTagBtn : ImageButton
    private lateinit var delLabelBtn : ImageButton
    private lateinit var reminderIcon : ImageView
    private lateinit var reminderTV : TextView
    private var noteUid : String? = null
    private val viewModal : NoteViewModel by viewModels()
    private lateinit var labelViewModel : LabelViewModel
    private var oldLabel : Int = -1
    private var noteDescOrig : String? = null
    private var noteDescOrigList = ArrayList<String>()
    private lateinit var noteType : String
    private lateinit var tvTimeStamp : TextView
    private lateinit var redoUndoGroup: View
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
    private var TAG = "AddEditNoteActivity"
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
    private lateinit var colorPickerView: ColorPickerView
    private lateinit var labelTitleET: EditText
    private val deletedTodos = ArrayList<TodoItem>()
    private lateinit var deletedNotePrefs : SharedPreferences
    private lateinit var settingsPref : SharedPreferences
    private var protected = false
    private var labelColor = 0
    private var labelTitle :String?=null
    private var noteChanged = false
    private var tagList : ArrayList<String> = ArrayList()

    private val cropActivityResultContract = object : ActivityResultContract<Any?,Uri?>(){
        override fun createIntent(context: Context, input: Any?): Intent {

            return CropImage.activity()
                .getIntent(this@AddEditNoteActivity)
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
            return CropImage.getActivityResult(intent)?.uriContent
        }
    }
    private lateinit var cropActivityResultLauncher: ActivityResultLauncher<Any?>

    override fun onCreate(savedInstanceState: Bundle?)  {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
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
            viewModal.labelFireList = ArrayList(it)
            labelListAdapter.updateLabelIDList(it)
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
                    todoRVAdapter.notifyItemInserted(todoRVAdapter.itemCount)

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
            addTagDialog.tagList = tagList
            addTagDialog.show(supportFragmentManager,"addTagDialogs")

        }
        when (noteType) {
            "Edit" -> {
                noteTitleEdit.setText(noteTitle)

                noteDescriptionEdit.setText(noteDesc)
                tvTimeStamp.visibility =VISIBLE
                redoUndoGroup.visibility = GONE

                tvTimeStamp.text= getString(R.string.timeStamp,cm.convertLongToTime(noteTimeStamp)[0],cm.convertLongToTime(noteTimeStamp)[1])

                tagListAdapter.updateList(viewModal.oldTagList)
            }
            else -> {
                redoUndoGroup.visibility = VISIBLE

                tvTimeStamp.visibility =GONE
            }
        }

        delLabelBtn.setOnClickListener {
            viewModal.noteChanged.value = true
            viewModal.labelChanged = true
            viewModal.labelColor.value = 0


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

        val fingerprintManager : FingerprintManager = getSystemService(FINGERPRINT_SERVICE) as FingerprintManager
        lockNoteItem?.isVisible = !(!fingerprintManager.isHardwareDetected || !fingerprintManager.hasEnrolledFingerprints())

        viewModal.deletedNote.observe(lifecycleOwner){
            val emptyTrashImmediately = settingsPref.getBoolean("EmptyTrashImmediately",false)

            val editor: SharedPreferences.Editor = deletedNotePrefs.edit()
            val noteUids = deletedNotePrefs.getStringSet("noteUids",HashSet())
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
                addTodoButton.visibility = GONE
                todoRV.isEnabled = false
                if (!emptyTrashImmediately){
                    if (noteUids != null){
                        deletedNoteUids.addAll(noteUids)
                        noteUid?.let { deletedNoteUids.add(it) }
                    }
                    editor.putStringSet("noteUids",deletedNoteUids)
                    editor.putLong(noteUid,calendar.timeInMillis)
                    editor.apply()
                }
                cancelAlarm(viewModal.reminderTime.toInt())



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
                addTodoButton.visibility = VISIBLE
                todoRV.isEnabled = true
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
                    todoCheckBox.isEnabled = false
                    infoContainer.visibility = GONE
                    addTodoButton.visibility = GONE

                    todoRV.isEnabled = false
                } else {
                    pinItem?.isVisible = true
                    archiveItem?.isVisible = true
                    restoreItem?.isVisible = false
                    reminderItem?.isVisible = true
                    todoCheckBox.isEnabled = true
                    infoContainer.visibility = VISIBLE
                    noteDescriptionEdit.isEnabled = true
                    noteTitleEdit.isEnabled = true
                    addTodoButton.visibility = VISIBLE
                    todoRV.isEnabled = true

                }
            }
        }


        
        viewModal.allFireTags().observe(this){

            tagList.clear()
            for (tag in it){
                tagList.add(tag.tagName)
            }

            val adapter = ArrayAdapter(applicationContext,android.R.layout.simple_dropdown_item_1line,tagList)
            noteDescriptionEdit.setAdapter(adapter)
            noteDescriptionEdit.setTokenizer(SpaceTokenizer())
            
        }

        todoRV.isNestedScrollingEnabled = false

        val touchHelper = ItemTouchHelper(object  : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.START or ItemTouchHelper.END,ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT){
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val fromPosition = viewHolder.adapterPosition
                val toPosition = target.adapterPosition
                if (fromPosition < toPosition) {
                    for (i in fromPosition until toPosition) {
                        Collections.swap(viewModal.todoItems, i, i + 1)
                    }
                } else {
                    for (i in fromPosition downTo toPosition + 1) {
                        Collections.swap(viewModal.todoItems, i, i - 1)
                    }
                }
                todoRVAdapter.notifyItemMoved(viewHolder.adapterPosition,target.adapterPosition)
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                viewModal.noteChanged.value = true
                val todoItem = viewModal.todoItems[viewHolder.adapterPosition]
                deletedTodos.add(todoItem)
                viewModal.todoItems.removeAt(viewHolder.adapterPosition)
                todoRVAdapter.updateTodoItems(viewModal.todoItems)
                todoRVAdapter.notifyItemRemoved(viewHolder.adapterPosition)
            }

            override fun isLongPressDragEnabled(): Boolean {
                return true
            }

            override fun isItemViewSwipeEnabled(): Boolean {
                return true
            }
        })
        touchHelper.attachToRecyclerView(todoRV)



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
        }
        viewModal.undoMode.value = false

        redoButton.setOnClickListener {
            helper.redo()
        }
        noteTitleEdit.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                tvTimeStamp.visibility =GONE
                redoUndoGroup.visibility = VISIBLE
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
                viewModal.noteChanged.value = true

                tvTimeStamp.visibility =GONE
                redoUndoGroup.visibility = VISIBLE
                if(p0?.length!! > 0){

                    noteListBullet()

                    getTagFromString(p0)

                }

            }

            override fun afterTextChanged(p0: Editable?) {

            }
        })


        noteDescriptionEdit.setOnKeyListener { _, key, _ ->

            viewModal.backPressed.value = key == KeyEvent.KEYCODE_DEL

            false
        }

        viewModal.noteLocked.observe(lifecycleOwner){

            protected = it
            if (it){
                lockNoteItem?.setIcon(R.drawable.baseline_lock_24)
            }else{
                lockNoteItem?.setIcon(R.drawable.baseline_lock_open_24)
            }
        }

        viewModal.pinned.observe(lifecycleOwner) {
            notePinned = it
            Log.d(TAG, "onCreate: $pinItem")
            if (it) {
                pinItem?.setIcon(R.drawable.ic_baseline_push_pin_24)

            } else {
                pinItem?.setIcon(R.drawable.ic_outline_push_pin_24)
            }
        }

        viewModal.labelColor.observe(this){
            labelColor = it
            if (it>0){
                coordinatorlayout.setBackgroundColor(it)
                delLabelBtn.visibility = VISIBLE

            }else{
                coordinatorlayout.setBackgroundColor(Color.TRANSPARENT)
                delLabelBtn.visibility = GONE

            }
        }




        noteTitleEdit.setOnKeyListener { _, _, _ ->
            viewModal.noteChanged.value = true
            tvTimeStamp.visibility = GONE
            redoUndoGroup.visibility = VISIBLE

            false
        }

        viewModal.noteChanged.observe(this) {
            textChanged = it
            if(it){
                tvTimeStamp.visibility = GONE
                redoUndoGroup.visibility = VISIBLE
            }else{
                tvTimeStamp.visibility = VISIBLE
                redoUndoGroup.visibility = GONE
            }
        }


        ocrButton.setOnClickListener {

            if(isNetworkConnected()){
                if (ContextCompat.checkSelfPermission(
                        this@AddEditNoteActivity,
                        Manifest.permission.CAMERA
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestCameraPermission()
                    requestStoragePermission()

                } else {
                    cropActivityResultLauncher.launch(null)
                }
            }else{
                Snackbar.make(coordinatorlayout, "No internet connection", Snackbar.LENGTH_SHORT).show()
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
                todoCheckBox.isChecked = false
                todoRVAdapter.updateTodoItems(ArrayList(viewModal.todoItems))
                todoRVAdapter.notifyDataSetChanged()

            }


        }
    }


    private fun requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        ) {
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Permission needed")
                .setMessage("This permission is needed because we need to access your storage")
                .setPositiveButton(
                    "ok"
                ) { _: DialogInterface?, _: Int ->
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        GALLERY_REQUEST
                    )
                }
                .setNegativeButton(
                    "cancel"
                ) { dialog: DialogInterface, which: Int -> dialog.dismiss() }
                .create().show()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                GALLERY_REQUEST
            )
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
                val frame = bitmap?.let { Frame.Builder().setBitmap(it).build() }
                val sparseArray = frame?.let { recognizer.detect(it) } as SparseArray<TextBlock>
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

    private fun isNetworkConnected(): Boolean {
        val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo != null && cm.activeNetworkInfo!!.isConnected
    }

    private fun initControllers(){
        cm= Common()
        if (Build.VERSION.SDK_INT >= 25) {
            createShortcut();
        }else{
            removeShortcuts();
        }

        deletedNotePrefs = applicationContext.getSharedPreferences("DeletedNotes", MODE_PRIVATE)
        settingsPref = applicationContext.getSharedPreferences("Settings", MODE_PRIVATE)
        fontMultiplier = settingsPref.getInt("fontMultiplier",2)

        noteTitleEdit = findViewById(R.id.noteEditTitle)
        layoutManager = LinearLayoutManager(applicationContext,LinearLayoutManager.HORIZONTAL,false)
        calendar = Calendar.getInstance()
        noteDescriptionEdit = findViewById(R.id.noteEditDesc)
        tvTimeStamp = findViewById(R.id.tvTimeStamp)
        redoUndoGroup = findViewById(R.id.redoUndoGroup)
        tagListRV = findViewById(R.id.tagListRV)
        labelBtn = findViewById(R.id.labelBtn)
        coordinatorlayout = findViewById(R.id.coordinatorlayout)
        addTagBtn = findViewById(R.id.addTagBtn)
        reminderTV = findViewById(R.id.reminderTV)
        reminderIcon = findViewById(R.id.reminderIcon)
        noteTitleEdit.setTextSize(TypedValue.COMPLEX_UNIT_SP,32f+ ((fontMultiplier-2)*2).toFloat())
        noteDescriptionEdit.setTextSize(TypedValue.COMPLEX_UNIT_SP,18f+ ((fontMultiplier-2)*2).toFloat())
        tvTimeStamp.setTextSize(TypedValue.COMPLEX_UNIT_SP,12f+ ((fontMultiplier-2)*2).toFloat())
        noteType = intent.getStringExtra("noteType").toString()
        intent.putExtra("noteType","Edit")
        noteTitle = intent.getStringExtra("noteTitle")
        noteUid = intent.getStringExtra("noteUid")
        noteDesc = intent.getStringExtra("noteDescription")

        notePinned = intent.getBooleanExtra("pinned",false)
        archived = intent.getBooleanExtra("archieved",false)
        deleted = intent.getBooleanExtra("deleted",false)
        if (viewModal.noteChanged.value != true){
            viewModal.archived.value = archived
            viewModal.deletedNote.value = deleted
            viewModal.noteChanged.value = noteChanged
            viewModal.pinned.value = notePinned
            viewModal.noteLocked.value = intent.getBooleanExtra("protected", false)
        }


        val tagIntentList = intent.getStringArrayListExtra("tagList")
        oldLabel = intent.getIntExtra("labelColor",0)
        val reminderTime = intent.getLongExtra("reminder",0)
        val todoItemStr = intent.getStringExtra("todoItems")
        var todoDoItemList: ArrayList<TodoItem> = ArrayList()
        if (todoItemStr != null){
            todoDoItemList = Gson().fromJson(todoItemStr, object : TypeToken<List<TodoItem?>?>() {}.type)
        }
        noteChanged = intent.getBooleanExtra("noteChanged",false)
        viewModal.noteChanged.value = noteChanged

        supportActionBar?.title = ""
        cropActivityResultLauncher = registerForActivityResult(cropActivityResultContract){
            it?.let { uri ->
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(contentResolver,uri)
                    recognizeText()
                }catch (e : IOException){
                    e.printStackTrace()
                }
            }
        }

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
        todoRVAdapter = TodoRVAdapter(applicationContext,this)
        labelBottomSheet.setContentView(R.layout.note_label_bottom_sheet)
        delLabelBtn = labelBottomSheet.findViewById(R.id.delLabel)!!
        tagListRV.layoutManager= layoutManager
        tagListRV.adapter = tagListAdapter
        addTodoButton = findViewById(R.id.addTodo)
        dismissTodoButton = findViewById(R.id.dismissTodoBtn)
        noteDescScrollView = findViewById(R.id.scrollView2)
        todoRV = findViewById(R.id.todoRV)
        val layoutManagerTodo = LinearLayoutManager(applicationContext,LinearLayoutManager.VERTICAL,false)
        todoRV.layoutManager = layoutManagerTodo
        todoRV.adapter = todoRVAdapter
        todoCheckBox = findViewById(R.id.todoCheckBox)
        todoItemDescTV = findViewById(R.id.todoItemDescTV)
        lifecycleOwner = this

        labelViewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[LabelViewModel::class.java]
        if (tagIntentList != null){
            viewModal.oldTagList = tagIntentList
        }
        viewModal.reminderTime = reminderTime
        if (reminderTime > (0).toLong()){
            viewModal.reminderSet.value = true
        }
        viewModal.allTodoItems.value = todoDoItemList
        if (oldLabel > 0){
            viewModal.labelColor.value = oldLabel
        }

        val fontStyle = settingsPref.getString("font",null)
        val typeface: Typeface? = when (fontStyle) {
            "Architects daughter" -> {
                ResourcesCompat.getFont(applicationContext, R.font.architects_daughter)
            }
            "Abreeze" -> {
                ResourcesCompat.getFont(applicationContext, R.font.abeezee)
            }
            "Adamina" -> {
                ResourcesCompat.getFont(applicationContext, R.font.adamina)
            }
            else -> {
                ResourcesCompat.getFont(applicationContext, R.font.roboto)
            }
        }
        todoItemDescTV.setTextSize(TypedValue.COMPLEX_UNIT_SP,18+ ((fontMultiplier-2)*2).toFloat())
        todoItemDescTV.typeface = typeface
        todoRVAdapter.fontStyle = fontStyle
        todoRVAdapter.fontMultiplier = fontMultiplier
        noteDescriptionEdit.typeface = typeface
        noteTitleEdit.typeface = typeface
        reminderTV.typeface = typeface
        tvTimeStamp.typeface = typeface
    }



    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.add_edit_menu, menu)
        restoreItem  = menu?.findItem(R.id.restoreButton)
        pinItem = menu?.findItem(R.id.pinButton)
        archiveItem = menu?.findItem(R.id.archiveButton)
        deleteItem = menu?.findItem(R.id.deleteButton)
        lockNoteItem = menu?.findItem(R.id.lockButton)
        reminderItem = menu?.findItem(R.id.reminderButton)
        val shareItem = menu?.findItem(R.id.shareButton)
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
            viewModal.noteChanged.value = true
            val reminderTime = viewModal.reminderTime
            if (reminderTime == (0).toLong()) {
                showAlertSheetDialog()

            } else {
                cancelAlarm(reminderTime.toInt())
                viewModal.reminderTime = 0
            }
            return@setOnMenuItemClickListener true
        }

        if (reminderNoteSet){
            reminderItem?.setIcon(R.drawable.ic_baseline_add_alert_24)
        }else{
            reminderItem?.setIcon(R.drawable.ic_outline_add_alert_24)
        }
        if (protected){
            lockNoteItem?.setIcon(R.drawable.baseline_lock_24)
        }else{
            lockNoteItem?.setIcon(R.drawable.baseline_lock_open_24)
        }

        if (notePinned){
            pinItem?.setIcon(R.drawable.ic_baseline_push_pin_24)
        }else{
            pinItem?.setIcon(R.drawable.ic_outline_push_pin_24)
        }

        lockNoteItem?.setOnMenuItemClickListener {
            viewModal.noteChanged.value = true
            viewModal.noteLocked.value = !protected
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

    @TargetApi(25)
    private fun createShortcut() {
        val intent = Intent(applicationContext, AddEditNoteActivity::class.java)
        intent.action = Intent.ACTION_VIEW
        val shortcutInfo = ShortcutInfoCompat.Builder(applicationContext, "newNote")
            .setShortLabel(getString(R.string.shortcut_short_label))
            .setLongLabel(getString(R.string.shortcut_long_label))
            .setIcon(IconCompat.createWithResource(applicationContext,
                R.drawable.ic_baseline_mode_edit_24
            ))
            .setIntent(intent) // Push the shortcut
            .build()

        ShortcutManagerCompat.pushDynamicShortcut(applicationContext, shortcutInfo)
    }


    @TargetApi(25)
    private fun removeShortcuts() {
        val shortcutManager = getSystemService(ShortcutManager::class.java)
        shortcutManager.disableShortcuts(listOf("newNote"))
        shortcutManager.removeAllDynamicShortcuts()
    }


    private fun getTagFromString(p0: CharSequence?) {
        val strL = p0?.toString()?.split(" ")
        if (!backPressed) {
            if (strL != null && strL.size > 1) {
                var word = strL[strL.lastIndex - 1]
                if (word.contains("#")) {
                    if (!viewModal.oldTagList.contains(word)){
                        word = word.replace("\n", "");
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
                        viewModal.noteChanged.value = true
                        viewModal.labelChanged = true
                        if (viewModal.labelColor.value != null){

                            val label = viewModal.labelTitle.value?.let { it1 -> LabelFire(labelColor= viewModal.labelColor.value!!,labelTitle= it1) }
                            if (label != null) {
                                viewModal.labelFireList.add(label)
                                labelListAdapter.updateLabelIDList(viewModal.labelFireList)
                            }
                        }


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
            labelTitleET = labelDialog.findViewById(R.id.labelTitle)
            labelTitle = labelTitleET.text.toString()
            viewModal.labelTitle.value = labelTitle
            labelTitleET.addTextChangedListener(object : TextWatcher{
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun afterTextChanged(p0: Editable?) {
                    viewModal.labelTitle.value = p0.toString()
                }
            })

            colorPickerView.addOnColorSelectedListener{
                val hex = ColorTransparentUtils.transparentColor(it,30)
                viewModal.labelColor.value = Color.parseColor(hex)
                delLabelBtn.visibility = VISIBLE


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
        intent.putExtra("labelColor",labelColor)
        intent.putExtra("pinned",viewModal.pinned.value)
        intent.putExtra("archieved",viewModal.archived.value)
        intent.putExtra("protected",protected)
        val tags = ArrayList<String>()
        tags.addAll(viewModal.oldTagList)
        tags.addAll(viewModal.newTags)
        tags.removeAll(viewModal.deletedTags.toSet())
        intent.putStringArrayListExtra("tagList", tags)
        intent.putExtra("noteType","Edit")

        val pendingIntent = PendingIntent.getBroadcast(this, requestCode, intent, PendingIntent.FLAG_IMMUTABLE)
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
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
        viewModal.labelColor.observe(this){
            labelColor = it
        }

        if(viewModal.noteChanged.value == true){

            if (noteTitle.isNotEmpty() || noteDescription.isNotEmpty() || viewModal.todoItems.isNotEmpty()){

                val tags = ArrayList<String>()
                tags.addAll(viewModal.oldTagList)
                tags.addAll(viewModal.newTags)
                tags.removeAll(viewModal.deletedTags.toSet())

                if (viewModal.deletedNote.value != true){
                    if(noteType == "Edit" && noteUid!= null){
                        cancelDelete(viewModal.reminderTime.toInt())
                        val noteUpdate = HashMap<String,Any>()
                        noteUpdate["title"] = noteTitle
                        noteUpdate["description"] = noteDescription
                        noteUpdate["timeStamp"] = currentDate
                        noteUpdate["label"] = labelColor
                        noteUpdate["pinned"] = viewModal.pinned.value == true
                        noteUpdate["archived"] = viewModal.archived.value == true
                        noteUpdate["reminderDate"] = viewModal.reminderTime
                        noteUpdate["todoItems"] = viewModal.todoItems
                        noteUpdate["protected"] = viewModal.noteLocked.value == true
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
                            noteFire.pinned = viewModal.pinned.value == true
                            noteFire.label = labelColor
                            noteFire.protected = viewModal.noteLocked.value == true
                            noteFire.todoItems = ArrayList(viewModal.todoItems)
                            noteUid =  viewModal.addFireNote(noteFire)
                            saveOtherEntities()

                        }
                        Toast.makeText(this,"Note added .. " , Toast.LENGTH_SHORT).show()

                    }
                }else{
                    val emptyTrashImmediately = settingsPref.getBoolean("EmptyTrashImmediately",false)
                    if (!emptyTrashImmediately){
                        noteUid?.let { scheduleDelete(it, tags,labelColor,noteTimeStamp) }
                    }else{
                        noteUid?.let { viewModal.deleteNote(it, labelColor,tags) }
                    }
                    Toast.makeText(this@AddEditNoteActivity,"Note Deleted",Toast.LENGTH_SHORT).show()

                }

            }

        }
    }

    override fun onResume() {
        if (protected && viewModal.appPaused){
            val intent = Intent( applicationContext, Fingerprint::class.java)
            val noteTitle = noteTitleEdit.text.toString()
            val noteDescription = noteDescriptionEdit.text.toString()
            intent.putExtra("noteType","Edit")
            intent.putExtra("noteTitle",noteTitle)
            intent.putExtra("noteDescription",noteDescription)
            intent.putExtra("noteUid",noteUid)
            intent.putExtra("timeStamp",noteTimeStamp)
            intent.putExtra("labelColor",labelColor)
            intent.putExtra("pinned",viewModal.pinned.value)
            intent.putExtra("archieved",viewModal.archived.value)
            intent.putExtra("protected",true)
            intent.putExtra("reminder",viewModal.reminderTime)
            val tags = ArrayList<String>()
            tags.addAll(viewModal.oldTagList)
            tags.addAll(viewModal.newTags)
            tags.removeAll(viewModal.deletedTags.toSet())
            intent.putExtra("tags",tags)
            val toDoItemString: String = Gson().toJson(viewModal.todoItems)
            intent.putExtra("todoItems", toDoItemString)
            startActivity(intent)
        }
        super.onResume()
    }

    override fun onPause() {
        viewModal.appPaused = true
        super.onPause()
    }


    private fun saveOtherEntities(){
        noteUid?.let {

            val newTagsAdded = viewModal.newTags
            val deletedTags = viewModal.deletedTags
            viewModal.addOrDeleteTags(newTagsAdded,deletedTags,it)
        }

        val labelColor = viewModal.labelColor.value
        val labelTitle = viewModal.labelTitle.value
        if (viewModal.labelChanged){
            if (labelColor != null) {
                if (labelColor > 0){
                    noteUid?.let { viewModal.addOrDeleteLabel(labelColor,labelTitle,oldLabel, it,true) }
                }else{
                    noteUid?.let { viewModal.addOrDeleteLabel(labelColor,labelTitle, oldLabel, it, false) }

                }
            }
        }




        if (viewModal.reminderTime > 0){
            if (viewModal.reminderSet.value == true){
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
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
    }




    override fun onBackPressed() {

        goToMain()
        super.onBackPressed()

    }

    override fun onSupportNavigateUp(): Boolean {
        goToMain()

        return super.onSupportNavigateUp()
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

        val tags = HashSet<String>()
        tags.addAll(viewModal.oldTagList)
        viewModal.newTags.add(tag)
        tags.addAll(viewModal.newTags)
        tagListAdapter.updateList(ArrayList(tags))
        viewModal.noteChanged.value = true

    }

    override fun onLabelItemClick(labelColor: Int) {
        viewModal.labelColor.value = labelColor
        viewModal.labelChanged = true
        viewModal.noteChanged.value = true
        textChanged = true

    }


    override fun onItemDelete(position: Int, todoItem: TodoItem) {
        viewModal.noteChanged.value = true
        viewModal.todoItems.remove(todoItem)
        todoRVAdapter.updateTodoItems(viewModal.todoItems)
        todoRVAdapter.notifyItemRemoved(position)
        deletedTodos.add(todoItem)
    }

    override fun onItemCheckChanged(position: Int, todoItem: TodoItem) {
        if (!deletedTodos.contains(todoItem)){
            viewModal.noteChanged.value = true
            val todoItems = viewModal.todoItems
            todoItems[position] = todoItem
            viewModal.todoItems = todoItems

        }
    }

    override fun onItemDescChanged(position: Int, todoItem: TodoItem) {
        viewModal.noteChanged.value = true
        val todoItems = viewModal.todoItems
        todoItems[position] = todoItem
        viewModal.todoItems = todoItems
        todoRVAdapter.updateTodoItems(todoItems)
        todoRVAdapter.notifyItemChanged(position)

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




}