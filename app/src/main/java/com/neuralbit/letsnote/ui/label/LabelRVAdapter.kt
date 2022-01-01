package com.neuralbit.letsnote.ui.label

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.neuralbit.letsnote.LabelNotesActivity
import com.neuralbit.letsnote.R
import com.neuralbit.letsnote.utilities.Common

class LabelRVAdapter(
    val context: Context
    ): RecyclerView.Adapter<LabelRVAdapter.ViewHolder>() {
    val TAG = "LabelRV"
    var labelCount : Map<Int,Int> = HashMap()
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val noteCountTV: TextView = itemView.findViewById(R.id.noteCount)
        val labelCard : CardView = itemView.findViewById(R.id.labelCard)
        val noteTV : TextView = itemView.findViewById(R.id.textView2)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.label_item,parent,false)

        return ViewHolder(itemView)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cm = Common()
        val labelID = position+1
        val labelCountVal = labelCount[labelID]
        val labelCardColor = context.getColor(cm.getLabelColor(labelID) )
        holder.labelCard.setCardBackgroundColor(labelCardColor)
        holder.noteCountTV.text = labelCountVal.toString()
        holder.noteCountTV.setTextColor(context.getColor(cm.getFontColor(labelCardColor)))
        holder.noteTV.setTextColor(context.getColor(cm.getFontColor(labelCardColor)))
        if (labelCountVal==1){
            holder.noteTV.text = "note"
        }
        holder.labelCard.setOnClickListener {
            val intent = Intent(context,LabelNotesActivity::class.java)
            intent.putExtra("labelID",labelID)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return labelCount.size
    }
    fun updateLabelCount(labelCountMap : Map<Int,Int>){
        labelCount = labelCountMap
        notifyDataSetChanged()
    }
}