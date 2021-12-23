package com.neuralbit.letsnote.utilities

import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import java.util.*


class DatePickerFragment (
    private val ctx :Activity,
    private val getDateFromPicker: GetDateFromPicker
    ): DialogFragment(), DatePickerDialog.OnDateSetListener {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        return DatePickerDialog(ctx,this,year,month,day)
    }

    override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {
        getDateFromPicker.getDateInfo(year,month,day)
    }
}

interface GetDateFromPicker{
    fun getDateInfo( year:Int,month: Int, day: Int )
}