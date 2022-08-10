package com.neuralbit.letsnote.ui.allNotes

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.neuralbit.letsnote.AddEditNoteActivity
import com.neuralbit.letsnote.Fingerprint
import com.neuralbit.letsnote.R
import com.neuralbit.letsnote.Services.DeleteReceiver
import com.neuralbit.letsnote.adapters.NoteFireClick
import com.neuralbit.letsnote.adapters.NoteRVAdapter
import com.neuralbit.letsnote.databinding.FragmentAllNotesBinding
import com.neuralbit.letsnote.entities.NoteFire
import com.neuralbit.letsnote.utilities.AlertReceiver
import java.util.*

class AllNotesFragment : Fragment() , NoteFireClick {

    private val allNotesViewModel: AllNotesViewModel by activityViewModels()
    private var _binding: FragmentAllNotesBinding? = null
    val TAG = "HOMEFRAGMENT"
    private lateinit var  notesRV: RecyclerView
    private lateinit var  pinnedNotesRV: RecyclerView
    private lateinit var addNoteFAB : FloatingActionButton
    private lateinit var welcomeIcon : ImageView
    private lateinit var welcomeText : TextView
    private val binding get() = _binding!!
    private lateinit var pinnedNotesTV: TextView
    private lateinit var otherNotesTV: TextView
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAllNotesBinding.inflate(inflater, container, false)

        val root: View = binding.root
        addNoteFAB = binding.FABAddNote
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



        val noteRVAdapter = context?.let { NoteRVAdapter(it,this) }
        val pinnedNoteRVAdapter = context?.let { NoteRVAdapter(it,this) }
        notesRV.adapter= noteRVAdapter
        pinnedNotesRV.adapter = pinnedNoteRVAdapter
        noteRVAdapter?.viewModel = allNotesViewModel
        noteRVAdapter?.lifecycleScope = lifecycleScope
        noteRVAdapter?.lifecycleOwner = this
        pinnedNoteRVAdapter?.viewModel = allNotesViewModel
        pinnedNoteRVAdapter?.lifecycleScope = lifecycleScope
        pinnedNoteRVAdapter?.lifecycleOwner = this
        allNotesViewModel.selectedNotes.clear()
        allNotesViewModel.getAllFireNotes().observe(viewLifecycleOwner){ notes ->

            val pinnedNotes = LinkedList<NoteFire>()
            val otherNotes = LinkedList<NoteFire>()
            for (note in notes) {
                val pref = context?.getSharedPreferences("DeletedNotes", AppCompatActivity.MODE_PRIVATE)
                val deletedNotes = pref?.getStringSet("noteUids", HashSet())
                if (deletedNotes != null){
                    if (!deletedNotes.contains(note.noteUid)){
                        if (!note.archived){
                            if (note.pinned){
                                pinnedNotes.add(note)
                            }else{
                                otherNotes.add(note)
                            }
                        }
                    }
                }else{
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
            allNotesViewModel.filterPinnedFireList().observe(viewLifecycleOwner) {
                pinnedNoteRVAdapter?.searchString = str
                pinnedNoteRVAdapter?.updateListFire(it)

            }
            allNotesViewModel.filterOtherFireList().observe(viewLifecycleOwner) {
                noteRVAdapter?.updateListFire(it)
                noteRVAdapter?.searchString = str
            }

        }

        allNotesViewModel.itemArchiveClicked.observe(viewLifecycleOwner){
            if (it && allNotesViewModel.selectedNotes.isNotEmpty()){

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

                Toast.makeText(context,"Notes archived successfully",Toast.LENGTH_SHORT).show()
            }
        }

        allNotesViewModel.itemDeleteClicked.observe(viewLifecycleOwner){
            if (it && allNotesViewModel.selectedNotes.isNotEmpty()){
                val pref = context?.getSharedPreferences("DeletedNotes", AppCompatActivity.MODE_PRIVATE)

                for ( note in allNotesViewModel.selectedNotes){
                    val editor: SharedPreferences.Editor ?= pref?.edit()
                    val noteUids = pref?.getStringSet("noteUids",HashSet())
                    val deletedNoteUids = HashSet<String>()
                    if (noteUids != null){ deletedNoteUids.addAll(noteUids)}
                    note.noteUid?.let { it1 -> deletedNoteUids.add(it1) }

                    note.noteUid?.let { it1 -> scheduleDelete(it1,note.tags,note.label,note.timeStamp) }

                    editor?.putStringSet("noteUids",deletedNoteUids)
                    editor?.apply()

                    if (note.pinned){
                        allNotesViewModel.pinnedFireNotesList.value?.remove(note)
                        cancelAlarm(note.reminderDate.toInt())
                        pinnedNoteRVAdapter?.notifyDataSetChanged()

                    }else{
                        allNotesViewModel.otherFireNotesList.value?.remove(note)
                        cancelAlarm(note.reminderDate.toInt())
                        noteRVAdapter?.notifyDataSetChanged()
                    }

                }

                allNotesViewModel.otherFireNotesList.value?.let { list ->
                    noteRVAdapter?.updateListFire(list)
                }
                allNotesViewModel.pinnedFireNotesList.value?.let { list ->
                    pinnedNoteRVAdapter?.updateListFire(list)
                }

                allNotesViewModel.selectedNotes.clear()


                allNotesViewModel.itemSelectEnabled.value = false

                Toast.makeText(context,"Notes deleted successfully",Toast.LENGTH_SHORT).show()
            }
        }


        addNoteFAB.setOnClickListener{
            val intent = Intent( context,AddEditNoteActivity::class.java)
            intent.putExtra("noteType","NewNote")
            startActivity(intent)
        }

       
        return root
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        val trashButton = menu.findItem(R.id.trash)
        trashButton.isVisible = false
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
