package com.neuralbit.letsnote.ui.allNotes

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.neuralbit.letsnote.*
import com.neuralbit.letsnote.adapters.NoteClickInterface
import com.neuralbit.letsnote.adapters.NoteRVAdapter
import com.neuralbit.letsnote.databinding.FragmentAllNotesBinding
import com.neuralbit.letsnote.entities.ArchivedNote
import com.neuralbit.letsnote.entities.Note
import com.neuralbit.letsnote.entities.NoteTagCrossRef
import com.neuralbit.letsnote.entities.PinnedNote
import com.neuralbit.letsnote.utilities.Common
import kotlinx.coroutines.launch

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
        noteRVAdapter?.viewModel = allNotesViewModel
        noteRVAdapter?.lifecycleScope = lifecycleScope
        noteRVAdapter?.lifecycleOwner = this
        pinnedNoteRVAdapter?.viewModel = allNotesViewModel
        pinnedNoteRVAdapter?.lifecycleScope = lifecycleScope
        pinnedNoteRVAdapter?.lifecycleOwner = this

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

        val touchHelperPinned = ItemTouchHelper(object  : ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.RIGHT){
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {

                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val note = pinnedNotes[viewHolder.adapterPosition]
                allNotesViewModel.removeArchive(ArchivedNote(note.noteID))

                allNotesViewModel.removePin(PinnedNote(note.noteID))
                allNotesViewModel.deleteLabel(note.noteID)
                allNotesViewModel.deleteReminder(note.noteID)
                lifecycleScope.launch {
                    val allTags = allNotesViewModel.getTagsWithNote(note.noteID)

                    for(tag in allTags.first().tags){
                        allNotesViewModel.deleteNoteTagCrossRef(NoteTagCrossRef( note.noteID,tag.tagTitle))
                    }
                    allNotesViewModel.deleteNote(note)
                    noteRVAdapter?.notifyItemRemoved(viewHolder.adapterPosition)
                }



            }

            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                super.onSelectedChanged(viewHolder, actionState)
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE){
                    val iView = viewHolder?.itemView as CardView
                    iView.setCardBackgroundColor(resources.getColor(R.color.Red))

                }
            }

            override fun clearView(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ) {
                super.clearView(recyclerView, viewHolder)
                val iView = viewHolder.itemView as CardView

                iView.setCardBackgroundColor(resources.getColor(R.color.def_Card_Color))
                try{
                    val note = pinnedNotes[viewHolder.adapterPosition]
                    val cm = Common()
                    allNotesViewModel.getNoteLabel(note.noteID).observe(viewLifecycleOwner){
                        if(it!=null) {
                            iView.setCardBackgroundColor(resources.getColor(cm.getLabelColor(it.labelID)))
                        }else{
                            iView.setCardBackgroundColor(resources.getColor(R.color.def_Card_Color))

                        }
                    }
                }catch (e : Exception){
                    e.printStackTrace()
                }


//                viewHolder.itemView.setBackgroundColor(Color.TRANSPARENT)
            }
        })
        val touchHelperOther = ItemTouchHelper(object  : ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.RIGHT){
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {

                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val note = allNotes[viewHolder.adapterPosition]
                allNotesViewModel.removeArchive(ArchivedNote(note.noteID))

                allNotesViewModel.removePin(PinnedNote(note.noteID))

                allNotesViewModel.deleteLabel(note.noteID)

                allNotesViewModel.deleteReminder(note.noteID)
                lifecycleScope.launch {
                    val allTags = allNotesViewModel.getTagsWithNote(note.noteID)

                    for(tag in allTags.first().tags){
                        allNotesViewModel.deleteNoteTagCrossRef(NoteTagCrossRef( note.noteID,tag.tagTitle))
                    }
                    allNotesViewModel.deleteNote(note)
                    noteRVAdapter?.notifyItemRemoved(viewHolder.adapterPosition)
                }



            }

            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                super.onSelectedChanged(viewHolder, actionState)
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE){
                    val iView = viewHolder?.itemView as CardView
                    iView.setCardBackgroundColor(resources.getColor(R.color.Red))

                }
            }

            override fun clearView(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ) {
                super.clearView(recyclerView, viewHolder)
                val iView = viewHolder.itemView as CardView

                iView.setCardBackgroundColor(resources.getColor(R.color.def_Card_Color))
                try{
                    val note = allNotes[viewHolder.adapterPosition]
                    val cm = Common()
                    allNotesViewModel.getNoteLabel(note.noteID).observe(viewLifecycleOwner){
                        if(it!=null) {
                            iView.setCardBackgroundColor(resources.getColor(cm.getLabelColor(it.labelID)))
                        }else{
                            iView.setCardBackgroundColor(resources.getColor(R.color.def_Card_Color))

                        }
                    }
                }catch (e : Exception){
                    e.printStackTrace()
                }


//                viewHolder.itemView.setBackgroundColor(Color.TRANSPARENT)
            }
        })
        touchHelperPinned.attachToRecyclerView(pinnedNotesRV)
        touchHelperOther.attachToRecyclerView(notesRV)


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
