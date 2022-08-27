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
                val welcomeIcon = binding.welcomeIcon
                val welcomeText = binding.allNotesText
                if (sortedTagList.isEmpty()){
                    welcomeIcon.visibility = View.VISIBLE
                    welcomeText.visibility = View.VISIBLE
                }else{
                    welcomeIcon.visibility = View.GONE
                    welcomeText.visibility = View.GONE
                }
                tagViewModel.allTagFire = ArrayList(sortedTagFireList)
                tagViewModel.allTags = ArrayList(sortedTagList)
                tagRVAdapter?.updateTagList(ArrayList(sortedTagList))
            }

        }



        tagViewModel.searchQuery.observe(viewLifecycleOwner){
            if(it!=null){
                tagRVAdapter?.searchString = it
                tagRVAdapter?.updateTagList(filterTags(it))

            }else{
                tagRVAdapter?.updateTagList(tagViewModel.allTags)
            }
        }


        return root
    }
    private fun filterTags(text : String) : ArrayList<Tag>{
        val newList = ArrayList<Tag>()
        val textLower= text.lowercase(Locale.ROOT)
        for ( tag in tagViewModel.allTags){
            if(tag.title.lowercase(Locale.ROOT).contains(textLower)){
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