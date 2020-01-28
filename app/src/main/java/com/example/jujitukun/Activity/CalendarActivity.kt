package com.example.jujitukun.Activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.jujitukun.R


//https://github.com/prolificinteractive/material-calendarview
//TODO:上記のライブラリ使ってカレンダーを実装する
class CalendarActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)
    }
}
