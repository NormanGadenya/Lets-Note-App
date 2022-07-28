package com.neuralbit.letsnote.ui.tag

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.neuralbit.letsnote.NoteViewModel
import com.neuralbit.letsnote.databinding.FragmentTagBinding
import java.util.*

class TagFragment : Fragment() {

    private val tagViewModel: TagViewModel by activityViewModels()
    private val noteViewModel: NoteViewModel by activityViewModels()
    private var tagCount = HashMap<String,Int>()
    private var _binding: FragmentTagBinding? = null
    lateinit var tagRV: RecyclerView
    private val binding get() = _binding!!
    var tagList : ArrayList<String> = ArrayList()

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

//        tagViewModel.allTags.observe(viewLifecycleOwner){
//            for (tag in it){
//                lifecycleScope.launch {
//
//                    tagCount[tag.tagTitle] = tagViewModel.getNotesWithTag(tag.tagTitle).first().notes.size
//                    tagRVAdapter?.tagCount = tagCount
//                    tagRVAdapter?.notifyDataSetChanged()
//
//                }
//            }
//            tagList = ArrayList<Tag>()
//            tagList.addAll(it)
//            tagRVAdapter?.updateTagList(tagList)
//
//        }
        noteViewModel.allFireTags().observe(viewLifecycleOwner){
            val tagList = ArrayList<Tag>()
            for (t in it){
                val tag = Tag(t.tagName,t.noteUids.size)
                tagList.add(tag)
            }
            tagViewModel.allTags = tagList
            tagRVAdapter?.updateTagList(tagList)
        }

        tagViewModel.searchQuery.observe(viewLifecycleOwner){
            if(it!=null){
                tagRVAdapter?.searchString = it
                tagRVAdapter?.updateTagList(filterTags(it))

            }else{
                tagRVAdapter?.updateTagList(tagViewModel.allTags)
            }
        }


//        val touchHelperTag = ItemTouchHelper(object  : ItemTouchHelper.SimpleCallback(0,
//            ItemTouchHelper.RIGHT){
//            override fun onMove(
//                recyclerView: RecyclerView,
//                viewHolder: RecyclerView.ViewHolder,
//                target: RecyclerView.ViewHolder
//            ): Boolean {
//
//                return true
//            }
//
//            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
//                val tag = tagList[viewHolder.adapterPosition]
//                lifecycleScope.launch {
//                    val notes =  tagViewModel.getNotesWithTag(tag.tagTitle)
//
//                    val deleteDialog: AlertDialog? = this.let {
//                        val builder = AlertDialog.Builder(context)
//                        builder.apply {
//                            setPositiveButton("ok"
//                            ) { _, _ ->
//                                for (note in notes.first().notes){
//
//                                    tagViewModel.deleteNoteTagCrossRef(NoteTagCrossRef(note.noteID,tag.tagTitle))
//                                }
//                                tagViewModel.deleteTag(tag)
//                                tagRVAdapter?.notifyItemRemoved(viewHolder.adapterPosition)
//                                Toast.makeText(context, "#${tag.tagTitle} Deleted", Toast.LENGTH_SHORT).show()
//
//
//                            }
//                            setNegativeButton("cancel"
//                            ) { _, _ ->
////                                tagRVAdapter?.updateTagList(tagList)
//
//                            }
//
//                            setTitle("Delete ${tag.tagTitle}")
//
//                        }
//                        builder.create()
//                    }
//                    deleteDialog?.show()
//
//
//                }
//
//
//
//            }
//
//            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
//                super.onSelectedChanged(viewHolder, actionState)
//                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE){
//                    val iView = viewHolder?.itemView as CardView
//                    iView.setCardBackgroundColor(resources.getColor(R.color.Red))
//
//                }
//            }
//
//            override fun clearView(
//                recyclerView: RecyclerView,
//                viewHolder: RecyclerView.ViewHolder
//            ) {
//                super.clearView(recyclerView, viewHolder)
//                val iView = viewHolder.itemView as CardView
//
//                try{
//                    iView.setCardBackgroundColor(resources.getColor(R.color.Apricot))
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//                        iView.outlineSpotShadowColor = resources.getColor(R.color.Apricot)
//                    }
//                }catch (e : Exception){
//                    e.printStackTrace()
//                }
//
//
//            }
//        })
//
//        touchHelperTag.attachToRecyclerView(tagRV)

        return root
    }
    private fun filterTags(text : String) : ArrayList<Tag>{
        val newList = ArrayList<Tag>()
        val textLower= text.toLowerCase(Locale.ROOT)
        for ( tag in tagViewModel.allTags){
            if(tag.title.toLowerCase(Locale.ROOT).contains(textLower)){
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