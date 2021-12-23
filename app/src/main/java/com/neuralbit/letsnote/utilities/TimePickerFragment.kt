package com.neuralbit.letsnote.utilities

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.format.DateFormat.is24HourFormat
import android.util.Log
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import java.text.DateFormat
import com.google.android.material.timepicker.TimeFormat
import java.util.*

class TimePickerFragment : DialogFragment() , TimePickerDialog.OnTimeSetListener {
    override fun onTimeSet(p0: TimePicker?, p1: Int, p2: Int) {
        Log.d("TAG", "onTimeSet: $p1 and $p2")
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        return TimePickerDialog(activity,this,hour,minute, is24HourFormat(activity))
    }
}