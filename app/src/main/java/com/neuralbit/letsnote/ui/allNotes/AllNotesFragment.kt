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

class AllNotesFragment : Fragment() , NoteClickInterface, NoteDeleteInterface {

    private val allNotesViewModel: AllNotesViewModel by activityViewModels()
    private var _binding: FragmentAllNotesBinding? = null
    val TAG = "HOMEFRAGMENT"
    lateinit var  notesRV: RecyclerView
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
        val staggeredLayoutManager = StaggeredGridLayoutManager( 2,LinearLayoutManager.VERTICAL)
        notesRV.layoutManager = staggeredLayoutManager
        val noteRVAdapter = context?.let { NoteRVAdapter(it,this,this) }

        notesRV.adapter= noteRVAdapter

        allNotesViewModel.allNotes.observe(viewLifecycleOwner,{

            noteRVAdapter?.updateList(it)

        })


        allNotesViewModel.searchQuery.observe(viewLifecycleOwner,{ s ->
            allNotesViewModel.filterList().observe(viewLifecycleOwner,{
                noteRVAdapter?.updateList(it)
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
        intent.putExtra("noteTimeStamp",note.timeStamp )
        startActivity(intent)


    }


    override fun onDeleteIconClick(note: Note) {
        allNotesViewModel.deleteNote(note)
        Toast.makeText(context,"${note.title} Deleted" , Toast.LENGTH_SHORT).show()    }
}
