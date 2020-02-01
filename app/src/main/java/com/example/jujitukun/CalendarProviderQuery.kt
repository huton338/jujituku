package com.example.jujitukun

import android.Manifest
import android.app.Activity
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.CalendarContract
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.core.database.getIntOrNull
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull
import java.util.*



class CalendarProviderQuery {

    private val appContext = JujutukunApplication.applicationContext()
    val callbackId = 42

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
    fun queryCalendar(context: Context, activity: Activity) {
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


        // Use the cursor to step through the returned records
        if (cur != null && cur.count > 0) {
            while (cur.moveToNext()) {
                // Get the field values
                val calID: Long = cur.getLong(CAL_PROJECTION_ID_INDEX)
                val displayName: String = cur.getString(CAL_PROJECTION_DISPLAY_NAME_INDEX)
                val accountName: String = cur.getString(CAL_PROJECTION_ACCOUNT_NAME_INDEX)
                val ownerName: String = cur.getString(CAL_PROJECTION_OWNER_ACCOUNT_INDEX)
                val accessLevel: Int = cur.getInt(CAL_PROJECTION_CALENDAR_ACCESS_LEVEL_INDEX)
                // Do something with the values...

                Log.v("calID", calID.toString())
                Log.v("displayName", displayName)
                Log.v("accountName", accountName)
                Log.v("ownerName", ownerName)
            }
        }
    }

    fun queryEvent(context: Context, activity: Activity) {

        //Calendar:permissionチェック
        checkPermission(
            context,
            activity,
            Manifest.permission.READ_CALENDAR,
            Manifest.permission.WRITE_CALENDAR
        )

        val dtstart: Calendar = Calendar.getInstance()
        dtstart.add(Calendar.YEAR, -3)
        val dtend: Calendar = Calendar.getInstance()
        dtend.add(Calendar.YEAR, 5)


        // Run query
        val uri: Uri = CalendarContract.Events.CONTENT_URI
        val selection: String = "((${CalendarContract.Events.DTSTART} >= ?) AND (" +
                "${CalendarContract.Events.DTEND} <= ?))"
        val selectionArgs: Array<String> =
            arrayOf(dtstart.timeInMillis.toString(), dtend.timeInMillis.toString())
        val cur: Cursor? = appContext.contentResolver.query(
            uri,
            EVENT_PROJECTION,
            selection,
            selectionArgs,
            "${CalendarContract.Events.DTSTART} asc"
        )


        // Use the cursor to step through the returned records

        if (cur != null && cur.count > 0) {
            while (cur.moveToNext()) {
                // Get the field values
                val calendarID: Long = cur.getLong(EV_PROJECTION_CALENDAR_ID_INDEX)
                val organizer: String = cur.getString(EV_PROJECTION_ORGANIZER_INDEX)
                val title: String = cur.getString(EV_PROJECTION_TITLE_INDEX)
                val location: String = cur.getString(EV_PROJECTION_EVENT_LOCATION_INDEX)
                val description: String? = cur.getStringOrNull(EV_PROJECTION_DESCRIPTION_INDEX)
                val dtstart: Long? = cur.getLongOrNull(EV_PROJECTION_DTSTART_INDEX)
                val dtend: Long? = cur.getLongOrNull(EV_PROJECTION_DTEND_INDEX)
                val allDay: Int? = cur.getIntOrNull(EV_PROJECTION_ALL_DAY_INDEX)
                val timezone :String? = cur.getStringOrNull(EV_PROJICTION_EVENT_TIMEZONE)

                Log.v("calendarID", calendarID.toString())
                Log.v("title", title)
            }
        }

    }

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



