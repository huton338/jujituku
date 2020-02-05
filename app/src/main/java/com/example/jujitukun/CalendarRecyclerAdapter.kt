package com.example.jujitukun;

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.jujitukun.Dto.EventDto
import kotlinx.android.synthetic.main.event_row.view.*
import org.threeten.bp.LocalDate
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class CalendarRecyclerAdapter(val events: ArrayList<EventDto>) :
    RecyclerView.Adapter<CalendarRecyclerAdapter.CalendarHolder>() {


    class CalendarHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleView: TextView = view.eventTitleView
        val timeView: TextView = view.eventTimeView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.event_row,parent,false)
        return CalendarHolder(view)
    }

    override fun getItemCount(): Int {
        return events.size
    }

    override fun onBindViewHolder(holder: CalendarHolder, position: Int) {
        val event = events[position]
        holder.timeView.text=epochtimeToTimeViewText(event)
        holder.titleView.text=event.titile
    }


    /**
     * epochtimeから画面に表示する形式へフォーマット.
     * 開始時間 - 終了時間 または　終日の表記へ変換.
     * 例："15:00 - 20:00" または　"終日" .
     */
    private fun epochtimeToTimeViewText(event:EventDto):String{

        //終日フラグ：ON
        val allday:Int =1

        var startStr = epochtimeToTimeStr(event.dtStart)
        var endStr = epochtimeToTimeStr(event.dtEnd)

        if (startStr.isNullOrEmpty() or endStr.isNullOrEmpty() or event.allDay.equals(allday)){
            return "終日"
        }

        return "$startStr - $endStr"

    }

    /**
     * epochtimeから時間文字列(HH:mm)へ変換.
     * 例："15:00".
     */
    private fun epochtimeToTimeStr(epochtime:Long): String? {

        if (epochtime==-1L) return null

        val startDate = Date(epochtime)
        //APKレベルを最下に合わせて実装
        // DateTimeFormatter.ISO_LOCAL_DATEに無理やり合わせている
        return  SimpleDateFormat("HH:mm").format(startDate)
    }
}


