package com.neuralbit.letsnote.ui.tag

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.neuralbit.letsnote.R
import com.neuralbit.letsnote.adapters.LabelRVAdapter
import com.neuralbit.letsnote.databinding.FragmentTagBinding
import com.neuralbit.letsnote.databinding.LabelFragmentBinding
import com.neuralbit.letsnote.entities.*
import com.neuralbit.letsnote.ui.label.LabelViewModel
import com.neuralbit.letsnote.utilities.Common
import kotlinx.coroutines.launch

class TagFragment : Fragment() {

    private val tagViewModel: TagViewModel by activityViewModels()
    private var tagCount = HashMap<String,Int>()
    private var _binding: FragmentTagBinding? = null
    lateinit var tagRV: RecyclerView
    private val binding get() = _binding!!
    private lateinit var tagList : ArrayList<Tag>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        _binding = FragmentTagBinding.inflate(inflater, container, false)
        val root: View = binding.root
        tagRV = binding.noteTagRV
        val layoutManager = StaggeredGridLayoutManager( 2, LinearLayoutManager.VERTICAL)
        tagRV.layoutManager =layoutManager
        val tagRVAdapter = context?.let { TagRVAdapter(it) }
        tagRV.adapter= tagRVAdapter

        tagViewModel.allTags.observe(viewLifecycleOwner){
            for (tag in it){
                lifecycleScope.launch {

                    tagCount[tag.tagTitle] = tagViewModel.getNotesWithTag(tag.tagTitle).first().notes.size
                    tagRVAdapter?.tagCount = tagCount
                    tagRVAdapter?.notifyDataSetChanged()

                }
            }
            tagList = ArrayList<Tag>()
            tagList.addAll(it)
            tagRVAdapter?.updateTagList(tagList)

        }

        tagViewModel.searchQuery.observe(viewLifecycleOwner){
            if(it!=null){
                tagRVAdapter?.searchString = it
                tagRVAdapter?.updateTagList(filterNotes(it))

            }
        }

        val touchHelperTag = ItemTouchHelper(object  : ItemTouchHelper.SimpleCallback(0,
            ItemTouchHelper.RIGHT){
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {

                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val tag = tagList[viewHolder.adapterPosition]
                lifecycleScope.launch {
                   val notes =  tagViewModel.getNotesWithTag(tag.tagTitle)
                    for (note in notes.first().notes){
//                        tagViewModel.removeArchive(ArchivedNote(note.noteID))
//                        tagViewModel.removePin(PinnedNote(note.noteID))
//                        tagViewModel.deleteLabel(note.noteID)
//                        tagViewModel.deleteReminder(note.noteID)

                        tagViewModel.deleteNoteTagCrossRef(NoteTagCrossRef(note.noteID,tag.tagTitle))
//                        tagViewModel.deleteNote(note)
                    }
                    tagViewModel.deleteTag(tag)
                    tagRVAdapter?.notifyItemRemoved(viewHolder.adapterPosition)

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
                    val cm = Common()
                    iView.setCardBackgroundColor(resources.getColor(R.color.Apricot))

                }catch (e : Exception){
                    e.printStackTrace()
                }


//                viewHolder.itemView.setBackgroundColor(Color.TRANSPARENT)
            }
        })

        touchHelperTag.attachToRecyclerView(tagRV)

        return root
    }
    fun filterNotes(text : String) : ArrayList<Tag>{
        val newList = ArrayList<Tag>()


            val textLower= text.toLowerCase()
            for ( tag in tagList){

                if(tag.tagTitle?.toLowerCase()?.contains(textLower) == true){
                    newList.add(tag)
                }
            }

            return newList

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}