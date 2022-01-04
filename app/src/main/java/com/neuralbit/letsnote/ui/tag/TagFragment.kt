package com.neuralbit.letsnote.ui.tag

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.neuralbit.letsnote.R
import com.neuralbit.letsnote.adapters.LabelRVAdapter
import com.neuralbit.letsnote.databinding.FragmentTagBinding
import com.neuralbit.letsnote.databinding.LabelFragmentBinding
import com.neuralbit.letsnote.entities.Note
import com.neuralbit.letsnote.entities.Tag
import com.neuralbit.letsnote.ui.label.LabelViewModel
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