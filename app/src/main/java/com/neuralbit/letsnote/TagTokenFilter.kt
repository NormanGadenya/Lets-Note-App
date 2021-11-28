package com.neuralbit.letsnote

import android.util.Log
import android.widget.MultiAutoCompleteTextView
import com.teamwork.autocomplete.filter.HandleTokenFilter

class TagTokenFilter(handleChar :Char = '#') : HandleTokenFilter<Tag>(handleChar){
    override fun toTokenString(item: Tag): CharSequence {
        return handleChar + item.tagTitle
    }

    override fun matchesConstraint(item: Tag, constraint: CharSequence): Boolean {
        return item.tagTitle.toLowerCase().contains(constraint.toString().toLowerCase())
    }
}