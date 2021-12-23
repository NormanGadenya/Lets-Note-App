package com.neuralbit.letsnote.utilities

import android.view.View
import android.widget.TextView
import com.neuralbit.letsnote.R
import com.neuralbit.letsnote.entities.Tag
import com.teamwork.autocomplete.view.AutoCompleteViewBinder
import com.teamwork.autocomplete.view.AutoCompleteViewHolder

class TagViewBinder : AutoCompleteViewBinder<Tag> {
    val TAG = TagViewBinder::class.simpleName
    override fun getItemId(item: Tag): Long {
//        try {
//            return item.tagTitle.toLong()
//        }catch (e : Exception) {
//            Log.e(TAG, "getItemId: ",e )
//        }
        return 0
    }

    override fun getItemLayoutId(): Int {
        return R.layout.tag_spinner_row
    }

    override fun getViewHolder(view: View): AutoCompleteViewHolder {
        return TagViewHolder(view)
    }

    override fun bindData(
        viewHolder: AutoCompleteViewHolder,
        item: Tag,
        constraint: CharSequence?
    ) {
        var tagViewHolder : TagViewHolder = viewHolder as TagViewHolder
        tagViewHolder.tagTitle.text = item.tagTitle
    }

    inner class TagViewHolder (view : View): AutoCompleteViewHolder(view){

        var tagTitle: TextView = view.findViewById(R.id.tagTitle)

    }

}
