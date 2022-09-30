package com.neuralbit.letsnote.utilities

import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
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

        val dialog = DatePickerDialog(ctx,this,year,month,day)
        dialog.datePicker.minDate = System.currentTimeMillis() -1000
        return dialog
    }

    override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {
        val c = Calendar.getInstance()

        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, day);
        getDateFromPicker.getDateInfo(c)
    }
}

interface GetDateFromPicker{
    fun getDateInfo( calendar : Calendar)
}