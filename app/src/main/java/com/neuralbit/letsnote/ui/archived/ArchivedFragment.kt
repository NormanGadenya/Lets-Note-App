package com.neuralbit.letsnote.ui.archived

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.neuralbit.letsnote.*
import com.neuralbit.letsnote.databinding.FragmentArchivedNotesBinding

class ArchivedFragment : Fragment() , NoteClickInterface, NoteDeleteInterface {

    private lateinit var archivedViewModel: ArchivedViewModel
    private var _binding: FragmentArchivedNotesBinding? = null
    lateinit var  notesRV: RecyclerView
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        archivedViewModel =
            ViewModelProvider(this).get(ArchivedViewModel::class.java)

        _binding = FragmentArchivedNotesBinding.inflate(inflater, container, false)
        val root: View = binding.root
        notesRV = binding.archivedRV
        notesRV.layoutManager = LinearLayoutManager(context)
        val noteRVAdapter = context?.let { NoteRVAdapter(it,this,this) }
        notesRV.adapter= noteRVAdapter

        archivedViewModel.archivedNotes.observe(viewLifecycleOwner,{
            noteRVAdapter?.updateList(it)

        })
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
        archivedViewModel.deleteNote(note)
        Toast.makeText(context,"${note.title} Deleted" , Toast.LENGTH_SHORT).show()    }
}