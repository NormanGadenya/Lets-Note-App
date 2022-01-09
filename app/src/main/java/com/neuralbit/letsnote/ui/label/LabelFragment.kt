package com.neuralbit.letsnote.ui.label

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.neuralbit.letsnote.R
import com.neuralbit.letsnote.adapters.LabelRVAdapter
import com.neuralbit.letsnote.databinding.LabelFragmentBinding
import com.neuralbit.letsnote.entities.Label

class LabelFragment : Fragment() {
    private val labelViewModel: LabelViewModel by activityViewModels()
    private var labelCount = HashMap<Int,Int>()
    private var _binding:LabelFragmentBinding ? = null
    lateinit var labelRV:RecyclerView
    private val binding get() = _binding!!
    val TAG = "LabelFragment"
    private val labelIDs = HashSet<Int>()



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        _binding = LabelFragmentBinding.inflate(inflater,container,false)
        val root: View = binding.root
        labelRV = binding.noteLabelRV
        val layoutManager = StaggeredGridLayoutManager( 2, LinearLayoutManager.VERTICAL)
        labelRV.layoutManager =layoutManager
        val labelRVAdapter = context?.let { LabelRVAdapter(it) }
        labelRV.adapter= labelRVAdapter


        labelViewModel.getAllNotes().observe(viewLifecycleOwner){ list ->
            for (lwn in list){
                val label = lwn.label.labelID
                labelIDs.add(label)
                labelViewModel.getNotesWithLabel(label).observe(viewLifecycleOwner){
                    labelCount[label] = it.size

                    labelRVAdapter?.updateLabelCount(labelCount,labelIDs)
                }
            }

        }
            



        return root
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        val searchViewMenuItem = menu.findItem(R.id.search)
        searchViewMenuItem.isVisible = false
    }



}