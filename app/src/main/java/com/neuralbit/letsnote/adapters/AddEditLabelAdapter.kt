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


class AddEditLabelAdapter(

    val context : Context

): RecyclerView.Adapter<AddEditLabelAdapter.ViewHolder>() {
    var labels : ArrayList<Label> = ArrayList()

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        val labelView : ImageView = itemView.findViewById(R.id.labelRVItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.label_rv_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val labelColor = labels[position].labelID
        holder.labelView.setColorFilter(labelColor);
    }

    override fun getItemCount(): Int {
        return labels.size
    }
}