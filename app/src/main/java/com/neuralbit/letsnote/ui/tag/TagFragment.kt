package com.neuralbit.letsnote.ui.tag

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.neuralbit.letsnote.adapters.LabelRVAdapter
import com.neuralbit.letsnote.databinding.FragmentTagBinding
import com.neuralbit.letsnote.databinding.LabelFragmentBinding
import com.neuralbit.letsnote.entities.Tag
import com.neuralbit.letsnote.ui.label.LabelViewModel
import kotlinx.coroutines.launch

class TagFragment : Fragment() {

    private val tagViewModel: TagViewModel by activityViewModels()
    private var tagCount = HashMap<String,Int>()
    private var _binding: FragmentTagBinding? = null
    lateinit var tagRV: RecyclerView
    private val binding get() = _binding!!


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
                    tagCount[tag.tagTitle] = tagViewModel.getNotesWithTag(tag.tagTitle).size
                    tagRVAdapter?.tagCount = tagCount
                    tagRVAdapter?.notifyDataSetChanged()

                }
            }
            val arrayList = ArrayList<Tag>()
            arrayList.addAll(it)
            tagRVAdapter?.updateTagList(arrayList)

        }




        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}