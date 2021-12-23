package com.neuralbit.letsnote.ui.archived

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.neuralbit.letsnote.*
import com.neuralbit.letsnote.databinding.FragmentArchivedNotesBinding
import com.neuralbit.letsnote.entities.Note
import com.neuralbit.letsnote.ui.allNotes.AllNotesViewModel

class ArchivedFragment : Fragment() , NoteClickInterface {

    private val archivedViewModel: ArchivedViewModel by activityViewModels()
    private val allNotesViewModel: AllNotesViewModel by activityViewModels()
    private var _binding: FragmentArchivedNotesBinding? = null
    lateinit var  notesRV: RecyclerView
    private val binding get() = _binding!!
    private lateinit var pinnedNotes: List<Note>


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

        archivedViewModel.archivedNotes.observe(viewLifecycleOwner,{
            noteRVAdapter?.updateList(it)

        })
        allNotesViewModel.pinnedNotes.observe(viewLifecycleOwner,{
            pinnedNotes = it
        })

        archivedViewModel.searchQuery.observe(viewLifecycleOwner,{
            archivedViewModel.filterList().observe(viewLifecycleOwner,{
                noteRVAdapter?.updateList(it)
            })
        })

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onNoteClick(note: Note) {
        val intent = Intent( context, AddEditNoteActivity::class.java)
        intent.putExtra("archivedNote",true)
        intent.putExtra("noteType","Edit")
//        intent.putExtra("noteColor",note.tagColor)
        intent.putExtra("noteTitle",note.title)
        intent.putExtra("noteDescription",note.description)
        intent.putExtra("noteID",note.noteID)
        intent.putExtra("noteTimeStamp",note.timeStamp )
        if(note in pinnedNotes ){
            intent.putExtra("pinnedNote",true)
        }
        startActivity(intent)




    }

}