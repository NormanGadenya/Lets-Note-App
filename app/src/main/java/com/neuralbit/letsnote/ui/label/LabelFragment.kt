package com.neuralbit.letsnote.ui.label

import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.neuralbit.letsnote.R
import com.neuralbit.letsnote.adapters.LabelRVAdapter
import com.neuralbit.letsnote.databinding.LabelFragmentBinding
import com.neuralbit.letsnote.entities.Label
import com.neuralbit.letsnote.entities.NoteTagCrossRef
import com.neuralbit.letsnote.utilities.Common
import kotlinx.coroutines.launch

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

            labelCount.clear()
            labelIDs.clear()
            for (lwn in list){
                val label = lwn.label.labelID
                labelIDs.add(label)
                labelViewModel.getNotesWithLabel(label).observe(viewLifecycleOwner){
                    labelCount[label] = it.size

                    labelRVAdapter?.updateLabelCount(labelCount,labelIDs)
                }
            }
            labelRVAdapter?.updateLabelCount(labelCount,labelIDs)


        }
        val touchHelperLabel = ItemTouchHelper(object  : ItemTouchHelper.SimpleCallback(0,
            ItemTouchHelper.RIGHT){
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {

                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val list = ArrayList(labelIDs)
                val labelID = list[viewHolder.adapterPosition]



                    val deleteDialog: AlertDialog? = this.let {
                        val builder = AlertDialog.Builder(context)
                        builder.apply {
                            setPositiveButton("ok"
                            ) { _, _ ->

                                labelViewModel.deleteLabel(labelID)

                            }
                            setNegativeButton("cancel"
                            ) { _, _ ->
                                labelRVAdapter?.updateLabelCount(labelCount,labelIDs)


                            }

                            setTitle("Delete label?")

                        }
                        builder.create()
                    }
                    deleteDialog?.show()






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
                val list = ArrayList(labelIDs)
                try {
                    val labelID = list[viewHolder.adapterPosition]
                    iView.setCardBackgroundColor(labelID)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        iView.outlineSpotShadowColor = labelID
                    }
                }catch (e : Exception){
                    e.printStackTrace()
                }



            }
        })

        touchHelperLabel.attachToRecyclerView(labelRV)
            



        return root
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        val searchViewMenuItem = menu.findItem(R.id.search)
        searchViewMenuItem.isVisible = false
    }



}