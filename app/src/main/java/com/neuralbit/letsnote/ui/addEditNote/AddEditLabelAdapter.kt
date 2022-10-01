package com.neuralbit.letsnote.ui.addEditNote

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.neuralbit.letsnote.R
import com.neuralbit.letsnote.firebaseEntities.LabelFire


class AddEditLabelAdapter(

    val context : Context,
    private val labelClickInterface: LabelClickInterface

): RecyclerView.Adapter<AddEditLabelAdapter.ViewHolder>() {
    private var labels : List<LabelFire> = ArrayList()


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val labelView : View = itemView.findViewById(R.id.label)
        val labelViewIV : ImageView = itemView.findViewById(R.id.labelRVItem)
        val labelTitleTv : TextView = itemView.findViewById(R.id.labelTitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.label_rv_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val label = labels[position]
        val labelColor = label.labelColor
        val labelTitle = label.labelTitle
        if (labelTitle.isEmpty()){
            holder.labelTitleTv.visibility =GONE
        }else{
            holder.labelTitleTv.visibility = VISIBLE
        }
        holder.labelViewIV.setColorFilter(labelColor)
        holder.labelTitleTv.text = labelTitle
        holder.labelView.setOnClickListener {
            labelClickInterface.onLabelItemClick(labelColor)
        }
    }

    override fun getItemCount(): Int {
        return labels.size
    }

    fun updateLabelIDList(labels : List<LabelFire>){
        this.labels = labels
        notifyDataSetChanged()
    }
}

interface  LabelClickInterface{
    fun onLabelItemClick(labelColor: Int)
}