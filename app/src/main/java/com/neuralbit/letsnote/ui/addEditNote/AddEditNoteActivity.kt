package com.neuralbit.letsnote.ui.addEditNote

import android.Manifest
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
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.util.Log
import android.util.Patterns
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
import androidx.core.hardware.fingerprint.FingerprintManagerCompat
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
import com.neuralbit.letsnote.R
import com.neuralbit.letsnote.firebase.entities.LabelFire
import com.neuralbit.letsnote.firebase.entities.NoteFireIns
import com.neuralbit.letsnote.firebase.entities.TodoItem
import com.neuralbit.letsnote.receivers.AlertReceiver
import com.neuralbit.letsnote.receivers.DeleteReceiver
import com.neuralbit.letsnote.ui.label.LabelViewModel
import com.neuralbit.letsnote.ui.main.MainActivity
import com.neuralbit.letsnote.utilities.*
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.*
import java.util.regex.Matcher


class AddEditNoteActivity : AppCompatActivity() ,
    TagRVInterface,
    GetTimeFromPicker,
    GetDateFromPicker,
    GetTagFromDialog,
    LabelClickInterface,
    TodoItemInterface{
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
    private lateinit var webLinkRV : RecyclerView
    private lateinit var todoCheckBox: CheckBox
    private lateinit var todoItemDescTV : EditText
    private var todoItemChecked: Boolean = false
    private lateinit var todoRVAdapter : TodoRVAdapter
    private lateinit var tagListAdapter : AddEditTagRVAdapter
    private lateinit var webLinkAdapter : WebLinkAdapter
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
        protected = intent.getBooleanExtra("protected", false)
        if (protected){
            window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        }
        setContentView(R.layout.activity_add_edit_note)

        initControllers()


        checkCameraPermission()
        if(intent?.action == Intent.ACTION_SEND){
            if("text/plain" == intent.type){
                intent.getStringExtra(Intent.EXTRA_TEXT)?.let {
                    noteDesc = it
                    viewModal.noteChanged.value = true
                    noteDescriptionEdit.setText(it)
                    val phoneNumbers = extractPhoneNumber(it)
                    val webLinks = extractUrl(it)
                    val emailLinks = extractEmail(it)
                    val webPhoneLinks = emailLinks + webLinks + phoneNumbers
                    Log.d(TAG, "onCreate: $webPhoneLinks")
                    viewModal.webPhoneLinks.value = webPhoneLinks
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
            val addTagDialog = AddTagDialog(this,this@AddEditNoteActivity)
            addTagDialog.tagList = tagList
            addTagDialog.show(supportFragmentManager,"addTagDialogs")

        }
        when (noteType) {
            "Edit" -> {
                noteTitleEdit.setText(noteTitle)

                noteDescriptionEdit.setText(noteDesc)
                if ( noteDesc != null){
                    val phoneNumbers = extractPhoneNumber(noteDesc!!)
                    val webLinks = extractUrl(noteDesc!!)
                    val emailLinks = extractEmail(noteDesc!!)
                    val webPhoneLinks = emailLinks + webLinks + phoneNumbers
                    viewModal.webPhoneLinks.value = webPhoneLinks
                }
                tvTimeStamp.visibility =VISIBLE
                redoUndoGroup.visibility = GONE

                tvTimeStamp.text= getString(R.string.timeStamp,cm.convertLongToTime(noteTimeStamp)[0],cm.convertLongToTime(noteTimeStamp)[1])

                tagListAdapter.updateList(viewModal.oldTagList)
            }
            else -> {
                if (noteType != "NewTodo"){
                    redoUndoGroup.visibility = VISIBLE
                }

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


        viewModal.deletedNote.observe(lifecycleOwner){
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
        helper.setUndoButton(undoButton)
        helper.setRedoButton(redoButton)
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
                viewModal.deleteIgnored.value = true

                if(p3>0){

                    if(!tagListAdapter.deleteIgnored){
                        viewModal.noteChanged.value = true
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
                viewModal.deleteIgnored.value = true
                tvTimeStamp.visibility =GONE
                redoUndoGroup.visibility = VISIBLE
                if(p0?.length!! > 0){
                    if (p0[p0.length -1] == ' '){
                        val phoneNumbers = extractPhoneNumber(p0.toString())
                        val webLinks = extractUrl(p0.toString())
                        val emailLinks = extractEmail(p0.toString())
                        val webPhoneLinks = emailLinks + webLinks + phoneNumbers
                        viewModal.webPhoneLinks.value = webPhoneLinks
                    }
                    noteListBullet()

                    getTagFromString(p0)

                }

            }

            override fun afterTextChanged(p0: Editable?) {

            }
        })



        noteDescriptionEdit.setOnKeyListener { _, key, _ ->
            viewModal.backPressed.value = key == KeyEvent.KEYCODE_DEL


            return@setOnKeyListener false
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
            if (it) {
                pinItem?.setIcon(R.drawable.ic_baseline_push_pin_24)

            } else {
                pinItem?.setIcon(R.drawable.ic_outline_push_pin_24)
            }
        }
        window.statusBarColor = resources.getColor(R.color.black)
        window.navigationBarColor = resources.getColor(R.color.black)
        supportActionBar?.setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.black)))

        viewModal.labelColor.observe(this){
            labelColor = it
            if (it>0){
                window.statusBarColor = cm.darkenColor(it, 0.9f)
                window.navigationBarColor = cm.darkenColor(it, 0.9f)
                supportActionBar?.setBackgroundDrawable(ColorDrawable(cm.darkenColor(it, 0.9f)))

                noteDescriptionEdit.setTextColor(cm.darkenColor(it,0.9f))
                noteTitleEdit.setTextColor(cm.darkenColor(it,0.9f))
                noteTitleEdit.setHintTextColor(cm.darkenColor(it,0.6f))
                noteDescriptionEdit.setHintTextColor(cm.darkenColor(it,0.6f))

                todoRVAdapter.updateTextColor(cm.darkenColor(it, 0.9f))
                coordinatorlayout.setBackgroundColor(it)
                delLabelBtn.visibility = VISIBLE

            }else{
                coordinatorlayout.setBackgroundColor(Color.TRANSPARENT)
                noteDescriptionEdit.setTextColor(resources.getColor(R.color.black))
                todoRVAdapter.updateTextColor(resources.getColor(R.color.black))
                window.statusBarColor = resources.getColor(R.color.black)
                window.navigationBarColor = resources.getColor(R.color.black)
                supportActionBar?.setBackgroundDrawable(ColorDrawable(cm.darkenColor(it, 0.9f)))
                delLabelBtn.visibility = GONE

            }


        }



        noteTitleEdit.setOnKeyListener { _, _, _ ->
            viewModal.noteChanged.value = true
            tvTimeStamp.visibility = GONE

            false
        }

        viewModal.noteChanged.observe(this) {
            textChanged = it
            if(it){
                tvTimeStamp.visibility = GONE
            }else{
                tvTimeStamp.visibility = VISIBLE
                redoUndoGroup.visibility = GONE
            }
        }

        viewModal.webPhoneLinks.observe(this){
            webLinkAdapter.updateWebPhoneLinkList(it)
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
                Snackbar.make(coordinatorlayout, resources.getString(R.string.no_internet_connection), Snackbar.LENGTH_SHORT).show()
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
    private fun isFingerPrintAvailable(): Boolean {
        val fingerprintManager = FingerprintManagerCompat.from(applicationContext)
        return fingerprintManager.isHardwareDetected && fingerprintManager.hasEnrolledFingerprints()
    }

    private fun requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this@AddEditNoteActivity,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        ) {
            AlertDialog.Builder(this@AddEditNoteActivity)
                .setTitle("Permission needed")
                .setMessage("This permission is needed because we need to access your storage")
                .setPositiveButton(
                    "ok"
                ) { _: DialogInterface?, _: Int ->
                    ActivityCompat.requestPermissions(
                        this@AddEditNoteActivity,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        GALLERY_REQUEST
                    )
                }
                .setNegativeButton(
                    "cancel"
                ) { dialog: DialogInterface, _: Int -> dialog.dismiss() }
                .create().show()
        } else {
            ActivityCompat.requestPermissions(
                this@AddEditNoteActivity,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                GALLERY_REQUEST
            )
        }
    }

    private fun extractUrl(text: String): List<WebPhoneLink> {
        val webPhoneLinks: MutableList<WebPhoneLink> = ArrayList()
        val matcher: Matcher = Patterns.WEB_URL.matcher(text)
        while (matcher.find()) {
            val start: Int = matcher.start()
            if (start > 0 && text[start - 1] == '@') {
                continue
            } //from w  w w  .j av a  2s .  c o  m
            var url: String = matcher.group()
            if (!url.startsWith("http")) {
                url = "http://$url"
            }
            val webPhoneLink = WebPhoneLink(WebPhoneType.WEB, url)
            webPhoneLinks.add(webPhoneLink)
        }
        return webPhoneLinks
    }

    private fun extractPhoneNumber(text: String): List<WebPhoneLink> {
        val phoneLinks: MutableList<WebPhoneLink> = ArrayList()
        val matcher: Matcher = Patterns.PHONE.matcher(text)
        while (matcher.find()) {
            val phoneNumber: String = matcher.group()
            val webPhoneLink = WebPhoneLink(WebPhoneType.PHONE_NUMBER, phoneNumber)
            phoneLinks.add(webPhoneLink)
        }
        return phoneLinks
    }

    private fun extractEmail(text: String): List<WebPhoneLink> {
        val emailLinks: MutableList<WebPhoneLink> = ArrayList()
        val matcher: Matcher = Patterns.EMAIL_ADDRESS.matcher(text)
        while (matcher.find()) {
            val email: String = matcher.group()
            val webPhoneLink = WebPhoneLink(WebPhoneType.EMAIL, email)
            emailLinks.add(webPhoneLink)
        }
        return emailLinks
    }


    private fun deleteNote(){
        val alertDialog: AlertDialog? = this@AddEditNoteActivity.let {
            val builder = AlertDialog.Builder(this@AddEditNoteActivity)
            builder.apply {
                setPositiveButton(resources.getString(R.string.yes)
                ) { _, _ ->
                    viewModal.deletedNote.value = true
                    viewModal.noteChanged.value = true
                    goToMain()
                }

                setNegativeButton(resources.getString(R.string.cancel)
                ) { _, _ ->
                }
                setTitle(getString(R.string.delete_note))

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
            val allowedIndents = ArrayList<String>()
            allowedIndents.add("      ")
            allowedIndents.add("     ")
            allowedIndents.add("    ")
            allowedIndents.add("   ")
            allowedIndents.add("  ")

            val allPrefixes = ArrayList<String>();
            allPrefixes.addAll(allowedIndents)
            allPrefixes.addAll(listOf(" ", "->", "-", "+", "*", ">"))
            for (p in allPrefixes) {
                if (!backPressed) {
                    addBulletin(noteContentSplit, lineIndex, noteContent, p, allowedIndents)

                }
            }
            if ((noteContentSplit[lineIndex].endsWith(":")) || (noteContentSplit[lineIndex].endsWith(": "))) {
                if (noteContent.endsWith("\n")) {
                    if (!backPressed) {
                        noteDescriptionEdit.append("+ ")

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
                this@AddEditNoteActivity,
                Manifest.permission.CAMERA
            )
        ) {
            AlertDialog.Builder(this@AddEditNoteActivity)
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
                this@AddEditNoteActivity,
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_CAMERA_CODE
            )
        }
    }

    private fun recognizeText(){
        val recognizer = TextRecognizer.Builder(applicationContext).build()
        if (!recognizer.isOperational){
            Toast.makeText(applicationContext,getString(R.string.recognizer_unavailable),Toast.LENGTH_SHORT).show()
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
        var snackbar = Snackbar.make(coordinatorlayout,getString(R.string.note_archived),Snackbar.LENGTH_LONG)
        snackbar.setAction("UNDO"
        ) {
            viewModal.archived.value= false
            snackbar = Snackbar.make(coordinatorlayout,getString(R.string.note_restored),Snackbar.LENGTH_SHORT)
        }
        snackbar.show()


    }

    private fun addBulletin(noteContentSplit : List<String>, lineIndex : Int ,noteContent : Editable, prefix: String, allowedIndents : ArrayList<String>){
        if (noteContentSplit[lineIndex].startsWith(prefix)){

            if(noteContent.endsWith("\n")){
                if (allowedIndents.contains(prefix)){
                    noteDescriptionEdit.append(prefix)
                }else{
                    noteDescriptionEdit.append("$prefix ")
                }
            }
        }
    }

    private fun unArchiveNote(){
        viewModal.noteChanged.value = true
        if(viewModal.archived.value == true) {
            viewModal.archived.value = false
            var snackbar = Snackbar.make(coordinatorlayout, getString(R.string.note_restored), Snackbar.LENGTH_LONG)
            snackbar.setAction(
                "UNDO"
            ) {
                viewModal.archived.value = true
                snackbar = Snackbar.make(coordinatorlayout, getString(R.string.note_archived), Snackbar.LENGTH_SHORT)
            }
            snackbar.show()
        }
    }
    private fun unDelete(){
        viewModal.noteChanged.value = true
        if(viewModal.deletedNote.value == true) {
            viewModal.deletedNote.value = false
            var snackbar = Snackbar.make(coordinatorlayout, resources.getString(R.string.note_restored), Snackbar.LENGTH_LONG)
            snackbar.setAction(
                "UNDO"
            ) {
                viewModal.deletedNote.value = true
                snackbar = Snackbar.make(coordinatorlayout, resources.getString(R.string.note_deleted), Snackbar.LENGTH_SHORT)
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
        createShortcut()

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
        noteType = intent.getStringExtra("noteType").toString()
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
            viewModal.noteLocked.value = protected
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
        alertBottomSheet =  BottomSheetDialog(this@AddEditNoteActivity)
        labelBottomSheet = BottomSheetDialog(this@AddEditNoteActivity)
        coordinatorlayout = findViewById(R.id.coordinatorlayout)
        layoutManager.orientation = HORIZONTAL
        tagListAdapter= AddEditTagRVAdapter(applicationContext,this)
        labelListAdapter= AddEditLabelAdapter(applicationContext,this)
        todoRVAdapter = TodoRVAdapter(applicationContext,this)
        webLinkAdapter = WebLinkAdapter(applicationContext)
        labelBottomSheet.setContentView(R.layout.note_label_bottom_sheet)
        delLabelBtn = labelBottomSheet.findViewById(R.id.delLabel)!!
        tagListRV.layoutManager= layoutManager
        tagListRV.adapter = tagListAdapter
        addTodoButton = findViewById(R.id.addTodo)
        dismissTodoButton = findViewById(R.id.dismissTodoBtn)
        noteDescScrollView = findViewById(R.id.scrollView2)
        todoRV = findViewById(R.id.todoRV)
        webLinkRV = findViewById(R.id.LinkListRV)
        val layoutManagerTodo = LinearLayoutManager(applicationContext,LinearLayoutManager.VERTICAL,false)
        todoRV.layoutManager = layoutManagerTodo
        
        val layoutManagerWebLink = LinearLayoutManager(applicationContext,LinearLayoutManager.VERTICAL,false)
        webLinkRV.layoutManager = layoutManagerWebLink
        
        webLinkRV.adapter = webLinkAdapter
        todoRV.adapter = todoRVAdapter
        todoCheckBox = findViewById(R.id.todoCheckBox)
        todoItemDescTV = findViewById(R.id.todoItemDescTV)
        lifecycleOwner = this
        todoRVAdapter.viewModel = viewModal
        todoRVAdapter.lifecycleOwner = this
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
        tagListAdapter.viewModel = viewModal
        tagListAdapter.lifecycleOwner = lifecycleOwner
        tagListAdapter.defaultTextColor = resources.getColor(R.color.black)

        viewModal.allTodoItems.value = todoDoItemList
        if (oldLabel > 0){
            viewModal.labelColor.value = oldLabel
        }

        val fontStyle = settingsPref.getString("font",null)
        todoItemDescTV.setTextSize(TypedValue.COMPLEX_UNIT_SP,18+ ((fontMultiplier-2)*2).toFloat())


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            try{
                val typeface: Typeface? = when (fontStyle) {
                    cm.ARCHITECTS_DAUGHTER -> {
                        ResourcesCompat.getFont(applicationContext, R.font.architects_daughter)
                    }
                    cm.ABREEZE -> {
                        ResourcesCompat.getFont(applicationContext, R.font.abeezee)
                    }
                    cm.ADAMINA -> {
                        ResourcesCompat.getFont(applicationContext, R.font.adamina)
                    }
                    cm.BELLEZA-> {
                        ResourcesCompat.getFont(applicationContext, R.font.belleza)
                    }
                    cm.JOTI_ONE -> {
                        ResourcesCompat.getFont(applicationContext, R.font.joti_one)
                    }
                    cm.NOVA_FLAT -> {
                        ResourcesCompat.getFont(applicationContext, R.font.nova_flat)
                    }
                    else -> {
                        ResourcesCompat.getFont(applicationContext, R.font.roboto)
                    }
                }


                todoItemDescTV.typeface = typeface
                todoRVAdapter.fontStyle = fontStyle
                todoRVAdapter.fontMultiplier = fontMultiplier
                noteDescriptionEdit.typeface = typeface
                noteTitleEdit.typeface = typeface
                reminderTV.typeface = typeface
                tvTimeStamp.typeface = typeface
            }catch (_ : Exception){

            }

        }
        if (noteType == "NewTodo"){
            noteDescriptionEdit.visibility = GONE
            todoRV.visibility = VISIBLE
            todoItemDescTV.visibility = VISIBLE
            todoCheckBox.visibility = VISIBLE
            ocrButton.visibility = GONE
        }
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
                viewModal.reminderSet.value = false
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
            if (!protected){
                Toast.makeText(applicationContext,getString(R.string.note_locked),Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(applicationContext,getString(R.string.note_unlocked),Toast.LENGTH_SHORT).show()
            }
            viewModal.noteLocked.value = !protected
            return@setOnMenuItemClickListener true
        }
        try {
            lockNoteItem?.isVisible = isFingerPrintAvailable()
        }catch (ignored : Exception){

        }


        return super.onCreateOptionsMenu(menu)
    }

    private fun shareNote() {
        noteTitle = noteTitleEdit.text.toString()
        noteDesc = noteDescriptionEdit.text.toString()
        if (noteTitle?.isNotBlank() == true || noteDesc?.isNotBlank() == true){

            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND

                var textToShare : String ? = if (noteTitle!!.isNotBlank()){
                    noteTitle + "\n" + noteDesc
                }else{
                    noteDesc
                }
                for (todoItem in viewModal.todoItems) {
                    textToShare += if (todoItem.checked){
                        "\n + ${todoItem.item} "
                    }else{
                        "\n - ${todoItem.item} "
                    }
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
            var snackbar = Snackbar.make(coordinatorlayout,getString(R.string.note_unpinned),Snackbar.LENGTH_LONG)
            snackbar.setAction(getString(R.string.undo)) {
                viewModal.pinned.value = true

                snackbar = Snackbar.make(coordinatorlayout,getString(R.string.note_pinned),Snackbar.LENGTH_SHORT)
                snackbar.show()
            }
            snackbar.show()
        }else{
            viewModal.pinned.value = true

            var snackbar = Snackbar.make(coordinatorlayout,getString(R.string.note_pinned),Snackbar.LENGTH_LONG)
            snackbar.setAction(getString(R.string.undo)) {
                viewModal.pinned.value = false

                snackbar = Snackbar.make(coordinatorlayout,getString(R.string.note_unpinned),Snackbar.LENGTH_SHORT)
                snackbar.show()
            }
            snackbar.show()
        }

    }

    private fun createShortcut() {
        val shortcutManager = getSystemService(ShortcutManager::class.java)
        shortcutManager.disableShortcuts(listOf("newNote","newTodo"))
        shortcutManager.removeAllDynamicShortcuts()
        val intent = Intent(applicationContext, AddEditNoteActivity::class.java)
        intent.action = Intent.ACTION_VIEW
        val newNoteShortcut = ShortcutInfoCompat.Builder(applicationContext, "newNote")
            .setShortLabel(getString(R.string.note_shortcut_short_label))
            .setLongLabel(getString(R.string.note_shortcut_long_label))
            .setIcon(
                IconCompat.createWithResource(applicationContext,
                R.drawable.ic_baseline_mode_edit_24
            ))
            .setIntent(intent) // Push the shortcut
            .build()
        ShortcutManagerCompat.pushDynamicShortcut(applicationContext, newNoteShortcut)

        intent.putExtra("noteType","NewTodo")


        val newTodoShortcut = ShortcutInfoCompat.Builder(applicationContext, "newTodo")
            .setShortLabel(getString(R.string.todo_shortcut_short_label))
            .setLongLabel(getString(R.string.todo_shortcut_long_label))
            .setIcon(IconCompat.createWithResource(applicationContext,
                R.drawable.ic_baseline_checklist_rtl_24
            ))
            .setIntent(intent) // Push the shortcut
            .build()

        ShortcutManagerCompat.pushDynamicShortcut(applicationContext, newTodoShortcut)


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
            val labelAlertLayout = layoutInflater.inflate(R.layout.add_label_dialog,null)
            val labelConfirmBtn = labelAlertLayout.findViewById<Button>(R.id.okayBtn)
            val labelDismissBtn = labelAlertLayout.findViewById<Button>(R.id.cancelBtn)

            val labelDialog: AlertDialog = this@AddEditNoteActivity.let {

                val builder = AlertDialog.Builder(it)
                builder.setView(labelAlertLayout)
                builder.create()

            }
            labelDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));

            labelConfirmBtn.setOnClickListener {
                viewModal.noteChanged.value = true
                viewModal.labelChanged = true
                if (viewModal.labelColor.value != null){

                    val label = viewModal.labelTitle.value?.let { it1 -> LabelFire(labelColor= viewModal.labelColor.value!!,labelTitle= it1) }
                    if (label != null) {
                        viewModal.labelFireList.add(label)
                        labelListAdapter.updateLabelIDList(viewModal.labelFireList)
                    }
                }
                labelDialog.dismiss()
                labelBottomSheet.dismiss()
            }
            labelDismissBtn.setOnClickListener {
                labelDialog.dismiss()
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
                val hex = ColorTransparentUtils.transparentColor(it,50)
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

                    Toast.makeText(this@AddEditNoteActivity, resources.getString(R.string.reminder_set_today,"8:00 am"), Toast.LENGTH_SHORT).show()

                }
                in 5..8 -> {
                    calendar[Calendar.HOUR_OF_DAY] = 14
                    calendar[Calendar.MINUTE] = 0
                    Toast.makeText(this@AddEditNoteActivity, resources.getString(R.string.reminder_set_today,"2:00 pm"), Toast.LENGTH_SHORT).show()

                }
                in 9..14 ->{
                    calendar[Calendar.HOUR_OF_DAY] = 18
                    calendar[Calendar.MINUTE] = 0
                    Toast.makeText(this@AddEditNoteActivity, resources.getString(R.string.reminder_set_today,"6:00 pm"), Toast.LENGTH_SHORT).show()

                }
                in 15..18 -> {
                    calendar[Calendar.HOUR_OF_DAY] = 20
                    calendar[Calendar.MINUTE] = 0
                    Toast.makeText(this@AddEditNoteActivity, resources.getString(R.string.reminder_set_today,"8:00 pm"), Toast.LENGTH_SHORT).show()
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
            Toast.makeText(this@AddEditNoteActivity, resources.getString(R.string.reminder_set_tomorrow,"8:00 pm"), Toast.LENGTH_SHORT).show()

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

            Toast.makeText(this,  resources.getString(R.string.reminder_set_tomorrow,"6:00 pm"), Toast.LENGTH_SHORT).show()

        }
    }


    private fun openDateTimeDialog(){

        val dateTimeLayout = layoutInflater.inflate(R.layout.alert_datetime_dialog,null)
        val dateTimeDismissBtn = dateTimeLayout.findViewById<Button>(R.id.cancelBtn)
        val dateTimeConfirmBtn = dateTimeLayout.findViewById<Button>(R.id.okayBtn)
        val dateTimeDialog = AlertDialog.Builder(this@AddEditNoteActivity)
            .setView(dateTimeLayout)
            .create()
        dateTimeDialog.show()
        dateTimeDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        dateTimeDismissBtn.setOnClickListener {
            dateTimeDialog.dismiss()
        }
        dateTimeConfirmBtn.setOnClickListener {
            if(noteDescriptionEdit.length() > 0 || noteTitleEdit.length() >0 ){
                viewModal.noteChanged.value = true
            }
                viewModal.reminderTime = calendar.timeInMillis
                viewModal.reminderSet.value = true
                Toast.makeText(applicationContext,resources.getString(R.string.reminder, DateFormat.getDateFormat(applicationContext).format(calendar.time) , DateFormat.getTimeFormat(applicationContext).format(calendar.time)),Toast.LENGTH_SHORT).show()

            dateTimeDialog.dismiss()
        }

        val timePickerBtn=dateTimeDialog?.findViewById<View>(R.id.timePickButton)
        val datePickerBtn = dateTimeDialog?.findViewById<ImageButton>(R.id.datePickButton)
        timeTitleTV = dateTimeDialog?.findViewById(R.id.timeTitle)!!
        dateTitleTV = dateTimeDialog.findViewById(R.id.dateTitle)

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
        val intent = Intent(this@AddEditNoteActivity, AlertReceiver::class.java)
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
        val toDoItemString: String = Gson().toJson(viewModal.todoItems)
        intent.putExtra("todoItems", toDoItemString)

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
        val intent = Intent(this@AddEditNoteActivity, AlertReceiver::class.java)
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
                val emptyTrashImmediately = settingsPref.getBoolean("EmptyTrashImmediately",false)

                if (viewModal.deletedNote.value != true){
                    if(noteType == "Edit" && noteUid!= null){
                        cancelDelete(viewModal.reminderTime.toInt())
                        val noteUpdate = HashMap<String,Any>()
                        noteUpdate["title"] = noteTitle
                        noteUpdate["description"] = noteDescription
                        noteUpdate["timeStamp"] = currentDate
                        noteUpdate["label"] = labelColor
                        noteUpdate["pinned"] = viewModal.pinned.value == true
                        noteUpdate["deletedDate"] = 0
                        noteUpdate["archived"] = viewModal.archived.value == true
                        noteUpdate["reminderDate"] = viewModal.reminderTime
                        noteUpdate["todoItems"] = viewModal.todoItems
                        noteUpdate["protected"] = viewModal.noteLocked.value == true
                        noteUpdate["tags"] = tags
                        noteUid?.let { viewModal.updateFireNote(noteUpdate, it) }

                        if (!archived){
                            saveOtherEntities()
                            Toast.makeText(this,getString(R.string.note_updated) , Toast.LENGTH_SHORT).show()
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
                            noteFire.archived = viewModal.archived.value == true
                            noteUid =  viewModal.addFireNote(noteFire)
                            saveOtherEntities()

                        }
                        Toast.makeText(this,getString(R.string.note_saved) , Toast.LENGTH_SHORT).show()

                    }
                }else{
                    if (!emptyTrashImmediately){
                        cancelDelete(viewModal.reminderTime.toInt())
                        val noteUpdate = HashMap<String,Any>()
                        noteUpdate["title"] = noteTitle
                        noteUpdate["description"] = noteDescription
                        noteUpdate["timeStamp"] = currentDate
                        noteUpdate["label"] = labelColor
                        noteUpdate["pinned"] = viewModal.pinned.value == true
                        noteUpdate["deletedDate"] = calendar.timeInMillis
                        noteUpdate["reminderDate"] = viewModal.reminderTime
                        noteUpdate["todoItems"] = viewModal.todoItems
                        noteUpdate["protected"] = viewModal.noteLocked.value == true
                        noteUpdate["tags"] = tags
                        noteUid?.let { viewModal.updateFireNote(noteUpdate, it) }
                        noteUid?.let { scheduleDelete(it, tags,labelColor,noteTimeStamp) }
                    }else{
                        noteUid?.let { viewModal.deleteNote(it, labelColor,tags) }
                    }
                    Toast.makeText(this@AddEditNoteActivity,getString(R.string.note_deleted),Toast.LENGTH_SHORT).show()

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


    override fun onDestroy() {

        Log.d(TAG, "onDestroy: App destroyed")
        super.onDestroy()
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

        this.calendar[Calendar.HOUR_OF_DAY]= calendar[Calendar.HOUR_OF_DAY]
        this.calendar[Calendar.MINUTE]= calendar[Calendar.MINUTE]
        this.calendar[Calendar.SECOND]= calendar[Calendar.SECOND]
        timeTitleTV.text= resources.getString(R.string.date_time_set,"Time ", DateFormat.getTimeFormat(applicationContext).format(calendar.time))
    }

    override fun getDateInfo(calendar : Calendar) {
        this.calendar[Calendar.DAY_OF_MONTH] = calendar[Calendar.DAY_OF_MONTH]
        this.calendar[Calendar.MONTH] = calendar[Calendar.MONTH]
        this.calendar[Calendar.YEAR] = calendar[Calendar.YEAR]
        dateTitleTV.text= resources.getString(R.string.date_time_set,"Date ", DateFormat.getDateFormat(applicationContext).format(calendar.time))
    }

    override fun getTag(tag: String) {
        var tagTitle = tag
        if(tagTitle.isNotEmpty()){
            if (tagTitle[0] != '#'){
                tagTitle = "#$tag"
            }
        }
        val tags = HashSet<String>()
        tags.addAll(viewModal.oldTagList)
        viewModal.newTags.add(tagTitle)
        tags.addAll(viewModal.newTags)
        tagListAdapter.updateList(ArrayList(tags))
        viewModal.noteChanged.value = true
    }

    override fun onLabelItemClick(labelColor: Int, labelTitle : String?) {
        viewModal.labelColor.value = labelColor
        viewModal.labelTitle.value = labelTitle
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