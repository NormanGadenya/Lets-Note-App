package com.neuralbit.letsnote.utilities

import androidx.room.TypeConverter
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