package com.neuralbit.letsnote.ui.label

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
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

            val labelList = ArrayList<Label>()
            for ( l in it){
                val label = Label(l.labelColor,l.noteUids.size)
                labelList.add(label)
            }
            labelViewModel.labelFire = it
            labelRVAdapter?.updateLabelList(labelList)
        }
//        val touchHelperLabel = ItemTouchHelper(object  : ItemTouchHelper.SimpleCallback(0,
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
//                val list = ArrayList(labelIDs)
//                val labelID = list[viewHolder.adapterPosition]
//
//
//
//                    val deleteDialog: AlertDialog? = this.let {
//                        val builder = AlertDialog.Builder(context)
//                        builder.apply {
//                            setPositiveButton("ok"
//                            ) { _, _ ->
//
//                                labelViewModel.deleteLabel(labelID)
//
//                            }
//                            setNegativeButton("cancel"
//                            ) { _, _ ->
//                                labelRVAdapter?.updateLabelCount(labelCount,labelIDs)
//
//
//                            }
//
//                            setTitle("Delete label?")
//
//                        }
//                        builder.create()
//                    }
//                    deleteDialog?.show()
//
//
//
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
//                val list = ArrayList(labelIDs)
//                try {
//                    val labelID = list[viewHolder.adapterPosition]
//                    iView.setCardBackgroundColor(labelID)
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//                        iView.outlineSpotShadowColor = labelID
//                    }
//                }catch (e : Exception){
//                    e.printStackTrace()
//                }
//
//
//
//            }
//        })
//
//        touchHelperLabel.attachToRecyclerView(labelRV)

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