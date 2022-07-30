package com.neuralbit.letsnote.ui.allNotes

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.neuralbit.letsnote.AddEditNoteActivity
import com.neuralbit.letsnote.R
import com.neuralbit.letsnote.adapters.NoteFireClick
import com.neuralbit.letsnote.adapters.NoteRVAdapter
import com.neuralbit.letsnote.databinding.FragmentAllNotesBinding
import com.neuralbit.letsnote.entities.NoteFire
import java.util.*

class AllNotesFragment : Fragment() , NoteFireClick {

    private val allNotesViewModel: AllNotesViewModel by activityViewModels()
    private var _binding: FragmentAllNotesBinding? = null
    val TAG = "HOMEFRAGMENT"
    private lateinit var  notesRV: RecyclerView
    private lateinit var  pinnedNotesRV: RecyclerView
    private lateinit var addNoteFAB : FloatingActionButton
    private val binding get() = _binding!!
    private lateinit var pinnedNotesTV: TextView
    private lateinit var otherNotesTV: TextView
    private val rotateOpen: Animation by lazy { AnimationUtils.loadAnimation(context,R.anim.rotate_open_anim)}
    private val rotateClose: Animation by lazy { AnimationUtils.loadAnimation(context,R.anim.rotate_close_anim)}
    private val fromBottom: Animation by lazy { AnimationUtils.loadAnimation(context,R.anim.from_bottom_anim)}
    private val toBottom: Animation by lazy { AnimationUtils.loadAnimation(context,R.anim.to_bottom)}
    private var clicked = false
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAllNotesBinding.inflate(inflater, container, false)

        val root: View = binding.root
        addNoteFAB = binding.FABAddNote
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
        noteRVAdapter?.viewModel = allNotesViewModel
        noteRVAdapter?.lifecycleScope = lifecycleScope
        noteRVAdapter?.lifecycleOwner = this
        pinnedNoteRVAdapter?.viewModel = allNotesViewModel
        pinnedNoteRVAdapter?.lifecycleScope = lifecycleScope
        pinnedNoteRVAdapter?.lifecycleOwner = this

        allNotesViewModel.getAllFireNotes().observe(viewLifecycleOwner){

            val pinnedNotes = ArrayList<NoteFire>()
            val otherNotes = ArrayList<NoteFire>()
            for (note in it) {
                val pref = context?.getSharedPreferences("DeletedNotes", AppCompatActivity.MODE_PRIVATE)
                val deletedNotes = pref?.getStringSet("noteUids", HashSet())
                if (deletedNotes != null){
                    if (!deletedNotes.contains(note.noteUid)){
                        if (!note.archived){
                            if (note.pinned){
                                pinnedNotes.add(note)
                            }else{
                                otherNotes.add(note)
                            }
                        }
                    }
                }else{
                    if (!note.archived){
                        if (note.pinned){
                            pinnedNotes.add(note)
                        }else{
                            otherNotes.add(note)
                        }
                    }
                }
            }

            allNotesViewModel.otherFireNotesList.value = otherNotes
            allNotesViewModel.pinnedFireNotesList.value = pinnedNotes
            if (pinnedNotes.isNotEmpty()){
                otherNotesTV.visibility = VISIBLE
                pinnedNotesTV.visibility = VISIBLE
                pinnedNotesRV.visibility = VISIBLE
                pinnedNoteRVAdapter?.updateListFire(pinnedNotes)

            }else{
                otherNotesTV.visibility = GONE
                pinnedNotesTV.visibility = GONE
                pinnedNotesRV.visibility = GONE
            }
            noteRVAdapter?.updateListFire(otherNotes)

        }


        allNotesViewModel.searchQuery.observe(viewLifecycleOwner) { str->
            allNotesViewModel.filterPinnedFireList().observe(viewLifecycleOwner) {
                pinnedNoteRVAdapter?.searchString = str
                pinnedNoteRVAdapter?.updateListFire(it)

            }
            allNotesViewModel.filterOtherFireList().observe(viewLifecycleOwner) {
                noteRVAdapter?.updateListFire(it)
                noteRVAdapter?.searchString = str
            }

        }


        addNoteFAB.setOnClickListener{
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


    override fun onNoteFireClick(note: NoteFire) {
        val intent = Intent( context, AddEditNoteActivity::class.java)
        intent.putExtra("noteType","Edit")
        intent.putExtra("noteTitle",note.title)
        intent.putExtra("noteDescription",note.description)
        intent.putExtra("noteUid",note.noteUid)
        intent.putExtra("timeStamp",note.timeStamp)
        intent.putExtra("labelColor",note.label)
        intent.putExtra("pinned",note.pinned)
        intent.putExtra("archieved",note.archived)
        val c = Calendar.getInstance()
        if (c.timeInMillis < note.reminderDate){
            intent.putExtra("reminder",note.reminderDate)
        }
        val toDoItemString: String = Gson().toJson(note.todoItems)
        intent.putExtra("todoItems", toDoItemString)
        intent.putStringArrayListExtra("tagList", ArrayList(note.tags))
        startActivity(intent)
    }

}
