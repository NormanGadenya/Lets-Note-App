package com.neuralbit.letsnote.ui.deletedNotes

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.gms.ads.AdRequest
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.neuralbit.letsnote.R
import com.neuralbit.letsnote.databinding.DeletedNotesFragmentBinding
import com.neuralbit.letsnote.firebase.entities.NoteFire
import com.neuralbit.letsnote.receivers.DeleteReceiver
import com.neuralbit.letsnote.ui.adapters.NoteFireClick
import com.neuralbit.letsnote.ui.adapters.NoteRVAdapter
import com.neuralbit.letsnote.ui.addEditNote.AddEditNoteActivity
import com.neuralbit.letsnote.ui.addEditNote.Fingerprint
import com.neuralbit.letsnote.ui.allNotes.AllNotesViewModel
import com.neuralbit.letsnote.ui.settings.SettingsViewModel
import kotlinx.coroutines.*
import kotlin.math.floor

class DeletedNotesFragment : Fragment() , NoteFireClick {
    private var noteRVAdapter: NoteRVAdapter? = null
    private val allNotesViewModel: AllNotesViewModel by activityViewModels()
    private val deletedNotesViewModel: DeletedNotesViewModel by activityViewModels()
    private val settingsViewModel: SettingsViewModel by activityViewModels()

    private lateinit var deletedRV : RecyclerView
    private var _binding : DeletedNotesFragmentBinding? = null
    private val binding get() = _binding!!
    val TAG = "DELETEDNOTESFRAG"
    private lateinit var trashIcon : ImageView
    private lateinit var trashText : TextView
    private lateinit var parentLayout : CoordinatorLayout
    private val tempNotesToDelete = ArrayList<NoteFire>()

    @Volatile
    var restoreDeleted = false



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DeletedNotesFragmentBinding.inflate(inflater, container, false)

        deletedRV = binding.deletedRV
        val root: View = binding.root
        trashIcon = binding.trashIcon
        trashText = binding.trashText
        parentLayout = binding.coordinatorlayout
        val adView = binding.adView
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
        settingsViewModel.settingsFrag.value = false

        noteRVAdapter = context?.let { NoteRVAdapter(it,this) }
        deletedRV.layoutManager = LinearLayoutManager(context)

        noteRVAdapter?.viewModel = allNotesViewModel
        noteRVAdapter?.lifecycleScope = lifecycleScope
        noteRVAdapter?.lifecycleOwner = this

        deletedRV.adapter= noteRVAdapter
        allNotesViewModel.archiveFrag = false

        allNotesViewModel.deleteFrag.value = true
        noteRVAdapter?.deleteFrag = true
        val settingsSharedPref = context?.getSharedPreferences("Settings", AppCompatActivity.MODE_PRIVATE)
        val fontStyle = settingsSharedPref?.getString("font",null)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            noteRVAdapter?.fontStyle = fontStyle
        }
        val useLocalStorage = settingsSharedPref?.getBoolean("useLocalStorage",false)
        if (useLocalStorage != null) {
            allNotesViewModel.useLocalStorage = useLocalStorage
            deletedNotesViewModel.useLocalStorage = useLocalStorage
        }
        val staggeredLayoutManagerAll = StaggeredGridLayoutManager( 2,LinearLayoutManager.VERTICAL)
        allNotesViewModel.staggeredView.observe(viewLifecycleOwner){
            if (it){
                deletedRV.layoutManager = staggeredLayoutManagerAll
            }else{

                deletedRV.layoutManager = LinearLayoutManager(context)
            }
        }
        setHasOptionsMenu(true)
        lifecycleScope.launch {
            allNotesViewModel.getAllFireNotes().observe(viewLifecycleOwner){
                allNotesViewModel.allFireNotes.value = it
            }
        }

        allNotesViewModel.allFireNotes.observe(viewLifecycleOwner){
            val filteredNotes = HashSet<NoteFire>()
            for (n in it){
                val timeLeftMS = n.deletedDate +  6.048e+8 - System.currentTimeMillis()
                val daysLeft = floor(timeLeftMS * 1.1574E-8)

                if (n.deletedDate > 0 && daysLeft > 0){
                    if (daysLeft > 0){
                        filteredNotes.add(n)
                        Log.d(TAG, "onCreateView: $filteredNotes")
                    }else{
                        n.noteUid?.let { uid ->
                            deletedNotesViewModel.deleteNote(
                                uid,
                                n.label,
                                n.tags
                            )
                            cancelDelete(n.timeStamp.toInt())
                        }
                    }
                }
            }

            if (filteredNotes.isEmpty()){
                trashText.visibility = View.VISIBLE
                trashIcon.visibility = View.VISIBLE
            }else{
                trashText.visibility = View.GONE
                trashIcon.visibility = View.GONE
            }
            deletedNotesViewModel.deletedNotes = filteredNotes
            noteRVAdapter?.updateListFire(ArrayList(filteredNotes))

        }
        deletedNotesViewModel.clearTrash.observe(viewLifecycleOwner){

            if (it ){
                val snackbar :Snackbar
                val selectedNotesCount = deletedNotesViewModel.deletedNotes.size
                snackbar = if (selectedNotesCount > 1){
                    Snackbar.make(parentLayout,resources.getString(R.string.notes_deleted_successfully,"s"), Snackbar.LENGTH_LONG)
                }else{
                    Snackbar.make(parentLayout,resources.getString(R.string.notes_deleted_successfully,""), Snackbar.LENGTH_LONG)
                }
                snackbar.setAction("UNDO"
                ) { restoreTempNotes()}
                snackbar.show()
                GlobalScope.launch {
                    permanentlyDeleteNotes()
                }
                val notes = ArrayList(deletedNotesViewModel.deletedNotes)

                for (deletedNote in deletedNotesViewModel.deletedNotes) {

                    tempNotesToDelete.add(deletedNote)
                    notes.remove(deletedNote)

                }
                noteRVAdapter?.updateListFire(notes)
                trashText.visibility = View.VISIBLE
                trashIcon.visibility = View.VISIBLE
                allNotesViewModel.itemSelectEnabled.value = false
                deletedNotesViewModel.clearTrash.value = false
            }

        }

        deletedNotesViewModel.itemDeleteClicked.observe(viewLifecycleOwner){


            if (it && allNotesViewModel.selectedNotes.isNotEmpty()){
                val snackbar :Snackbar
                val selectedNotesCount = allNotesViewModel.selectedNotes.size
                snackbar = if (selectedNotesCount > 1){
                    Snackbar.make(parentLayout,resources.getString(R.string.notes_deleted_successfully,"s"), Snackbar.LENGTH_LONG)
                }else{
                    Snackbar.make(parentLayout,resources.getString(R.string.notes_deleted_successfully,""), Snackbar.LENGTH_LONG)
                }
                snackbar.setAction("UNDO"
                ) { restoreTempNotes()}
                snackbar.show()
                GlobalScope.launch {
                    permanentlyDeleteNotes()
                }
                val notes = ArrayList(deletedNotesViewModel.deletedNotes)

                for (deletedNote in allNotesViewModel.selectedNotes) {
                    if (deletedNote.selected){
                        tempNotesToDelete.add(deletedNote)
                        notes.remove(deletedNote)
                    }
                }
                noteRVAdapter?.updateListFire(notes)

                allNotesViewModel.itemSelectEnabled.value = false
                deletedNotesViewModel.itemDeleteClicked.value = false
            }
        }


        deletedNotesViewModel.itemRestoreClicked.observe(viewLifecycleOwner){
            if (it && allNotesViewModel.selectedNotes.isNotEmpty()){
                val selectedNotesCount = allNotesViewModel.selectedNotes.size
                val notes = ArrayList(deletedNotesViewModel.deletedNotes)
                for ( note in allNotesViewModel.selectedNotes){
                    if (note.selected){
                        val noteUpdate = HashMap<String,Any>()
                        noteUpdate["title"] = note.title
                        noteUpdate["description"] = note.description
                        noteUpdate["label"] = note.label
                        noteUpdate["pinned"] = note.pinned
                        noteUpdate["reminderDate"] = note.reminderDate
                        noteUpdate["protected"] = note.protected
                        noteUpdate["deletedDate"] = 0
                        note.noteUid?.let { it1 -> allNotesViewModel.updateFireNote(noteUpdate, it1) }
                        cancelDelete(note.timeStamp.toInt())
                        deletedNotesViewModel.deletedNotes.remove(note)
                        notes.remove(note)
                    }
                }
                noteRVAdapter?.updateListFire(notes)
                allNotesViewModel.selectedNotes.clear()
                allNotesViewModel.itemSelectEnabled.value = false
                if (selectedNotesCount == 1){
                    Toast.makeText(context,resources.getString(R.string.notes_restored_successfully,""),Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(context,resources.getString(R.string.notes_restored_successfully, "s"),Toast.LENGTH_SHORT).show()

                }
                if (deletedNotesViewModel.deletedNotes.isEmpty()){
                    trashText.visibility = View.VISIBLE
                    trashIcon.visibility = View.VISIBLE
                }else{
                    trashText.visibility = View.GONE
                    trashIcon.visibility = View.GONE
                }
            }
        }

        return root

    }

    private fun restoreTempNotes() {
        trashText.visibility = View.GONE
        trashIcon.visibility = View.GONE
        restoreDeleted = true
        tempNotesToDelete.clear()
        noteRVAdapter?.updateListFire(ArrayList(deletedNotesViewModel.deletedNotes))
    }

    private suspend fun permanentlyDeleteNotes() {
        withContext(Dispatchers.Main){
            delay(2000L)
            if (!restoreDeleted){
                for (deletedNote in tempNotesToDelete) {
                    deletedNotesViewModel.deletedNotes.remove(deletedNote)
                    deletedNote.noteUid?.let { uid ->
                        deletedNotesViewModel.deleteNote(
                            uid,
                            deletedNote.label,
                            deletedNote.tags
                        )
                        cancelDelete(deletedNote.timeStamp.toInt())
                    }
                }

            }

        }
    }

    private fun cancelDelete(timestamp : Int){
        val alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, DeleteReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, timestamp, intent, PendingIntent.FLAG_IMMUTABLE)
        alarmManager.cancel(pendingIntent)
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
            intent.putExtra("deleted",true)
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