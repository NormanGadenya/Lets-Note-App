package com.neuralbit.letsnote.ui.deletedNotes

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
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
import com.google.gson.Gson
import com.neuralbit.letsnote.AddEditNoteActivity
import com.neuralbit.letsnote.Fingerprint
import com.neuralbit.letsnote.Services.DeleteReceiver
import com.neuralbit.letsnote.adapters.NoteFireClick
import com.neuralbit.letsnote.adapters.NoteRVAdapter
import com.neuralbit.letsnote.databinding.DeletedNotesFragmentBinding
import com.neuralbit.letsnote.entities.NoteFire
import com.neuralbit.letsnote.ui.allNotes.AllNotesViewModel
import com.neuralbit.letsnote.ui.settings.SettingsViewModel

class DeletedNotesFragment : Fragment() , NoteFireClick {
    private val allNotesViewModel: AllNotesViewModel by activityViewModels()
    private val deletedNotesViewModel: DeletedNotesViewModel by activityViewModels()
    private val settingsViewModel: SettingsViewModel by activityViewModels()

    private lateinit var deletedRV : RecyclerView
    private var _binding : DeletedNotesFragmentBinding? = null
    private val binding get() = _binding!!
    val TAG = "DELETEDNOTESFRAG"
    private lateinit var trashIcon : ImageView
    private lateinit var trashText : TextView



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DeletedNotesFragmentBinding.inflate(inflater, container, false)

        deletedRV = binding.deletedRV
        val root: View = binding.root
        trashIcon = binding.trashIcon
        trashText = binding.trashText
        settingsViewModel.settingsFrag.value = false

        val noteRVAdapter = context?.let { NoteRVAdapter(it,this) }
        deletedRV.layoutManager = LinearLayoutManager(context)

        noteRVAdapter?.viewModel = allNotesViewModel
        noteRVAdapter?.lifecycleScope = lifecycleScope
        noteRVAdapter?.lifecycleOwner = this

        deletedRV.adapter= noteRVAdapter
        allNotesViewModel.deleteFrag.value = true
        noteRVAdapter?.deleteFrag = true
        val pref = context?.getSharedPreferences("DeletedNotes", AppCompatActivity.MODE_PRIVATE)
        val settingsSharedPref = context?.getSharedPreferences("Settings", AppCompatActivity.MODE_PRIVATE)
        val fontStyle = settingsSharedPref?.getString("font",null)
        noteRVAdapter?.fontStyle = fontStyle
        val staggeredLayoutManagerAll = StaggeredGridLayoutManager( 2,LinearLayoutManager.VERTICAL)
        allNotesViewModel.staggeredView.observe(viewLifecycleOwner){
            if (it){
                deletedRV.layoutManager = staggeredLayoutManagerAll
            }else{

                deletedRV.layoutManager = LinearLayoutManager(context)
            }
        }
        allNotesViewModel.allFireNotes.observe(viewLifecycleOwner){
            val deletedNotes = pref?.getStringSet("noteUids", HashSet())

            val filteredNotes = HashSet<NoteFire>()
            for (n in it){
                if (deletedNotes != null){
                    for ( dNoteUid in deletedNotes){
                        if (dNoteUid == n.noteUid){
                            filteredNotes.add(n)
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
                for (deletedNote in deletedNotesViewModel.deletedNotes) {
                    deletedNote.noteUid?.let { uid -> deletedNotesViewModel.deleteNote(uid,deletedNote.label,deletedNote.tags)
                        cancelDelete(deletedNote.timeStamp.toInt())
                    }
                }
                val editor: SharedPreferences.Editor ?= pref?.edit()
                editor?.clear()
                editor?.apply()

                Toast.makeText(context,"Trash cleared successfully",Toast.LENGTH_SHORT).show()
            }
        }

        deletedNotesViewModel.itemDeleteClicked.observe(viewLifecycleOwner){
            if (it && allNotesViewModel.selectedNotes.isNotEmpty()){
                Log.d(TAG, "onCreateView: ${allNotesViewModel.selectedNotes}")

                val noteUids = pref?.getStringSet("noteUids", HashSet())
                val deletedNoteUids = HashSet<String>()
                if (noteUids != null){
                    deletedNoteUids.addAll(noteUids)
                }
                val notes = ArrayList(deletedNotesViewModel.deletedNotes)
                for ( deletedNote in allNotesViewModel.selectedNotes){
                    if (deletedNote.selected){
                        val editor: SharedPreferences.Editor ?= pref?.edit()
                        deletedNote.noteUid?.let { it1 -> deletedNoteUids.remove(it1) }
                        editor?.putStringSet("noteUids",deletedNoteUids)
                        editor?.remove(deletedNote.noteUid)
                        editor?.apply()
                        deletedNotesViewModel.deletedNotes.remove(deletedNote)
                        notes.remove(deletedNote)
                        deletedNote.noteUid?.let { uid -> deletedNotesViewModel.deleteNote(uid,deletedNote.label,deletedNote.tags)
                            cancelDelete(deletedNote.timeStamp.toInt())
                        }
                    }
                }
                noteRVAdapter?.updateListFire(notes)
                allNotesViewModel.selectedNotes.clear()
                allNotesViewModel.itemSelectEnabled.value = false

                Toast.makeText(context,"Notes deleted successfully",Toast.LENGTH_SHORT).show()
            }
        }


        deletedNotesViewModel.itemRestoreClicked.observe(viewLifecycleOwner){
            if (it && allNotesViewModel.selectedNotes.isNotEmpty()){
                Log.d(TAG, "onCreateView: ${allNotesViewModel.selectedNotes}")

                val noteUids = pref?.getStringSet("noteUids", HashSet())
                val deletedNoteUids = HashSet<String>()
                if (noteUids != null){
                    deletedNoteUids.addAll(noteUids)
                }
                val notes = ArrayList(deletedNotesViewModel.deletedNotes)
                for ( note in allNotesViewModel.selectedNotes){
                    if (note.selected){
                        val editor: SharedPreferences.Editor ?= pref?.edit()
                        note.noteUid?.let { it1 -> deletedNoteUids.remove(it1) }
                        cancelDelete(note.timeStamp.toInt())
                        editor?.putStringSet("noteUids",deletedNoteUids)
                        editor?.remove(note.noteUid)
                        editor?.apply()
                        deletedNotesViewModel.deletedNotes.remove(note)
                        notes.remove(note)
                    }
                }
                noteRVAdapter?.updateListFire(notes)
                allNotesViewModel.selectedNotes.clear()
                allNotesViewModel.itemSelectEnabled.value = false

                Toast.makeText(context,"Notes restored successfully",Toast.LENGTH_SHORT).show()
            }
        }

        return root

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