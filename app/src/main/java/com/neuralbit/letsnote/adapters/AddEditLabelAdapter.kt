package com.neuralbit.letsnote.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.neuralbit.letsnote.R
import com.neuralbit.letsnote.entities.Label
import com.neuralbit.letsnote.entities.Note


class AddEditLabelAdapter(

    val context : Context,
    val labelClickInterface: LabelClickInterface

): RecyclerView.Adapter<AddEditLabelAdapter.ViewHolder>() {
    private var labelIDs = ArrayList<Int>()


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        val labelView : ImageView = itemView.findViewById(R.id.labelRVItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.label_rv_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val labelID = labelIDs[position]
        holder.labelView.setColorFilter(labelID)
        holder.labelView.setOnClickListener{
            labelClickInterface.onLabelItemClick(labelID)
        }
    }

    override fun getItemCount(): Int {
        return labelIDs.size
    }

    fun updateLabelIDList(labelIDs : HashSet<Int>){
        val list = ArrayList<Int>(labelIDs)
        this.labelIDs = list
        notifyDataSetChanged()
    }
}

interface  LabelClickInterface{
    fun onLabelItemClick(labelID: Int)
}