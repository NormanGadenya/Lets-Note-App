package com.neuralbit.letsnote.ui.archived

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.neuralbit.letsnote.AddEditNoteActivity
import com.neuralbit.letsnote.adapters.NoteClickInterface
import com.neuralbit.letsnote.adapters.NoteFireClick
import com.neuralbit.letsnote.adapters.NoteRVAdapter
import com.neuralbit.letsnote.databinding.FragmentArchivedNotesBinding
import com.neuralbit.letsnote.entities.Note
import com.neuralbit.letsnote.entities.NoteFire
import com.neuralbit.letsnote.ui.allNotes.AllNotesViewModel

class ArchivedFragment : Fragment() , NoteClickInterface, NoteFireClick {

    private val archivedViewModel: ArchivedViewModel by activityViewModels()
    private val allNotesViewModel: AllNotesViewModel by activityViewModels()
    private var _binding: FragmentArchivedNotesBinding? = null
    lateinit var  notesRV: RecyclerView
    private val binding get() = _binding!!
    private lateinit var pinnedNotes: List<Note>
    val TAG = "HSBD"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentArchivedNotesBinding.inflate(inflater, container, false)
        val root: View = binding.root
        notesRV = binding.archivedRV
        notesRV.layoutManager = LinearLayoutManager(context)
        val noteRVAdapter = context?.let { NoteRVAdapter(it,this,this) }
        notesRV.adapter= noteRVAdapter
        noteRVAdapter?.viewModel = allNotesViewModel
        noteRVAdapter?.lifecycleScope = lifecycleScope
        noteRVAdapter?.lifecycleOwner = this

        allNotesViewModel.getAllFireNotes().observe(viewLifecycleOwner) {
            val archivedNotes = ArrayList<NoteFire>()
            for ( note in it){
                if (note.archived){
                    archivedNotes.add(note)
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

    override fun onNoteClick(note: Note) {
        val intent = Intent( context, AddEditNoteActivity::class.java)
        intent.putExtra("noteType","Edit")
        intent.putExtra("noteID",note.noteID)
        startActivity(intent)




    }

    override fun onNoteFireClick(note: NoteFire) {
        TODO("Not yet implemented")
    }

}