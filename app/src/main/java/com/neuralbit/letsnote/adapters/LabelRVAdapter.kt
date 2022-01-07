package com.neuralbit.letsnote.adapters

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.cardview.widget.CardView
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import com.neuralbit.letsnote.LabelNotesActivity
import com.neuralbit.letsnote.R
import com.neuralbit.letsnote.utilities.Common
import kotlinx.coroutines.*

class LabelRVAdapter(
    val context: Context
    ): RecyclerView.Adapter<LabelRVAdapter.ViewHolder>() {
    val TAG = "LabelRV"
    var labelCount : Map<Int,Int> = HashMap()
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val noteCountTV: TextView = itemView.findViewById(R.id.noteCount)
        val labelCard : CardView = itemView.findViewById(R.id.labelCard)
        val noteTV : TextView = itemView.findViewById(R.id.textView2)
        val arrow : ImageView = itemView.findViewById(R.id.imgView)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.label_item,parent,false)
        return ViewHolder(itemView)

    }
    private suspend fun updateNoteCountTV(value : Int, textView: TextView){
        withContext(Dispatchers.Main){
            for (i in 0..value){
                delay(100L)

                textView.text= i.toString()
            }
        }
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cm = Common()
        val labelID = position+1
        val labelCountVal = labelCount[labelID]
        val labelCardColor = context.getColor(cm.getLabelColor(labelID) )

        holder.labelCard.setCardBackgroundColor(labelCardColor)


        GlobalScope.launch {
            if (labelCountVal != null) {
                updateNoteCountTV(labelCountVal,holder.noteCountTV)
            }
        }

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