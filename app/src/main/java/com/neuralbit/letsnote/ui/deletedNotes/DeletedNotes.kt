package com.neuralbit.letsnote.ui.deletedNotes

import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.neuralbit.letsnote.AddEditNoteActivity
import com.neuralbit.letsnote.NoteViewModel
import com.neuralbit.letsnote.R
import com.neuralbit.letsnote.adapters.NoteClickInterface
import com.neuralbit.letsnote.adapters.NoteRVAdapter
import com.neuralbit.letsnote.databinding.DeletedNotesFragmentBinding
import com.neuralbit.letsnote.databinding.FragmentAllNotesBinding
import com.neuralbit.letsnote.entities.Note
import com.neuralbit.letsnote.ui.allNotes.AllNotesViewModel

class DeletedNotes : Fragment() , NoteClickInterface {
    private val noteViewModel: NoteViewModel by activityViewModels()
    private val allNotesViewModel: AllNotesViewModel by activityViewModels()
    private val deletedNotesViewModel: DeletedNotesViewModel by activityViewModels()
    private lateinit var deletedRV : RecyclerView
    private var _binding : DeletedNotesFragmentBinding? = null
    private val binding get() = _binding!!
    val TAG = "DELETEDNOTESFRAG"
    companion object {
        fun newInstance() = DeletedNotes()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DeletedNotesFragmentBinding.inflate(inflater, container, false)

        deletedRV = binding.deletedRV
        val root: View = binding.root

        val noteRVAdapter = context?.let { NoteRVAdapter(it,this) }
        deletedRV.layoutManager = LinearLayoutManager(context)

        noteRVAdapter?.viewModel = allNotesViewModel
        noteRVAdapter?.lifecycleScope = lifecycleScope
        noteRVAdapter?.lifecycleOwner = this
        noteViewModel.deletedNotes.observe(viewLifecycleOwner) {
            Log.d(TAG, "onCreateView: $it")
            noteRVAdapter?.updateList(it)
        }
        deletedRV.adapter= noteRVAdapter


        deletedNotesViewModel.searchQuery.observe(viewLifecycleOwner) {
            deletedNotesViewModel.filterList().observe(viewLifecycleOwner) {
                noteRVAdapter?.updateList(it)
            }
        }
        return root

    }



    override fun onNoteClick(note: Note) {
        val intent = Intent( context, AddEditNoteActivity::class.java)
        intent.putExtra("noteType","Edit")
        intent.putExtra("noteID",note.noteID)
        startActivity(intent)
    }

}