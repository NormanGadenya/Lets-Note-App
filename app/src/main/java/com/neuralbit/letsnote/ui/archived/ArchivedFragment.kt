package com.neuralbit.letsnote.ui.archived

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.*
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
import com.google.android.gms.ads.AdRequest
import com.google.gson.Gson
import com.neuralbit.letsnote.R
import com.neuralbit.letsnote.databinding.FragmentArchivedNotesBinding
import com.neuralbit.letsnote.firebase.entities.NoteFire
import com.neuralbit.letsnote.receivers.DeleteReceiver
import com.neuralbit.letsnote.ui.adapters.NoteFireClick
import com.neuralbit.letsnote.ui.adapters.NoteRVAdapter
import com.neuralbit.letsnote.ui.addEditNote.AddEditNoteActivity
import com.neuralbit.letsnote.ui.addEditNote.Fingerprint
import com.neuralbit.letsnote.ui.allNotes.AllNotesViewModel
import com.neuralbit.letsnote.ui.settings.SettingsViewModel
import java.util.*

class ArchivedFragment : Fragment() , NoteFireClick {

    private val archivedViewModel: ArchivedViewModel by activityViewModels()
    private val allNotesViewModel: AllNotesViewModel by activityViewModels()
    private val settingsViewModel: SettingsViewModel by activityViewModels()
    private var _binding: FragmentArchivedNotesBinding? = null
    private lateinit var  notesRV: RecyclerView
    private lateinit var archiveIcon : ImageView
    private lateinit var archiveText : TextView
    private val binding get() = _binding!!
    val TAG = "ArchivedFragment"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentArchivedNotesBinding.inflate(inflater, container, false)
        val root: View = binding.root
        notesRV = binding.archivedRV
        notesRV.layoutManager = LinearLayoutManager(context)
        val noteRVAdapter = context?.let { NoteRVAdapter(it,this) }
        notesRV.adapter= noteRVAdapter
        noteRVAdapter?.viewModel = allNotesViewModel
        noteRVAdapter?.lifecycleScope = lifecycleScope
        noteRVAdapter?.lifecycleOwner = this
        settingsViewModel.settingsFrag.value = false
        archiveIcon = binding.archivedIcon
        archiveText = binding.archivedText
        allNotesViewModel.archiveFrag = true
        allNotesViewModel.deleteFrag.value = false
        val adView = binding.adView
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
        val settingsSharedPref = context?.getSharedPreferences("Settings", AppCompatActivity.MODE_PRIVATE)
        val fontStyle = settingsSharedPref?.getString("font",null)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            noteRVAdapter?.fontStyle = fontStyle
        }
        val useLocalStorage = settingsSharedPref?.getBoolean("useLocalStorage",false)
        if (useLocalStorage != null) {
            allNotesViewModel.useLocalStorage = useLocalStorage
        }
        val staggeredLayoutManagerAll = StaggeredGridLayoutManager( 2,LinearLayoutManager.VERTICAL)
        allNotesViewModel.staggeredView.observe(viewLifecycleOwner){
            if (it){
                notesRV.layoutManager = staggeredLayoutManagerAll
            }else{

                notesRV.layoutManager = LinearLayoutManager(context)
            }
        }
        setHasOptionsMenu(true)

        allNotesViewModel.allFireNotes.observe(viewLifecycleOwner) {
            val archivedNotes = ArrayList<NoteFire>()

            for ( note in it){
                if (note.deletedDate == (0).toLong() && note.archived) {
                    archivedNotes.add(note)
                }
            }
            if (archivedNotes.isEmpty()){
                archiveIcon.visibility = View.VISIBLE
                archiveText.visibility = View.VISIBLE
            }else{
                archiveIcon.visibility = View.GONE
                archiveText.visibility = View.GONE
            }
            archivedViewModel.archivedFireNotes.value = archivedNotes
            noteRVAdapter?.updateListFire(archivedNotes)
        }

        archivedViewModel.itemDeleteClicked.observe(viewLifecycleOwner){
            if (it && allNotesViewModel.selectedNotes.isNotEmpty()){
                val selectedNotesCount = allNotesViewModel.selectedNotes.size
                val settings = context?.getSharedPreferences("Settings", AppCompatActivity.MODE_PRIVATE)
                val emptyTrashImmediately = settings?.getBoolean("EmptyTrashImmediately",false)

                for ( note in allNotesViewModel.selectedNotes){

                    if (emptyTrashImmediately != true){
                        val noteUpdate = HashMap<String,Any>()
                        noteUpdate["deletedDate"] = System.currentTimeMillis()
                        note.noteUid?.let { it1 -> allNotesViewModel.updateFireNote(noteUpdate, it1) }

                        note.noteUid?.let { it1 -> scheduleDelete(it1,note.tags,note.label,note.timeStamp) }

                    }else{
                        allNotesViewModel.notesToDelete.value =  note
                    }

                    archivedViewModel.archivedFireNotes.value?.remove(note)

                }
                noteRVAdapter?.notifyDataSetChanged()

                if (emptyTrashImmediately != true){
                    allNotesViewModel.selectedNotes.clear()
                }


                allNotesViewModel.itemSelectEnabled.value = false
                allNotesViewModel.itemDeleteClicked.value = false
                if (selectedNotesCount == 1){
                    Toast.makeText(context,resources.getString(R.string.notes_deleted_successfully),Toast.LENGTH_SHORT).show()

                }else{
                    Toast.makeText(context,resources.getString(R.string.notes_deleted_successfully,"s"),Toast.LENGTH_SHORT).show()
                }

            }
        }

        archivedViewModel.itemRestoreClicked.observe(viewLifecycleOwner){
            if (it && allNotesViewModel.selectedNotes.isNotEmpty()){

                val selectedNotesCount = allNotesViewModel.selectedNotes.size
                for ( note in allNotesViewModel.selectedNotes){
                    archivedViewModel.notesToRestore.value = note
                    archivedViewModel.archivedFireNotes.value?.remove(note)

                }
                noteRVAdapter?.notifyDataSetChanged()



                allNotesViewModel.itemSelectEnabled.value = false
                allNotesViewModel.itemDeleteClicked.value = false
                allNotesViewModel.selectedNotes.clear()
                if (selectedNotesCount == 1){
                    Toast.makeText(context,resources.getString(R.string.notes_restored_successfully),Toast.LENGTH_SHORT).show()

                }else{
                    Toast.makeText(context,resources.getString(R.string.notes_restored_successfully,"s"),Toast.LENGTH_SHORT).show()

                }
            }

        }

        archivedViewModel.searchQuery.observe(viewLifecycleOwner) {
            archivedViewModel.filterArchivedFireList().observe(viewLifecycleOwner) {
                noteRVAdapter?.updateListFire(it)
            }
        }




        return root
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        val trashButton = menu.findItem(R.id.trash)
        trashButton.isVisible = false
        super.onCreateOptionsMenu(menu, inflater)
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