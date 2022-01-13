package com.neuralbit.letsnote.ui.allNotes

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
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
import com.neuralbit.letsnote.entities.*
import com.neuralbit.letsnote.utilities.Common
import kotlinx.coroutines.launch

class AllNotesFragment : Fragment() , NoteClickInterface {

    private val allNotesViewModel: AllNotesViewModel by activityViewModels()
    private var _binding: FragmentAllNotesBinding? = null
    val TAG = "HOMEFRAGMENT"
    lateinit var  notesRV: RecyclerView
    lateinit var  pinnedNotesRV: RecyclerView
    lateinit var addNoteFAB : FloatingActionButton
    lateinit var noteTypeFAB : FloatingActionButton
    lateinit var addClistFAB : FloatingActionButton
    private val binding get() = _binding!!
    lateinit var pinnedNotesTV: TextView
    lateinit var otherNotesTV: TextView
    private lateinit var allNotes: List<Note>
    private lateinit var pinnedNotes: List<Note>
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
                when (direction) {
                    ItemTouchHelper.RIGHT -> {
                        val alertDialog: AlertDialog? = this.let {
                            val builder = AlertDialog.Builder(context)
                            builder.apply {
                                setPositiveButton("ok"
                                ) { _, _ ->
                                    deletePinnedNote(viewHolder, noteRVAdapter)

                                    Toast.makeText(context, "Note Deleted", Toast.LENGTH_SHORT).show()


                                }
                                setNegativeButton("cancel"
                                ) { _, _ ->
                                    pinnedNoteRVAdapter?.updateList(pinnedNotes)
                                }
                                if ((note.title?.isNotEmpty())==true){
                                    setTitle("Delete ${note.title}")
                                }
                            }
                            builder.create()
                        }
                        alertDialog?.show() }
                    ItemTouchHelper.LEFT -> {
                        val alertDialog: AlertDialog? = this.let {
                            val builder = AlertDialog.Builder(context)
                            builder.apply {
                                setPositiveButton("ok"
                                ) { _, _ ->
                                    archiveNote(viewHolder, noteRVAdapter)

                                    Toast.makeText(context, "Note Archived", Toast.LENGTH_SHORT).show()


                                }
                                setNegativeButton("cancel"
                                ) { _, _ ->
                                    noteRVAdapter?.updateList(allNotes)
                                }
                                if ((note.title?.isNotEmpty())==true){
                                    setTitle("Archive ${note.title} ?")
                                }else{
                                    setTitle("Archive note ?")
                                }
                            }
                            builder.create()
                        }
                        alertDialog?.show()
                    }
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
                            iView.setCardBackgroundColor(it.labelID)
                        }else{
                            iView.setCardBackgroundColor(resources.getColor(R.color.def_Card_Color))

                        }
                    }
                }catch (e : Exception){
                    e.printStackTrace()
                }


            }
        })
        val touchHelperOther = ItemTouchHelper(object  : ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT){
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {

                return true
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
                val iView = viewHolder.itemView as CardView
                when{
                    dX > 0 -> iView.setCardBackgroundColor(resources.getColor(R.color.Red))
                    dX.toInt() == 0 -> {
                        try{
                            val note = allNotes[viewHolder.adapterPosition]
                            allNotesViewModel.getNoteLabel(note.noteID).observe(viewLifecycleOwner){
                                if(it!=null) {
                                    iView.setCardBackgroundColor(it.labelID)
                                }else{
                                    iView.setCardBackgroundColor(resources.getColor(R.color.def_Card_Color))
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                        iView.outlineSpotShadowColor = resources.getColor(R.color.def_Card_Color)
                                    }

                                }
                            }
                        }catch (e : Exception){
                            e.printStackTrace()
                        }

                    }
                    dX < 0 -> iView.setCardBackgroundColor(resources.getColor(R.color.teal_200))

                }

            }
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val note = allNotes[viewHolder.adapterPosition]

                when (direction) {
                    ItemTouchHelper.RIGHT -> {
                        val alertDialog: AlertDialog? = this.let {
                            val builder = AlertDialog.Builder(context)
                            builder.apply {
                                setPositiveButton("ok"
                                ) { _, _ ->
                                    deleteOtherNotes(viewHolder, noteRVAdapter)

                                    Toast.makeText(context, "Note Deleted", Toast.LENGTH_SHORT).show()


                                }
                                setNegativeButton("cancel"
                                ) { _, _ ->
                                    noteRVAdapter?.updateList(allNotes)
                                }
                                if ((note.title?.isNotEmpty())==true){
                                    setTitle("Delete ${note.title} ?")
                                }else{
                                    setTitle("Delete ?")
                                }
                            }
                            builder.create()
                        }
                        alertDialog?.show() }
                    ItemTouchHelper.LEFT -> {
                        val alertDialog: AlertDialog? = this.let {
                            val builder = AlertDialog.Builder(context)
                            builder.apply {
                                setPositiveButton("ok"
                                ) { _, _ ->
                                    archiveNote(viewHolder, noteRVAdapter)

                                    Toast.makeText(context, "Note Archived", Toast.LENGTH_SHORT).show()


                                }
                                setNegativeButton("cancel"
                                ) { _, _ ->
                                    noteRVAdapter?.updateList(allNotes)
                                }
                                if ((note.title?.isNotEmpty())==true){
                                    setTitle("Archive ${note.title} ?")
                                }else{
                                    setTitle("Archive ?")

                                }
                            }
                            builder.create()
                        }
                        alertDialog?.show()
                    }
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

                try{
                    val note = allNotes[viewHolder.adapterPosition]
                    allNotesViewModel.getNoteLabel(note.noteID).observe(viewLifecycleOwner){
                        if(it!=null) {
                            iView.setCardBackgroundColor(it.labelID)
                        }else{
                            iView.setCardBackgroundColor(resources.getColor(R.color.def_Card_Color))
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                iView.outlineSpotShadowColor = resources.getColor(R.color.def_Card_Color)
                            }

                        }
                    }
                }catch (e : Exception){
                    e.printStackTrace()
                }


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


        addNoteFAB.setOnClickListener{
            val intent = Intent( context,AddEditNoteActivity::class.java)
            intent.putExtra("noteType","NewNote")
            startActivity(intent)
        }

       
        return root
    }


    private fun archiveNote(viewHolder: RecyclerView.ViewHolder, noteRVAdapter: NoteRVAdapter?) {
        val note = allNotes[viewHolder.adapterPosition]
        allNotesViewModel.archiveNote(ArchivedNote(note.noteID))
        noteRVAdapter?.notifyItemRemoved(viewHolder.adapterPosition)

    }

    private fun deleteOtherNotes(
        viewHolder: RecyclerView.ViewHolder,
        noteRVAdapter: NoteRVAdapter?
    ) {
        val note = allNotes[viewHolder.adapterPosition]
        allNotesViewModel.removeArchive(ArchivedNote(note.noteID))

        allNotesViewModel.removePin(PinnedNote(note.noteID))

        allNotesViewModel.deleteNoteLabel(note.noteID)

        allNotesViewModel.deleteReminder(note.noteID)
        lifecycleScope.launch {
            allNotesViewModel.insertDeleted(DeletedNote(note.noteID))

            val allTags = allNotesViewModel.getTagsWithNote(note.noteID)

            for (tag in allTags.first().tags) {
                allNotesViewModel.deleteNoteTagCrossRef(NoteTagCrossRef(note.noteID, tag.tagTitle))
            }
//            allNotesViewModel.deleteNote(note)
            noteRVAdapter?.notifyItemRemoved(viewHolder.adapterPosition)
        }
    }

    private fun deletePinnedNote(
        viewHolder: RecyclerView.ViewHolder,
        noteRVAdapter: NoteRVAdapter?
    ) {
        val note = pinnedNotes[viewHolder.adapterPosition]
        allNotesViewModel.removeArchive(ArchivedNote(note.noteID))

        allNotesViewModel.removePin(PinnedNote(note.noteID))
        allNotesViewModel.deleteNoteLabel(note.noteID)
        allNotesViewModel.deleteReminder(note.noteID)
        lifecycleScope.launch {
            allNotesViewModel.insertDeleted(DeletedNote(note.noteID))

            val allTags = allNotesViewModel.getTagsWithNote(note.noteID)

            for (tag in allTags.first().tags) {
                allNotesViewModel.deleteNoteTagCrossRef(NoteTagCrossRef(note.noteID, tag.tagTitle))
            }
//            allNotesViewModel.deleteNote(note)
            noteRVAdapter?.notifyItemRemoved(viewHolder.adapterPosition)
        }
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
