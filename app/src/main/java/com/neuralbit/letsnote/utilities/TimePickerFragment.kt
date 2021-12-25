package com.neuralbit.letsnote.utilities

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.format.DateFormat.is24HourFormat
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import java.util.*

class TimePickerFragment(private val getTimeFromPicker : GetTimeFromPicker ) : DialogFragment() , TimePickerDialog.OnTimeSetListener {

    override fun onTimeSet(p0: TimePicker?, p1: Int, p2: Int) {
        val c = Calendar.getInstance()
        c[Calendar.HOUR_OF_DAY] = p1
        c[Calendar.MINUTE] = p2
        c[Calendar.SECOND] = 0
        getTimeFromPicker.getTimeInfo(c)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        return TimePickerDialog(activity,this,hour,minute, is24HourFormat(activity))
    }
}

interface GetTimeFromPicker{
    fun getTimeInfo( calendar: Calendar)
}