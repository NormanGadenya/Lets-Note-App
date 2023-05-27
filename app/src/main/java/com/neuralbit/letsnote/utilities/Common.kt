package com.neuralbit.letsnote.utilities

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
import android.text.style.RelativeSizeSpan
import android.widget.TextView
import androidx.core.graphics.ColorUtils
import com.neuralbit.letsnote.R
import java.text.SimpleDateFormat
import java.util.*


class Common (){

    val ARCHITECTS_DAUGHTER = "Architects daughter"
    val ABREEZE = "Abreeze"
    val ADAMINA = "Adamina"
    val BELLEZA ="Belleza"
    val JOTI_ONE = "Joti One"
    val NOVA_FLAT = "Nova flat"
     fun convertLongToTime(time: Long): List<String> {
        val date = Date(time)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
        val timeFormat = SimpleDateFormat("HH:mm")
        return listOf<String>(dateFormat.format(date),timeFormat.format(date))
    }

    fun currentTimeToLong(): Long {
        return System.currentTimeMillis()
    }

    fun getFontColor(color : Int):Int{
//
//        return if(ColorUtils.calculateLuminance(color) < 0.7){
//            R.color.lightGrey
//        }else{
//            R.color.black
//        }
        return if (color == 0){
            R.color.black
        }else if ( ColorUtils.calculateLuminance(color) > 0 && ColorUtils.calculateLuminance(color) > 0.6 ){
            R.color.lightGrey
        }else{
            R.color.black
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

    fun setHighlightFontSize(tv: TextView, textToHighlight: String){
        val tvt = tv.text.toString()
        var ofe = tvt.indexOf(textToHighlight, 0)
        val wordToSpan: Spannable = SpannableString(tv.text)
        var ofs = 0
        while (ofs < tvt.length && ofe != -1) {
            ofe = tvt.indexOf(textToHighlight, ofs)
            if (ofe == -1) break else {
                wordToSpan.setSpan(
                    RelativeSizeSpan(1.5f),
                    ofe,
                    ofe + textToHighlight.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                tv.setText(wordToSpan, TextView.BufferType.SPANNABLE)
            }
            ofs = ofe + 1
        }
    }

    fun manipulateColor(color: Int, factor: Float): Int {
        val a: Int = Color.alpha(color)
        val r = Math.round(Color.red(color) * factor)
        val g = Math.round(Color.green(color) * factor)
        val b = Math.round(Color.blue(color) * factor)
        return Color.argb(
            a,
            Math.min(r, 255),
            Math.min(g, 255),
            Math.min(b, 255)
        )
    }

    fun darkenColor(color: Int, factor: Float): Int {
        return ColorUtils.blendARGB(color, Color.BLACK, factor)
    }

    fun lightenColor(color: Int, factor: Float): Int {
        return ColorUtils.blendARGB(color, Color.WHITE, factor)
    }


}
