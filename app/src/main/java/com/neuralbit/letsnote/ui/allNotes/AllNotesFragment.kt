package com.neuralbit.letsnote.ui.allNotes

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.neuralbit.letsnote.R
import com.neuralbit.letsnote.databinding.FragmentAllNotesBinding
import com.neuralbit.letsnote.firebase.entities.NoteFire
import com.neuralbit.letsnote.receivers.AlertReceiver
import com.neuralbit.letsnote.receivers.DeleteReceiver
import com.neuralbit.letsnote.ui.adapters.NoteFireClick
import com.neuralbit.letsnote.ui.adapters.NoteRVAdapter
import com.neuralbit.letsnote.ui.addEditNote.AddEditNoteActivity
import com.neuralbit.letsnote.ui.addEditNote.Fingerprint
import com.neuralbit.letsnote.ui.settings.SettingsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class AllNotesFragment : Fragment() , NoteFireClick {

    private val allNotesViewModel: AllNotesViewModel by activityViewModels()
    private val settingsViewModel: SettingsViewModel by activityViewModels()
    private var _binding: FragmentAllNotesBinding? = null
    val TAG = "HOMEFRAGMENT"
    private lateinit var  notesRV: RecyclerView
    private lateinit var  pinnedNotesRV: RecyclerView
    private lateinit var addNoteFAB : FloatingActionButton
    private lateinit var addNoteTV : TextView
    private lateinit var addTodoTV : TextView
    private lateinit var welcomeIcon : ImageView
    private lateinit var welcomeText : TextView
    private val binding get() = _binding!!
    private lateinit var pinnedNotesTV: TextView
    private lateinit var otherNotesTV: TextView
    private lateinit var noteTypeFAB : FloatingActionButton
    private lateinit var addTodoFAB : FloatingActionButton
    private val rotateOpen: Animation by lazy { AnimationUtils.loadAnimation(context,R.anim.rotate_open_anim)}
    private val rotateClose: Animation by lazy { AnimationUtils.loadAnimation(context,R.anim.rotate_close_anim)}
    private val fromBottom: Animation by lazy { AnimationUtils.loadAnimation(context,R.anim.from_bottom_anim)}
    private val toBottom: Animation by lazy { AnimationUtils.loadAnimation(context,R.anim.to_bottom)}
    private var clicked = false
    private var reviewSharedPrefEditor : SharedPreferences.Editor ? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAllNotesBinding.inflate(inflater, container, false)

        val root: View = binding.root
        addNoteFAB = binding.FABAddNote
        addTodoFAB = binding.FABAddTodoList
        noteTypeFAB = binding.FABNoteType
        addNoteTV = binding.noteTitleTv
        addTodoTV = binding.noteTodoTv
        notesRV = binding.notesRV
        pinnedNotesRV = binding.pinnedNotesRV
        pinnedNotesTV = binding.pinnedNotesTV
        otherNotesTV = binding.otherNotesTV
        welcomeIcon = binding.welcomeIcon
        welcomeText = binding.allNotesText
        val settingsSharedPref = context?.getSharedPreferences("Settings", AppCompatActivity.MODE_PRIVATE)
        val staggeredLayoutManagerAll = StaggeredGridLayoutManager( 2,LinearLayoutManager.VERTICAL)
        val staggeredLayoutManagerPinned = StaggeredGridLayoutManager( 2,LinearLayoutManager.VERTICAL)
        notesRV.layoutManager = staggeredLayoutManagerAll
        pinnedNotesRV.layoutManager =staggeredLayoutManagerPinned
        allNotesViewModel.deleteFrag.value = false
        settingsViewModel.settingsFrag.value = false
        allNotesViewModel.archiveFrag = false
        allNotesViewModel.staggeredView.value = settingsSharedPref?.getBoolean("staggered",true)

        allNotesViewModel.staggeredView.observe(viewLifecycleOwner){
            val editor: SharedPreferences.Editor ?= settingsSharedPref?.edit()
            editor?.putBoolean("staggered",it)
            editor?.apply()
            if (it){
                notesRV.layoutManager = staggeredLayoutManagerAll
                pinnedNotesRV.layoutManager =staggeredLayoutManagerPinned
            }else{

                notesRV.layoutManager = LinearLayoutManager(context)
                pinnedNotesRV.layoutManager = LinearLayoutManager(context)
            }
        }
        addNoteFAB.setOnClickListener{
            val intent = Intent( context,AddEditNoteActivity::class.java)
            intent.putExtra("noteType","NewNote")
            startActivity(intent)
        }
        addTodoFAB.setOnClickListener{
            val intent = Intent( context,AddEditNoteActivity::class.java)
            intent.putExtra("noteType","NewTodo")
            startActivity(intent)
        }
        noteTypeFAB.setOnClickListener{
            setVisibility(clicked)
            setAnimation(clicked)
            clicked = !clicked
        }

        val fontStyle = settingsSharedPref?.getString("font",null)
        val noteRVAdapter = context?.let { NoteRVAdapter(it,this) }
        val pinnedNoteRVAdapter = context?.let { NoteRVAdapter(it,this) }
        notesRV.adapter= noteRVAdapter
        pinnedNotesRV.adapter = pinnedNoteRVAdapter
        noteRVAdapter?.viewModel = allNotesViewModel
        noteRVAdapter?.lifecycleScope = lifecycleScope
        noteRVAdapter?.lifecycleOwner = this
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){

            noteRVAdapter?.fontStyle = fontStyle
            pinnedNoteRVAdapter?.fontStyle = fontStyle

        }
        pinnedNoteRVAdapter?.viewModel = allNotesViewModel
        pinnedNoteRVAdapter?.lifecycleScope = lifecycleScope
        pinnedNoteRVAdapter?.lifecycleOwner = this
        setHasOptionsMenu(true)
        val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
        mAuth.addAuthStateListener {
            val otherNotes = allNotesViewModel.otherFireNotesList.value
            val pinnedNotes = allNotesViewModel.pinnedFireNotesList.value
            if (pinnedNotes?.isNotEmpty() == true){
                otherNotesTV.visibility = VISIBLE
                pinnedNotesTV.visibility = VISIBLE
                pinnedNotesRV.visibility = VISIBLE
                pinnedNoteRVAdapter?.updateListFire(pinnedNotes)

            }else{
                otherNotesTV.visibility = GONE
                pinnedNotesTV.visibility = GONE
                pinnedNotesRV.visibility = GONE
            }
            if (otherNotes != null) {
                noteRVAdapter?.updateListFire(otherNotes)
            }

        }

        allNotesViewModel.selectedNotes.clear()

        lifecycleScope.launch(Dispatchers.Main) {
            allNotesViewModel.getAllFireNotes().observe(viewLifecycleOwner){
                allNotesViewModel.allFireNotes.value = it

            }
        }

        allNotesViewModel.allFireNotes.observe(viewLifecycleOwner){ notes ->
            val pinnedNotes = LinkedList<NoteFire>()
            val otherNotes = LinkedList<NoteFire>()

            for (note in notes) {
                if (note.deletedDate == (0).toLong()){
                    if (!note.archived){
                        if (note.pinned){
                            pinnedNotes.add(note)
                        }else{
                            otherNotes.add(note)
                        }
                    }
                }
            }
            if (otherNotes.isEmpty() && pinnedNotes.isEmpty()){
                welcomeIcon.visibility = VISIBLE
                welcomeText.visibility = VISIBLE
                pinnedNotesTV.visibility = GONE
                otherNotesTV.visibility = GONE
            }else{
                welcomeIcon.visibility = GONE
                welcomeText.visibility = GONE
            }


            allNotesViewModel.otherFireNotesList.value = otherNotes
            allNotesViewModel.pinnedFireNotesList.value = pinnedNotes
            if (pinnedNotes.isNotEmpty()){
                otherNotesTV.visibility = VISIBLE
                pinnedNotesTV.visibility = VISIBLE
                pinnedNotesRV.visibility = VISIBLE
                pinnedNoteRVAdapter?.updateListFire(pinnedNotes)

            }else{
                otherNotesTV.visibility = GONE
                pinnedNotesTV.visibility = GONE
                pinnedNotesRV.visibility = GONE
            }
            noteRVAdapter?.updateListFire(otherNotes)
        }





        allNotesViewModel.searchQuery.observe(viewLifecycleOwner) { str->

            allNotesViewModel.filterOtherFireList().observe(viewLifecycleOwner) {
                if (it.isEmpty()){
                    notesRV.isVisible = false
                    otherNotesTV.isVisible = false
                }else{
                    notesRV.isVisible = true
                    otherNotesTV.isVisible = true
                }
                noteRVAdapter?.updateListFire(it)
                noteRVAdapter?.searchString = str
            }
            allNotesViewModel.filterPinnedFireList().observe(viewLifecycleOwner) { pinnedNotes ->
                allNotesViewModel.filterOtherFireList().observe(viewLifecycleOwner) { otherNotes ->
                    if (pinnedNotes.isEmpty()){
                        pinnedNotesTV.isVisible = false
                        pinnedNotesRV.isVisible = false
                        otherNotesTV.isVisible = false
                    }else{
                        pinnedNotesTV.isVisible = true
                        pinnedNotesRV.isVisible = true
                        if (otherNotes.isNotEmpty()){
                            otherNotesTV.isVisible = true
                        }
                    }
                }

                pinnedNoteRVAdapter?.searchString = str
                pinnedNoteRVAdapter?.updateListFire(pinnedNotes)
            }
        }

        allNotesViewModel.itemArchiveClicked.observe(viewLifecycleOwner){
            if (it && allNotesViewModel.selectedNotes.isNotEmpty()){
                val selectedNotesCount = allNotesViewModel.selectedNotes.size
                for ( note in allNotesViewModel.selectedNotes){
                    if (note.pinned){
                        allNotesViewModel.pinnedFireNotesList.value?.remove(note)
                        cancelAlarm(note.reminderDate.toInt())
                        pinnedNoteRVAdapter?.notifyDataSetChanged()

                    }else{
                        allNotesViewModel.otherFireNotesList.value?.remove(note)
                        cancelAlarm(note.reminderDate.toInt())
                        noteRVAdapter?.notifyDataSetChanged()
                    }

                    val noteUpdate = HashMap<String,Any>()
                    noteUpdate["title"] = note.title
                    noteUpdate["description"] = note.description
                    noteUpdate["label"] = note.label
                    noteUpdate["pinned"] = note.pinned
                    noteUpdate["reminderDate"] = note.reminderDate
                    noteUpdate["protected"] = note.protected
                    noteUpdate["archived"] = true
                    note.noteUid?.let { it1 -> allNotesViewModel.updateFireNote(noteUpdate, it1) }
                }

                allNotesViewModel.otherFireNotesList.value?.let { list ->
                    noteRVAdapter?.updateListFire(list)
                }
                allNotesViewModel.pinnedFireNotesList.value?.let { list ->
                    pinnedNoteRVAdapter?.updateListFire(list)
                }

                allNotesViewModel.selectedNotes.clear()


                allNotesViewModel.itemSelectEnabled.value = false
                if (allNotesViewModel.otherFireNotesList.value?.isEmpty() == true && allNotesViewModel.pinnedFireNotesList.value?.isEmpty() == true){
                    welcomeIcon.visibility = VISIBLE
                    welcomeText.visibility = VISIBLE
                }
                if (selectedNotesCount ==1){
                    Toast.makeText(context,resources.getString(R.string.notes_archived_successfully,""),Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(context,resources.getString(R.string.notes_archived_successfully,"s"),Toast.LENGTH_SHORT).show()
                }

            }
        }

        allNotesViewModel.itemDeleteClicked.observe(viewLifecycleOwner){
            val settings = context?.getSharedPreferences("Settings", AppCompatActivity.MODE_PRIVATE)
            val emptyTrashImmediately = settings?.getBoolean("EmptyTrashImmediately",false)
            if (it && allNotesViewModel.selectedNotes.isNotEmpty()){
                val selectedNotesCount = allNotesViewModel.selectedNotes.size
                for ( note in allNotesViewModel.selectedNotes){
                    if (note.pinned){
                        allNotesViewModel.pinnedFireNotesList.value?.remove(note)
                        cancelAlarm(note.reminderDate.toInt())
                        pinnedNoteRVAdapter?.notifyDataSetChanged()

                    }else{
                        allNotesViewModel.otherFireNotesList.value?.remove(note)
                        cancelAlarm(note.reminderDate.toInt())
                        noteRVAdapter?.notifyDataSetChanged()
                    }
                    if (emptyTrashImmediately != true){
                        val noteUpdate = HashMap<String,Any>()
                        noteUpdate["title"] = note.title
                        noteUpdate["description"] = note.description
                        noteUpdate["label"] = note.label
                        noteUpdate["pinned"] = note.pinned
                        noteUpdate["reminderDate"] = note.reminderDate
                        noteUpdate["protected"] = note.protected
                        noteUpdate["deletedDate"] = System.currentTimeMillis()
                        note.noteUid?.let { it1 -> allNotesViewModel.updateFireNote(noteUpdate, it1) }

                        note.noteUid?.let { it1 -> scheduleDelete(it1,note.tags,note.label,note.timeStamp) }

                    }else{
                        allNotesViewModel.notesToDelete.value =  note
                    }


                }

                allNotesViewModel.otherFireNotesList.value?.let { list ->
                    noteRVAdapter?.updateListFire(list)
                }
                allNotesViewModel.pinnedFireNotesList.value?.let { list ->
                    pinnedNoteRVAdapter?.updateListFire(list)
                }

                allNotesViewModel.selectedNotes.clear()
                if (allNotesViewModel.otherFireNotesList.value?.isEmpty() == true && allNotesViewModel.pinnedFireNotesList.value?.isEmpty() == true){
                    welcomeIcon.visibility = VISIBLE
                    welcomeText.visibility = VISIBLE
                }

                allNotesViewModel.itemSelectEnabled.value = false
                allNotesViewModel.itemDeleteClicked.value = false

                allNotesViewModel.itemSelectEnabled.value = false
                if (selectedNotesCount ==1){
                    Toast.makeText(context,resources.getString(R.string.notes_deleted_successfully,""),Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(context,resources.getString(R.string.notes_deleted_successfully,"s"),Toast.LENGTH_SHORT).show()
                }
            }
        }
        return root
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        val trashButton = menu.findItem(R.id.trash)
        trashButton.isVisible = false
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun setAnimation(clicked: Boolean) {
        if (!clicked){
            noteTypeFAB.startAnimation(rotateOpen)
            addTodoFAB.startAnimation(fromBottom)
            addNoteFAB.startAnimation(fromBottom)
            addNoteTV.startAnimation(fromBottom)
            addTodoTV.startAnimation(fromBottom)
        }else{
            noteTypeFAB.startAnimation(rotateClose)
            addTodoFAB.startAnimation(toBottom)
            addNoteFAB.startAnimation(toBottom)
            addNoteTV.startAnimation(toBottom)
            addTodoTV.startAnimation(toBottom)
        }
    }

    private fun setVisibility(clicked : Boolean) {
        if (!clicked){
            addTodoFAB.visibility = VISIBLE
            addNoteFAB.visibility = VISIBLE
            addNoteTV.visibility = VISIBLE
            addTodoTV.visibility = VISIBLE
        }else{
            addTodoFAB.visibility = GONE
            addNoteFAB.visibility = GONE
            addNoteTV.visibility = GONE
            addTodoTV.visibility = GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun cancelAlarm(reminder : Int){
        val alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlertReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, reminder, intent, PendingIntent.FLAG_IMMUTABLE)
        alarmManager.cancel(pendingIntent)
    }


    private fun scheduleDelete( noteUid : String, tags : List<String>, label: Int , timeStamp : Long) {
        val alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, DeleteReceiver::class.java)

        intent.putExtra("noteUid",noteUid)
        intent.putExtra("timeStamp",System.currentTimeMillis())
        intent.putExtra("labelColor", label)
        intent.putStringArrayListExtra("tagList", ArrayList(tags))
        val pendingIntent = PendingIntent.getBroadcast(context, timeStamp.toInt(), intent, PendingIntent.FLAG_IMMUTABLE)
        val timeToDelete = timeStamp + 6.048e+8 // 7 days from the time it is softly deleted
        alarmManager.set(AlarmManager.RTC_WAKEUP, timeToDelete.toLong(), pendingIntent)
    }

    override fun onNoteFireClick(note: NoteFire, activated : Boolean) {
        reviewSharedPrefEditor?.apply()

        if (!note.selected && !activated){
            val intent : Intent = if(note.protected){
                Intent( context, Fingerprint::class.java)
            }else{
                Intent( context, AddEditNoteActivity::class.java)
            }
            intent.putExtra("noteType","Edit")
            intent.putExtra("noteTitle",note.title)
            intent.putExtra("noteDescription",note.description)
            intent.putExtra("noteUid",note.noteUid)
            intent.putExtra("timeStamp",note.timeStamp)
            intent.putExtra("labelColor",note.label)
            intent.putExtra("pinned",note.pinned)
            intent.putExtra("archieved",note.archived)
            intent.putExtra("protected",note.protected)
            val c = Calendar.getInstance()
            if (c.timeInMillis < note.reminderDate){
                intent.putExtra("reminder",note.reminderDate)
            }
            val toDoItemString: String = Gson().toJson(note.todoItems)
            intent.putExtra("todoItems", toDoItemString)
            intent.putStringArrayListExtra("tagList", ArrayList(note.tags))
            startActivity(intent)
        }else{
            if (note.selected){
                allNotesViewModel.selectedNotes.add(note)
            }else{
                allNotesViewModel.selectedNotes.remove(note)
            }
        }
    }

    override fun onNoteFireLongClick(note: NoteFire) {
        if (note.selected){
            allNotesViewModel.selectedNotes.add(note)
        }else{
            allNotesViewModel.selectedNotes.remove(note)
        }
        allNotesViewModel.itemSelectEnabled.value = true
    }

}
