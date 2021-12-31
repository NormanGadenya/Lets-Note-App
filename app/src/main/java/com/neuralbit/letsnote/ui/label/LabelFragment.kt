package com.neuralbit.letsnote.ui.label

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.neuralbit.letsnote.NoteRVAdapter
import com.neuralbit.letsnote.R
import com.neuralbit.letsnote.databinding.LabelFragmentBinding
import com.neuralbit.letsnote.ui.archived.ArchivedViewModel

class LabelFragment : Fragment() {
    private val labelViewModel: LabelViewModel by activityViewModels()
    private var label1Count = 0
    private var label2Count = 0
    private var label3Count = 0
    private var label4Count = 0
    private var label5Count = 0
    private var label6Count = 0
    private var labelCount = HashMap<Int,Int>()
    private var _binding:LabelFragmentBinding ? = null
    lateinit var labelRV:RecyclerView
    private val binding get() = _binding!!

    val TAG = "LabelFragment"


    private lateinit var viewModel: LabelViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = LabelFragmentBinding.inflate(inflater,container,false)
        val root: View = binding.root
        labelRV = binding.noteLabelRV
        val layoutManager = StaggeredGridLayoutManager( 2, LinearLayoutManager.VERTICAL)
        labelRV.layoutManager =layoutManager
        val labelRVAdapter = context?.let { LabelRVAdapter(it) }
        labelRV.adapter= labelRVAdapter


        for (labelID in 1..6){
            labelViewModel.getNotesWithLabel(labelID).observe(viewLifecycleOwner){
//                val notesCount=it.last().notes.size
//
//                labelCount[labelID] = notesCount
//
//                labelRVAdapter?.updateLabelCount(labelCount)
//                if (it.isNotEmpty()){
//                    Log.d(TAG, "onCreateView: ${it.last()}")
//
//                }
            }
            Log.d(TAG, "onCreateView: $labelID")
            labelCount.put(labelID,labelID)
            labelRVAdapter?.updateLabelCount(labelCount)
        }


        return root
    }



}