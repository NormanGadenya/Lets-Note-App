package com.neuralbit.letsnote.ui.allNotes

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.neuralbit.letsnote.*
import com.neuralbit.letsnote.databinding.FragmentAllNotesBinding

class AllNotesFragment : Fragment() , NoteClickInterface {

    private val allNotesViewModel: AllNotesViewModel by activityViewModels()
    private var _binding: FragmentAllNotesBinding? = null
    val TAG = "HOMEFRAGMENT"
    lateinit var  notesRV: RecyclerView
    lateinit var  pinnedNotesRV: RecyclerView
    lateinit var addFAB : FloatingActionButton
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAllNotesBinding.inflate(inflater, container, false)

        val root: View = binding.root
        addFAB = binding.FABAddNote
        notesRV = binding.notesRV
        pinnedNotesRV = binding.pinnedNotesRV
        val layoutManagerAll = StaggeredGridLayoutManager( 2,LinearLayoutManager.VERTICAL)
        val layoutManagerPinned = StaggeredGridLayoutManager( 2,LinearLayoutManager.VERTICAL)
        notesRV.layoutManager = layoutManagerAll
        pinnedNotesRV.layoutManager =layoutManagerPinned
        val noteRVAdapter = context?.let { NoteRVAdapter(it,this) }
        val pinnedNoteRVAdapter = context?.let { NoteRVAdapter(it,this) }
        notesRV.adapter= noteRVAdapter
        pinnedNotesRV.adapter = pinnedNoteRVAdapter
        //TODO introduce new pinned list fragment
        allNotesViewModel.allNotes.observe(viewLifecycleOwner,{

            noteRVAdapter?.updateList(it)

        })
        allNotesViewModel.pinnedNotes.observe(viewLifecycleOwner,{
            pinnedNoteRVAdapter?.updateList(it)
        })

        allNotesViewModel.searchQuery.observe(viewLifecycleOwner,{ s ->
            allNotesViewModel.filterList().observe(viewLifecycleOwner,{
                noteRVAdapter?.updateList(it)

            })
            allNotesViewModel.filterPinnedList().observe(viewLifecycleOwner,{
                pinnedNoteRVAdapter?.updateList(it)
            })
        })

        
        addFAB.setOnClickListener{
            val intent = Intent( context,AddEditNoteActivity::class.java)
            intent.putExtra("noteType","NewNote")
            startActivity(intent)
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
        intent.putExtra("noteTitle",note.title)
        intent.putExtra("noteDescription",note.description)
        intent.putExtra("noteID",note.id)
        intent.putExtra("noteColor",note.noteColor)
        intent.putExtra("noteTimeStamp",note.timeStamp )
        startActivity(intent)


    }

}
