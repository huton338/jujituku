package com.example.jujitukun.Activity

import android.Manifest
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.example.jujitukun.CalendarProviderQuery
import com.example.jujitukun.EventDecorator
import com.example.jujitukun.R
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener
import kotlinx.android.synthetic.main.activity_calendar.*
import org.threeten.bp.LocalDate


//https://github.com/prolificinteractive/material-calendarview
//TODO:上記のライブラリ使ってカレンダーを実装する
class CalendarActivity : AppCompatActivity() ,OnDateSelectedListener{

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        var decorator = EventDecorator(Color.RED, listOf(CalendarDay.from(LocalDate.now()),CalendarDay.from(LocalDate.now().plusDays(1L))))
        calendarView.addDecorator(decorator)

        CalendarProviderQuery().queryCalendar(this,this)
        CalendarProviderQuery().queryEvent(this,this)

    }

    override fun onDateSelected(
        widget: MaterialCalendarView,
        date: CalendarDay,
        selected: Boolean
    ) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }



}
