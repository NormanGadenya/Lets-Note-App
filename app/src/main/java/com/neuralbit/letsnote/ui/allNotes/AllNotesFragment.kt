package com.neuralbit.letsnote.ui.allNotes

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.neuralbit.letsnote.*
import com.neuralbit.letsnote.databinding.FragmentAllNotesBinding
import com.neuralbit.letsnote.entities.Note

class AllNotesFragment : Fragment() , NoteClickInterface {

    private val allNotesViewModel: AllNotesViewModel by activityViewModels()
    private var _binding: FragmentAllNotesBinding? = null
    val TAG = "HOMEFRAGMENT"
    lateinit var  notesRV: RecyclerView
    lateinit var  pinnedNotesRV: RecyclerView
    lateinit var addFAB : FloatingActionButton
    private val binding get() = _binding!!
    lateinit var pinnedNotesTV: TextView
    lateinit var otherNotesTV: TextView
    private lateinit var allNotes: List<Note>
    private lateinit var pinnedNotes: List<Note>

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
        pinnedNotesTV = binding.pinnedNotesTV
        otherNotesTV = binding.otherNotesTV
        val layoutManagerAll = StaggeredGridLayoutManager( 2,LinearLayoutManager.VERTICAL)
        val layoutManagerPinned = StaggeredGridLayoutManager( 2,LinearLayoutManager.VERTICAL)
        notesRV.layoutManager = layoutManagerAll
        pinnedNotesRV.layoutManager =layoutManagerPinned
        val noteRVAdapter = context?.let { NoteRVAdapter(it,this) }
        val pinnedNoteRVAdapter = context?.let { NoteRVAdapter(it,this) }
        notesRV.adapter= noteRVAdapter
        pinnedNotesRV.adapter = pinnedNoteRVAdapter
        //TODO introduce new pinned list fragment
        allNotesViewModel.allNotes.observe(viewLifecycleOwner) {
            allNotes = it
            noteRVAdapter?.updateList(it)
        }
        allNotesViewModel.pinnedNotes.observe(viewLifecycleOwner) {
            pinnedNoteRVAdapter?.updateList(it)
            pinnedNotes = it
            if (it.isNotEmpty()) {
                allNotesViewModel.allNotes.observe(viewLifecycleOwner) { allNotes ->
                    if (allNotes.isNotEmpty()) {
                        otherNotesTV.visibility = VISIBLE
                    } else {
                        otherNotesTV.visibility = GONE
                    }
                }

                pinnedNotesTV.visibility = VISIBLE
            } else {
                otherNotesTV.visibility = GONE
                pinnedNotesTV.visibility = GONE
            }
        }

        allNotesViewModel.searchQuery.observe(viewLifecycleOwner) { str->
            allNotesViewModel.filterPinnedList().observe(viewLifecycleOwner) {
                pinnedNoteRVAdapter?.searchString = str
                pinnedNoteRVAdapter?.updateList(it)

            }
            allNotesViewModel.filterList().observe(viewLifecycleOwner) {
                noteRVAdapter?.updateList(it)
                noteRVAdapter?.searchString = str

            }

        }


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

        intent.putExtra("noteID",note.noteID)

        startActivity(intent)


    }

}
