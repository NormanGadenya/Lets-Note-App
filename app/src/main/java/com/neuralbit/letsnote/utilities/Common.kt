package com.neuralbit.letsnote.utilities

import android.util.Log
import androidx.core.graphics.ColorUtils
import androidx.room.TypeConverter
import com.neuralbit.letsnote.R
import java.sql.Time
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

    fun getLabelColor(labelID : Int) : Int{
        return when (labelID){
            2 -> R.color.Wild_orchid
            3 -> R.color.Honeydew
            4 -> R.color.English_violet
            5 -> R.color.Celadon
            6 -> R.color.Apricot
            else-> R.color.white
        }
    }
    fun isDark(color : Int):Boolean{
        return ColorUtils.calculateLuminance(color) < 0.5;

    }



}
class DateTimeConverter {
    @TypeConverter
    fun toDate(dateLong: Long?): Date? {
        return dateLong?.let { Date(it) }
    }

    @TypeConverter
    fun fromDate(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun toTime(timeLong: Long?): Time? {
        return timeLong?.let { Time(it) }
    }

    @TypeConverter
    fun fromTime(time: Time?): Long? {
        return time?.time
    }


}