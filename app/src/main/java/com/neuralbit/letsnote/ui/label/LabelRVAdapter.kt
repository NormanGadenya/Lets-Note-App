package com.neuralbit.letsnote.ui.label

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.neuralbit.letsnote.R

class LabelRVAdapter(
    context: Context,
    ): RecyclerView.Adapter<LabelRVAdapter.ViewHolder>() {
    val TAG = "LabelRV"
    var labelCount : Map<Int,Int> = HashMap()
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val noteCountTV: TextView = itemView.findViewById(R.id.noteCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.label_item,parent,false)
        return ViewHolder(itemView)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

            holder.noteCountTV.text = labelCount[position+1].toString()
            Log.d(TAG, "onBindViewHolder: $position")


    }

    override fun getItemCount(): Int {
        return labelCount.size
    }
    fun updateLabelCount(labelCountMap : Map<Int,Int>){
        labelCount = labelCountMap
        notifyDataSetChanged()
    }
}