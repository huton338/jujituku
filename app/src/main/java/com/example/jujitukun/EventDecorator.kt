package com.example.jujitukun

import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.spans.DotSpan

class EventDecorator(_color :Int,_dates:Collection<CalendarDay>) : DayViewDecorator {

    //初期化
    private val color = _color
    //重複を削除
    private val dates = HashSet<CalendarDay>(_dates)

    //日付の存在チェック
    override fun shouldDecorate(day: CalendarDay?): Boolean {
        return dates.contains(day)
    }

    //viewにドットを設定
    override fun decorate(view: DayViewFacade?) {
        view?.addSpan(DotSpan(5F,color))
    }

}