package com.neuralbit.letsnote.ui.tag

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.neuralbit.letsnote.NoteViewModel
import com.neuralbit.letsnote.TagNotesActivity
import com.neuralbit.letsnote.databinding.FragmentTagBinding
import com.neuralbit.letsnote.entities.TagFire
import com.neuralbit.letsnote.ui.allNotes.AllNotesViewModel
import java.util.*

class TagFragment : Fragment(), TagRVAdapter.TagItemClick {

    private val tagViewModel: TagViewModel by activityViewModels()
    private val noteViewModel: NoteViewModel by activityViewModels()
    private val allNotesViewModel : AllNotesViewModel by activityViewModels()
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

        val settingsSharedPref = context?.getSharedPreferences("Settings", AppCompatActivity.MODE_PRIVATE)
        val staggeredLayoutManagerAll = StaggeredGridLayoutManager( 2,LinearLayoutManager.VERTICAL)
        tagRV.layoutManager = staggeredLayoutManagerAll
        allNotesViewModel.deleteFrag.value = false
        allNotesViewModel.staggeredView.value = settingsSharedPref?.getBoolean("staggered",true)
        allNotesViewModel.staggeredView.observe(viewLifecycleOwner){
            val editor: SharedPreferences.Editor ?= settingsSharedPref?.edit()
            editor?.putBoolean("staggered",it)
            editor?.apply()
            if (it){
                tagRV.layoutManager = staggeredLayoutManagerAll
            }else{

                tagRV.layoutManager = LinearLayoutManager(context)
            }
        }

        val layoutManager = StaggeredGridLayoutManager( 2, LinearLayoutManager.VERTICAL)
        tagRV.layoutManager =layoutManager
        val tagRVAdapter = context?.let { TagRVAdapter(it,this) }
        tagRV.adapter= tagRVAdapter

        noteViewModel.allFireTags().observe(viewLifecycleOwner){
            val pref = context?.getSharedPreferences("DeletedNotes", AppCompatActivity.MODE_PRIVATE)
            val deletedNotes = pref?.getStringSet("noteUids", HashSet())
            val tagList = HashSet<Tag>()
            val tagFireList = HashSet<TagFire>()
            allNotesViewModel.allFireNotes.observe(viewLifecycleOwner){ allNotes ->

                val archivedNotes = ArrayList<String>()
                for ( n in allNotes){
                    if (n.archived){
                        n.noteUid?.let { it1 -> archivedNotes.add(it1) }
                    }
                }
                for ( t in it){
                    val tag = Tag(t.tagName,t.noteUids.size)

                    for ( n in t.noteUids){
                        if(archivedNotes.contains(n) || deletedNotes?.contains(n) == true){
                            tag.noteCount-=1
                        }
                        if (!archivedNotes.contains(n)){
                            if (deletedNotes != null){

                                if (!deletedNotes.contains(n)){
                                    tagList.add(tag)
                                    tagFireList.add(t)
                                }


                            }else{
                                tagList.add(tag)
                                tagFireList.add(t)
                            }
                        }

                    }

                }
                val sortedTagList = tagList.sortedBy { i -> i.noteCount }.reversed()
                val sortedTagFireList = tagFireList.sortedBy { i -> i.noteUids.size }.reversed()
                tagViewModel.allTagFire = ArrayList(sortedTagFireList)
                tagViewModel.allTags = ArrayList(sortedTagList)
                tagRVAdapter?.updateTagList(ArrayList(sortedTagList))
            }

        }


//        noteViewModel.allFireTags().observe(viewLifecycleOwner){ it ->
//            val tagFireList = HashSet<TagFire>()
//            val pref = context?.getSharedPreferences("DeletedNotes", AppCompatActivity.MODE_PRIVATE)
//            val deletedNotes = pref?.getStringSet("noteUids", HashSet())
//            val tagList = HashSet<Tag>()
//            for (t in it){
//                val tag = Tag(t.tagName,t.noteUids.size)
//                if (deletedNotes != null){
//                    for (n in t.noteUids){
//                        if (!deletedNotes.contains(n)){
//                            tagList.add(tag)
//                            tagFireList.add(t)
//                        }
//                    }
//
//                }else{
//                    tagList.add(tag)
//                    tagFireList.add(t)
//                }
//
//            }
//
//
//
//            val sortedTagList = tagList.sortedBy { i -> i.noteCount }.reversed()
//            val sortedTagFireList = tagFireList.sortedBy { i -> i.noteUids.size }.reversed()
//            tagViewModel.allTagFire = ArrayList(sortedTagFireList)
//            tagViewModel.allTags = ArrayList(sortedTagList)
//            tagRVAdapter?.updateTagList(ArrayList(sortedTagList))
//        }

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

    override fun onTagItemClick(tagTitle: String) {
        val tagList = tagViewModel.allTagFire
        for( t in tagList){
            if (t.tagName == tagTitle){
                val intent = Intent(context, TagNotesActivity::class.java)
                intent.putExtra("tagTitle",tagTitle)
                intent.putStringArrayListExtra("noteUids", t.noteUids)
                startActivity(intent)
            }
        }
    }
}