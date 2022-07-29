package com.neuralbit.letsnote.ui.deletedNotes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.neuralbit.letsnote.NoteViewModel
import com.neuralbit.letsnote.adapters.NoteFireClick
import com.neuralbit.letsnote.adapters.NoteRVAdapter
import com.neuralbit.letsnote.databinding.DeletedNotesFragmentBinding
import com.neuralbit.letsnote.entities.NoteFire
import com.neuralbit.letsnote.ui.allNotes.AllNotesViewModel

class DeletedNotesFragment : Fragment() , NoteFireClick {
    private val noteViewModel: NoteViewModel by activityViewModels()
    private val allNotesViewModel: AllNotesViewModel by activityViewModels()
    private val deletedNotesViewModel: DeletedNotesViewModel by activityViewModels()
    private lateinit var deletedRV : RecyclerView
    private var _binding : DeletedNotesFragmentBinding? = null
    private val binding get() = _binding!!
    val TAG = "DELETEDNOTESFRAG"



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DeletedNotesFragmentBinding.inflate(inflater, container, false)

        deletedRV = binding.deletedRV
        val root: View = binding.root

        val noteRVAdapter = context?.let { NoteRVAdapter(it,this) }
        deletedRV.layoutManager = LinearLayoutManager(context)

        noteRVAdapter?.viewModel = allNotesViewModel
        noteRVAdapter?.lifecycleScope = lifecycleScope
        noteRVAdapter?.lifecycleOwner = this

        deletedRV.adapter= noteRVAdapter


        return root

    }


    override fun onNoteFireClick(note: NoteFire) {

    }

}