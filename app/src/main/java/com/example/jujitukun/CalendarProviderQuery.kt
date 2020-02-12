package com.example.jujitukun

import android.Manifest
import android.app.Activity
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.CalendarContract
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.core.database.getIntOrNull
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull
import com.example.jujitukun.Dto.CalendarDto
import com.example.jujitukun.Dto.EventDto
import java.util.*
import kotlin.collections.ArrayList


class CalendarProviderQuery {

    val callbackId = 42

    private val appContext = JujutukunApplication.applicationContext()
    private val EVENTS_START_YEAR :Int= 0
    private val EVENTS_END_YEAR :Int= 1


    //Calendar properties
    private val CALENDAR_PROJECTION: Array<String> = arrayOf(
        CalendarContract.Calendars._ID,                     // 0
        CalendarContract.Calendars.ACCOUNT_NAME,            // 1
        CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,   // 2
        CalendarContract.Calendars.OWNER_ACCOUNT,           // 3
        CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL    // 4
    )
    private val CAL_PROJECTION_ID_INDEX: Int = 0
    private val CAL_PROJECTION_ACCOUNT_NAME_INDEX: Int = 1
    private val CAL_PROJECTION_DISPLAY_NAME_INDEX: Int = 2
    private val CAL_PROJECTION_OWNER_ACCOUNT_INDEX: Int = 3
    private val CAL_PROJECTION_CALENDAR_ACCESS_LEVEL_INDEX: Int = 4


    //Events properties
    private val EVENT_PROJECTION: Array<String> = arrayOf(
        CalendarContract.Events.CALENDAR_ID,          // 0
        CalendarContract.Events.ORGANIZER,            // 1
        CalendarContract.Events.TITLE,                // 2
        CalendarContract.Events.EVENT_LOCATION,       // 3
        CalendarContract.Events.DESCRIPTION,          // 4
        CalendarContract.Events.DTSTART,              // 5
        CalendarContract.Events.DTEND,                // 6
        CalendarContract.Events.ALL_DAY,              // 7
        CalendarContract.Events.EVENT_TIMEZONE        // 8
    )
    private val EV_PROJECTION_CALENDAR_ID_INDEX: Int = 0
    private val EV_PROJECTION_ORGANIZER_INDEX: Int = 1
    private val EV_PROJECTION_TITLE_INDEX: Int = 2
    private val EV_PROJECTION_EVENT_LOCATION_INDEX: Int = 3
    private val EV_PROJECTION_DESCRIPTION_INDEX: Int = 4
    private val EV_PROJECTION_DTSTART_INDEX: Int = 5
    private val EV_PROJECTION_DTEND_INDEX: Int = 6
    private val EV_PROJECTION_ALL_DAY_INDEX: Int = 7
    private val EV_PROJICTION_EVENT_TIMEZONE: Int = 8


    //TODO:非同期スレッドで実行するように変更
    fun queryCalendar(context: Context, activity: Activity) :Collection<CalendarDto>?{
        //Calendar:permissionチェック
        checkPermission(
            context,
            activity,
            Manifest.permission.READ_CALENDAR,
            Manifest.permission.WRITE_CALENDAR
        )

        // Run query
        val uri: Uri = CalendarContract.Calendars.CONTENT_URI
        val selection: String = "((${CalendarContract.Calendars.ACCOUNT_NAME} = ?) AND (" +
                "${CalendarContract.Calendars.ACCOUNT_TYPE} = ?) AND (" +
                "${CalendarContract.Calendars.OWNER_ACCOUNT} = ?))"
        val selectionArgs: Array<String> =
            arrayOf("akhr338.k@gmail.com", "com.google", "akhr338.k@gmail.com")
        val cur: Cursor? = appContext.contentResolver.query(
            uri,
            CALENDAR_PROJECTION,
            selection,
            selectionArgs,
            null
        )

        //Dtoへ変換
         return bindToCalendarDto(cur)
    }

    fun queryEvent(context: Context, activity: Activity) :Collection<EventDto>?{

        //Calendar:permissionチェック
        checkPermission(
            context,
            activity,
            Manifest.permission.READ_CALENDAR,
            Manifest.permission.WRITE_CALENDAR
        )

        val dtStart: Calendar = Calendar.getInstance()
        dtStart.add(Calendar.YEAR, EVENTS_START_YEAR)
        val dtEnd: Calendar = Calendar.getInstance()
        dtEnd.add(Calendar.YEAR, EVENTS_END_YEAR)


        // Run query
        val uri: Uri = CalendarContract.Events.CONTENT_URI
        val selection: String = "((${CalendarContract.Events.DTSTART} >= ?) AND (" +
                "${CalendarContract.Events.DTEND} <= ?))"
        val selectionArgs: Array<String> =
            arrayOf(dtStart.timeInMillis.toString(), dtEnd.timeInMillis.toString())
        val cur: Cursor? = appContext.contentResolver.query(
            uri,
            EVENT_PROJECTION,
            selection,
            selectionArgs,
            "${CalendarContract.Events.DTSTART} asc"
        )

        //Dtoへ変換
        return bindToEventDto(cur)
    }


    fun queryEventBySelectedDay(context: Context, activity: Activity,year:Int,month:Int,date:Int) :Collection<EventDto>?{

        //Calendar:permissionチェック
        checkPermission(
            context,
            activity,
            Manifest.permission.READ_CALENDAR,
            Manifest.permission.WRITE_CALENDAR
        )

        // startからendは24h後を1日とする
        // calendar providerの終日のデフォルトの値に合わせて設定している
        val gmt = TimeZone.getTimeZone("GMT")
        val dtStart: Calendar = Calendar.getInstance()
        //calendar providerの時間はGMT基準で保存されているため合わせる
        dtStart.timeZone=gmt
        //clearを入れてmillisecondを0に初期化
        dtStart.clear()
        //start: 2020/01/01 00h00m00s
        dtStart.set(year, month-1, date,0,0,0)
        val dtEnd: Calendar = Calendar.getInstance()
        dtEnd.timeZone=gmt
        dtEnd.clear()
        //end: 2020/01/02 00h00m00s
        dtEnd.set(year, month-1, date+1,0,0,0)

        // Run query
        val uri: Uri = CalendarContract.Events.CONTENT_URI
        val selection: String = "((${CalendarContract.Events.DTSTART} >= ?) AND (" +
                "${CalendarContract.Events.DTEND} <= ?))"
        val selectionArgs: Array<String> =
            arrayOf(dtStart.time.time.toString(), dtEnd.time.time.toString())
        val cur: Cursor? = appContext.contentResolver.query(
            uri,
            EVENT_PROJECTION,
            selection,
            selectionArgs,
            "${CalendarContract.Events.DTSTART} asc"
        )

        //Dtoへ変換
        return bindToEventDto(cur)
    }

    /**
     * CalendarProvider経由で取得したCalendar情報をDTOへ変換.
     */
    private fun bindToCalendarDto(cursor: Cursor?):Collection<CalendarDto>?{
        if (cursor != null && cursor.count > 0) {
            val dtoList = ArrayList<CalendarDto>()
            while (cursor.moveToNext()) {
                val dto = CalendarDto()
                dto.id = cursor.getLong(CAL_PROJECTION_ID_INDEX)
                dto.accnoutName = cursor.getString(CAL_PROJECTION_ACCOUNT_NAME_INDEX)
                dto.accountDisplayName = cursor.getString(CAL_PROJECTION_DISPLAY_NAME_INDEX)
                dto.ownerAccount = cursor.getString(CAL_PROJECTION_OWNER_ACCOUNT_INDEX)
                dto.calendarAccessLevel = cursor.getInt(CAL_PROJECTION_CALENDAR_ACCESS_LEVEL_INDEX)
                dtoList.add(dto)
            }
            return dtoList
        }
        return null
    }

    /**
     * CalendarProvider経由で取得したEvent情報をDTOへ変換.
     */
    private fun bindToEventDto(cursor: Cursor?):Collection<EventDto>?{
        if(cursor != null && cursor.count>0){
            val dtoList = ArrayList<EventDto>()
            while (cursor.moveToNext()){
                var dto = EventDto()
                dto.calendarId=cursor.getLong(EV_PROJECTION_CALENDAR_ID_INDEX)
                dto.organizer = cursor.getString(EV_PROJECTION_ORGANIZER_INDEX)
                dto.titile=cursor.getString(EV_PROJECTION_TITLE_INDEX)
                dto.location=cursor.getStringOrNull(EV_PROJECTION_EVENT_LOCATION_INDEX) ?:""
                dto.description=cursor.getStringOrNull(EV_PROJECTION_DESCRIPTION_INDEX) ?:""
                dto.dtStart=cursor.getLong(EV_PROJECTION_DTSTART_INDEX)
                dto.dtEnd=cursor.getLongOrNull(EV_PROJECTION_DTEND_INDEX) ?:-1L
                dto.allDay=cursor.getIntOrNull(EV_PROJECTION_ALL_DAY_INDEX) ?:-1
                dto.timeZone=cursor.getString(EV_PROJICTION_EVENT_TIMEZONE)
                dtoList.add(dto)
            }
         return dtoList
        }
        return null
    }


    //permissionの確認とpermissionの付与
    private fun checkPermission(
        context: Context,
        activity: Activity,
        vararg permissionsId: String
    ) {
        var permissions = true
        for (p in permissionsId) {
            permissions =
                permissions && ContextCompat.checkSelfPermission(
                    context,
                    p
                ) == PermissionChecker.PERMISSION_GRANTED
        }
        if (!permissions) ActivityCompat.requestPermissions(activity, permissionsId, callbackId)
    }

}



