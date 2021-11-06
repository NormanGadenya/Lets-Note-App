package com.neuralbit.letsnote.ui.allNotes

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.neuralbit.letsnote.*
import com.neuralbit.letsnote.databinding.FragmentAllNotesBinding

class AllNotesFragment : Fragment() , NoteClickInterface, NoteDeleteInterface {

    private lateinit var homeViewModel: AllNotesViewModel
    private var _binding: FragmentAllNotesBinding? = null
    val TAG = "HOMEFRAGMENT"
    lateinit var  notesRV: RecyclerView
    lateinit var addFAB : FloatingActionButton
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProvider(this).get(AllNotesViewModel::class.java)

        _binding = FragmentAllNotesBinding.inflate(inflater, container, false)
        val root: View = binding.root
        addFAB = binding.FABAddNote
        notesRV = binding.notesRV
        val staggeredLayoutManager = StaggeredGridLayoutManager( 2,LinearLayoutManager.VERTICAL)
        notesRV.layoutManager = staggeredLayoutManager
        val noteRVAdapter = context?.let { NoteRVAdapter(it,this,this) }

        notesRV.adapter= noteRVAdapter

        homeViewModel.allNotes.observe(viewLifecycleOwner,{
            noteRVAdapter?.updateList(it)


        })
        addFAB.setOnClickListener{
            val intent = Intent( context,AddEditNoteActivity::class.java)
            intent.putExtra("noteType","newNote")
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
        intent.putExtra("noteTimeStamp",note.timeStamp )
        startActivity(intent)


    }

    override fun onDeleteIconClick(note: Note) {
        homeViewModel.deleteNote(note)
        Toast.makeText(context,"${note.title} Deleted" , Toast.LENGTH_SHORT).show()    }
}
