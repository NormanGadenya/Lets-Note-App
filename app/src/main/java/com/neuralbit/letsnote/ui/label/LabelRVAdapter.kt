package com.neuralbit.letsnote.ui.label

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import com.neuralbit.letsnote.R
import com.neuralbit.letsnote.utilities.Common
import kotlinx.coroutines.*


class LabelRVAdapter(
    val context: Context,
    private val labelClick: LabelClick
    ): RecyclerView.Adapter<LabelRVAdapter.ViewHolder>() {
    val TAG = "LabelRV"
    private var labels = ArrayList<Label>()
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val noteCountTV: TextView = itemView.findViewById(R.id.noteCount)
        val labelTitleTV: TextView = itemView.findViewById(R.id.labelName)
        val labelCard : View = itemView.findViewById(R.id.labelCard)
        val noteText : TextView = itemView.findViewById(R.id.noteText)
        val noteArrow : ImageView = itemView.findViewById(R.id.imgView)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.label_item,parent,false)

        return ViewHolder(itemView)

    }

    private suspend fun updateNoteCountTV(value : Int, textView: TextView){
        withContext(Dispatchers.Main){
            for (i in 0..value){
                delay(100L)
                textView.text = i.toString()
            }
        }
    }


    @OptIn(DelicateCoroutinesApi::class)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cm = Common()
        val labelColor = labels[position].labelColor
        val labelCount = labels[position].labelCount
        val labelTitle = labels[position].labelTitle

        holder.labelCard.setBackgroundColor(labelColor)
        holder.labelTitleTV.text = labelTitle
        holder.labelTitleTV.setTextColor(cm.darkenColor(labelColor, 0.8f))
        holder.noteText.setTextColor(cm.darkenColor(labelColor, 0.8f))
        holder.noteCountTV.setTextColor(cm.darkenColor(labelColor, 0.8f))
        val drawable: Drawable = context.getResources().getDrawable(R.drawable.ic_baseline_arrow_forward_24)
        var wrappedDrawable = DrawableCompat.wrap(drawable)
        wrappedDrawable = wrappedDrawable.mutate()
        DrawableCompat.setTint(wrappedDrawable, cm.darkenColor(labelColor, 0.8f))
        holder.noteArrow.setImageDrawable(drawable)
        if (labelCount == 1){
            holder.noteText.text = "note"
        }else{
            holder.noteText.text = "notes"
        }


        GlobalScope.launch {

            updateNoteCountTV(labelCount,holder.noteCountTV)
        }


        holder.labelCard.setOnClickListener {
            labelClick.onLabelClick(labelColor)
        }
    }

    override fun getItemCount(): Int {
        return labels.size
    }

    fun updateLabelList(labels : ArrayList<Label>){
        this.labels.clear()
        this.labels = labels
        notifyDataSetChanged()
    }

    interface LabelClick{
        fun onLabelClick(labelColor : Int)
    }
}