package com.neuralbit.letsnote.ui.label

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.neuralbit.letsnote.LabelNotesActivity
import com.neuralbit.letsnote.NoteViewModel
import com.neuralbit.letsnote.R
import com.neuralbit.letsnote.adapters.LabelRVAdapter
import com.neuralbit.letsnote.databinding.LabelFragmentBinding
import com.neuralbit.letsnote.entities.LabelFire

class LabelFragment : Fragment(), LabelRVAdapter.LabelClick {
    private val noteViewModel: NoteViewModel by activityViewModels()
    private val labelViewModel: LabelViewModel by activityViewModels()
    private var _binding:LabelFragmentBinding ? = null
    private lateinit var labelRV:RecyclerView
    private val binding get() = _binding!!
    val TAG = "LabelFragment"



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
        val labelRVAdapter = context?.let { LabelRVAdapter(it,this) }
        labelRV.adapter= labelRVAdapter


        noteViewModel.allFireLabels().observe(viewLifecycleOwner){
            val pref = context?.getSharedPreferences("DeletedNotes", AppCompatActivity.MODE_PRIVATE)
            val deletedNotes = pref?.getStringSet("noteUids", HashSet())
            val labelList = HashSet<Label>()
            val labelFireList = HashSet<LabelFire>()
            for ( l in it){
                val label = Label(l.labelColor,l.noteUids.size)
                if (deletedNotes != null){
                    for (n in l.noteUids){
                        if (!deletedNotes.contains(n)){
                            labelList.add(label)
                            labelFireList.add(l)
                        }
                    }

                }else{
                    labelList.add(label)
                    labelFireList.add(l)
                }
            }
            val sortedLabelList = labelList.sortedBy { i -> i.labelCount }.reversed()
            val sortedLabelFireList = labelFireList.sortedBy { i -> i.noteUids.size }.reversed()
            labelViewModel.labelFire = sortedLabelFireList
            labelRVAdapter?.updateLabelList(ArrayList(sortedLabelList))
        }

        return root
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        val searchViewMenuItem = menu.findItem(R.id.search)
        searchViewMenuItem.isVisible = false
    }

    override fun onLabelClick(labelColor: Int) {
        for (label in labelViewModel.labelFire){
            if (label.labelColor == labelColor){
                val intent = Intent(context, LabelNotesActivity::class.java)
                intent.putExtra("labelColor",labelColor)
                intent.putStringArrayListExtra("noteUids", label.noteUids)
                startActivity(intent)
            }
        }
    }


}