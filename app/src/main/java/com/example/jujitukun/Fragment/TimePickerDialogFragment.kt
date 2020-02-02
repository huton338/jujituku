package com.example.jujitukun.Fragment

import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import java.util.*

class TimePickerDialogFragment : DialogFragment(),TimePickerDialog.OnTimeSetListener{


    interface  OnTimeSelectedListener{
        fun onSelectedTime(hourOfDay: Int,minute: Int)
    }

    private var listener: OnTimeSelectedListener? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        when(context){
            is OnTimeSelectedListener -> listener = context
        }
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val c = Calendar.getInstance()
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)
        return TimePickerDialog(context,this,hour,minute,true)
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        listener?.onSelectedTime(hourOfDay,minute)
    }

}