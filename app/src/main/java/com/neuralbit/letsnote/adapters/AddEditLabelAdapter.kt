package com.neuralbit.letsnote.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.neuralbit.letsnote.R


class AddEditLabelAdapter(

    val context : Context,
    private val labelClickInterface: LabelClickInterface

): RecyclerView.Adapter<AddEditLabelAdapter.ViewHolder>() {
    private var labelColors = ArrayList<Int>()


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        val labelView : ImageView = itemView.findViewById(R.id.labelRVItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.label_rv_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val labelColor = labelColors[position]
        holder.labelView.setColorFilter(labelColor)
        holder.labelView.setOnClickListener{
            labelClickInterface.onLabelItemClick(labelColor)
        }
    }

    override fun getItemCount(): Int {
        return labelColors.size
    }

    fun updateLabelIDList(labelColors : HashSet<Int>){
        val list = ArrayList<Int>(labelColors)
        this.labelColors = list
        notifyDataSetChanged()
    }
}

interface  LabelClickInterface{
    fun onLabelItemClick(labelColor: Int)
}