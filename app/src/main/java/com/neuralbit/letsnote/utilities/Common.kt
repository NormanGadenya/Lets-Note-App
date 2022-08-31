package com.neuralbit.letsnote.utilities

import android.text.Spannable
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
import android.widget.TextView
import androidx.core.graphics.ColorUtils
import com.neuralbit.letsnote.R
import java.text.SimpleDateFormat
import java.util.*


class Common (){


     fun convertLongToTime(time: Long): List<String> {
        val date = Date(time)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
        val timeFormat = SimpleDateFormat("HH:mm")
        return listOf<String>(dateFormat.format(date),timeFormat.format(date))
    }

    fun currentTimeToLong(): Long {
        return System.currentTimeMillis()
    }

//    fun getLabelColor(labelID : Int) : Int{
//        return when (labelID){
//            1-> R.color.white
//            2 -> R.color.Wild_orchid
//            3 -> R.color.Honeydew
//            4 -> R.color.English_violet
//            5 -> R.color.Celadon
//            6 -> R.color.Apricot
//            else-> R.color.white
//        }
//    }

//    fun getToolBarDrawable (labelID: Int) : Int{
//        return when (labelID){
//            1-> R.drawable.white_toolbar
//            2 -> R.drawable.wild_orchid_toolbar
//            3 -> R.drawable.honey_drew_toolbar
//            4 -> R.drawable.ev_toolbar
//            5 -> R.drawable.celadon_toolbar
//            6 -> R.drawable.apricot_toolbar
//            else-> R.color.white
//        }
//    }
//    fun getStatusBarColor (labelID: Int) : Int{
//        return when (labelID){
//            1-> R.color.whiteDark
//            2 -> R.color.Wild_orchid_Dark
//            3 -> R.color.Honeydew_Dark
//            4 -> R.color.English_violet_Dark
//            5 -> R.color.Celadon_Dark
//            6 -> R.color.Apricot_Dark
//            else-> R.color.whiteDark
//        }
//    }
    fun getFontColor(color : Int):Int{
        return if(ColorUtils.calculateLuminance(color) < 0.5){
            R.color.lightGrey
        }else{
            R.color.darkGrey
        }


    }

    fun setHighLightedText(tv: TextView, textToHighlight: String) {
        val tvt = tv.text.toString()
        var ofe = tvt.indexOf(textToHighlight, 0)
        val wordToSpan: Spannable = SpannableString(tv.text)
        var ofs = 0
        while (ofs < tvt.length && ofe != -1) {
            ofe = tvt.indexOf(textToHighlight, ofs)
            if (ofe == -1) break else {
                // set color here
                wordToSpan.setSpan(
                    BackgroundColorSpan(-0x100),
                    ofe,
                    ofe + textToHighlight.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                tv.setText(wordToSpan, TextView.BufferType.SPANNABLE)
            }
            ofs = ofe + 1
        }
    }



}
