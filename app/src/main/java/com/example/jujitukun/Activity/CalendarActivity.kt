package com.example.jujitukun.Activity

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.jujitukun.CalendarProviderQuery
import com.example.jujitukun.CalendarRecyclerAdapter
import com.example.jujitukun.Dto.EventDto
import com.example.jujitukun.EventDecorator
import com.example.jujitukun.R
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener
import kotlinx.android.synthetic.main.activity_calendar.*
import org.threeten.bp.LocalDate
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


//https://github.com/prolificinteractive/material-calendarview
//TODO:上記のライブラリ使ってカレンダーを実装する
class CalendarActivity : AppCompatActivity(), OnDateSelectedListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        var calendars = CalendarProviderQuery().queryCalendar(this, this)
        var events = CalendarProviderQuery().queryEvent(this, this)

        var dates = events?.let { toDotDates(it) }

        if (dates != null && dates.isNotEmpty()) {
            var decorator = EventDecorator(Color.RED, dates)
            calendarView.addDecorator(decorator)
        }

        //this→onDateSelected
        calendarView.setOnDateChangedListener(this)
    }

    override fun onDateSelected(
        widget: MaterialCalendarView,
        date: CalendarDay,
        selected: Boolean
    ) {
        val events = CalendarProviderQuery().queryEventBySelectedDay(
            this,
            this,
            date.year,
            date.month,
            date.day
        )

        viewManager = LinearLayoutManager(this)

        if (!events.isNullOrEmpty()) {
            //findbyで取得後設定を行わなくても recycleview名.xxx で設定できる
            var radapter = CalendarRecyclerAdapter(events as ArrayList<EventDto>)
            viewAdapter = radapter
            recyclerView = findViewById<RecyclerView>(R.id.eventRecyclerView).apply {
                setHasFixedSize(true)
                layoutManager = viewManager
                adapter = viewAdapter
            }
        }else{
            //eventがnullの場合adapterを空にして空白表示に戻す
            recyclerView = findViewById<RecyclerView>(R.id.eventRecyclerView).apply {
                layoutManager = viewManager
                adapter = null
            }
        }



    }


    //TODO:性能改善
    //この中の処理重すぎて件数捌けない
    /**
     * カレンダーにdotを描画する日付を抽出.
     * EventDecoratorに設定.
     */
    private fun toDotDates(events: Collection<EventDto>): Collection<CalendarDay> {
        var dates = ArrayList<CalendarDay>()
        for (event in events) {
            var startDate = epochtimeToLocalDate(event.dtStart)
            var endDate = epochtimeToLocalDate(event.dtEnd)
            //開始日から終了日までの間のすべての日を配列に追加
            do {
                dates.add(CalendarDay.from(startDate))
                startDate.plusDays(1L)
            } while (startDate.compareTo(endDate) > 0)
        }
        return dates
    }

    /**
     * epochtimeからLocalDateへ変換.
     */
    private fun epochtimeToLocalDate(epochtime: Long): LocalDate {
        val date = Date(epochtime)
        //APKレベルを最下に合わせて実装
        // DateTimeFormatter.ISO_LOCAL_DATEに無理やり合わせている
        val strDate = SimpleDateFormat("YYYY-MM-dd").format(date)
        return LocalDate.parse(strDate)
    }

}
