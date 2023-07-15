package com.neuralbit.letsnote.ui.addEditNote

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.neuralbit.letsnote.R
import com.neuralbit.letsnote.utilities.Common


class AddEditTagRVAdapter (
    val context: Context,
    private val tagRVInterface : TagRVInterface

    ) : RecyclerView.Adapter<AddEditTagRVAdapter.ViewHolder>(){
    var deleteIgnored = false
    private val allTags = ArrayList<String>()
    var lifecycleOwner : LifecycleOwner ? = null
    var viewModel : NoteViewModel ? = null
    val TAG = " TAG "
    private var cm : Common = Common()
    var defaultTextColor =0;



    inner class ViewHolder( itemView: View) : RecyclerView.ViewHolder(itemView){
        val tagTitle : TextView = itemView.findViewById(R.id.tagTitle)
        val deleteBtn : ImageButton = itemView.findViewById(R.id.tagDelBtn)
        val layoutBackground : View = itemView.findViewById(R.id.tagConstLayout)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.tag_adapter_item,parent,false)

        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tag = allTags[position]
        var tagTitle = tag
        if (tagTitle[0] != '#'){
            tagTitle = "#$tagTitle"
        }
        holder.tagTitle.text = tagTitle
        holder.itemView.setOnLongClickListener {

            holder.deleteBtn.visibility = VISIBLE
            return@setOnLongClickListener true
        }
        holder.deleteBtn.setOnClickListener {
            holder.deleteBtn.visibility = GONE
            tagRVInterface.deleteTag(tag)
        }
        val tagBackgroundDrawable: Drawable = context.getResources().getDrawable(R.drawable.tag_background)
        val tagBackgroundDrawableWrapped = DrawableCompat.wrap(tagBackgroundDrawable)
        DrawableCompat.setTint(tagBackgroundDrawableWrapped, cm.darkenColor(R.color.def_Card_Color, 0.8f))
        holder.layoutBackground.background = tagBackgroundDrawableWrapped
        lifecycleOwner?.let {

            viewModel?.labelColor?.observe(it){ color ->
                if (color>0){
                    DrawableCompat.setTint(tagBackgroundDrawableWrapped, cm.darkenColor(color, 0.8f))
                    holder.layoutBackground.background = tagBackgroundDrawableWrapped
                    holder.tagTitle.setTextColor(cm.lightenColor(color,0.8f))

                }else{
                    DrawableCompat.setTint(tagBackgroundDrawableWrapped, cm.darkenColor(R.color.def_Card_Color, 0.8f))
                    holder.layoutBackground.background = tagBackgroundDrawableWrapped
                    holder.tagTitle.setTextColor(cm.lightenColor(R.color.def_Card_Color,0.8f))

                }
            }
            viewModel?.deleteIgnored?.observe(it){ d ->
                if (d){
                    holder.deleteBtn.visibility = GONE
                }
            }
        }

    }

    override fun getItemCount(): Int {
        return allTags.size
    }

    fun updateList( newList: ArrayList<String>){
        allTags.clear()
        allTags.addAll(newList)
        notifyDataSetChanged()
    }





}
interface TagRVInterface{
    fun deleteTag(tag : String)


}