package com.neuralbit.letsnote.ui.archived

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.neuralbit.letsnote.AddEditNoteActivity
import com.neuralbit.letsnote.adapters.NoteFireClick
import com.neuralbit.letsnote.adapters.NoteRVAdapter
import com.neuralbit.letsnote.databinding.FragmentArchivedNotesBinding
import com.neuralbit.letsnote.entities.NoteFire
import com.neuralbit.letsnote.ui.allNotes.AllNotesViewModel

class ArchivedFragment : Fragment() , NoteFireClick {

    private val archivedViewModel: ArchivedViewModel by activityViewModels()
    private val allNotesViewModel: AllNotesViewModel by activityViewModels()
    private var _binding: FragmentArchivedNotesBinding? = null
    private lateinit var  notesRV: RecyclerView
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

        allNotesViewModel.getAllFireNotes().observe(viewLifecycleOwner) {
            val pref = context?.getSharedPreferences("DeletedNotes", AppCompatActivity.MODE_PRIVATE)
            val deletedNotes = pref?.getStringSet("noteUids", HashSet())
            val archivedNotes = ArrayList<NoteFire>()
            for ( note in it){
                if (deletedNotes != null){
                    if (!deletedNotes.contains(note.noteUid)){
                        if (note.archived){
                            archivedNotes.add(note)
                        }
                    }
                }else{
                    if (note.archived){
                        archivedNotes.add(note)
                    }
                }
            }
            archivedViewModel.archivedFireNotes.value = archivedNotes
            noteRVAdapter?.updateListFire(archivedNotes)
        }

        archivedViewModel.searchQuery.observe(viewLifecycleOwner) {
            archivedViewModel.filterArchivedFireList().observe(viewLifecycleOwner) {
                noteRVAdapter?.updateListFire(it)
            }
        }


        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    override fun onNoteFireClick(note: NoteFire, activated : Boolean) {
        val intent = Intent( context, AddEditNoteActivity::class.java)
        intent.putExtra("noteType","Edit")
        intent.putExtra("noteTitle",note.title)
        intent.putExtra("noteDescription",note.description)
        intent.putExtra("noteUid",note.noteUid)
        intent.putExtra("timeStamp",note.timeStamp)
        intent.putExtra("labelColor",note.label)
        intent.putExtra("pinned",note.pinned)
        intent.putExtra("archieved",note.archived)
        intent.putExtra("reminder",note.reminderDate)
        val toDoItemString: String = Gson().toJson(note.todoItems)
        intent.putExtra("todoItems", toDoItemString)
        intent.putStringArrayListExtra("tagList", ArrayList(note.tags))
        startActivity(intent)
    }

    override fun onNoteFireLongClick(note: NoteFire) {
        TODO("Not yet implemented")
    }

}