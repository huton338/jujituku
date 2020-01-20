package com.example.jujitukun.Activity

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.format.DateFormat
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.jujitukun.AlarmBroadcastReceiver
import com.example.jujitukun.DatePickerDialogFragment
import com.example.jujitukun.Entity.Task
import com.example.jujitukun.R
import com.google.android.material.snackbar.Snackbar
import io.realm.Realm
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.activity_task_edit.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


class TaskEditActivity : AppCompatActivity(),
    DatePickerDialogFragment.OnDateSelectedListener {

    private lateinit var realm: Realm

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_edit)

        realm = Realm.getDefaultInstance()


        //カレンダー入力
        calIcon.setOnClickListener {
            val dialog = DatePickerDialogFragment()
            dialog.show(supportFragmentManager, "dialog_date")
        }

        //保存
        saveTask.setOnClickListener { view: View ->
            realm.executeTransaction { db: Realm ->
                val maxId = db.where<Task>().max("id")
                val nextId = (maxId?.toLong() ?: 0) + 1
                val task = db.createObject<Task>(nextId)
                val date = taskDateText.text.toString().toDate("yyyy/MM/dd")
                task.content = taskContetText.text.toString()
                if (date != null) task.deadline = date

                //LocalPush通知設定
                val c = Calendar.getInstance()
                c.time = date
                setLocalPushManager(c, task)
            }

            Snackbar.make(view, "追加しました", Snackbar.LENGTH_SHORT)
                .setAction("戻る") { finish() }
                .setActionTextColor(Color.YELLOW)
                .show()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }


    //DatePickerDialogFlagmentのinterface
    override fun OnSelected(year: Int, month: Int, date: Int) {
        val c = Calendar.getInstance()
        c.set(year, month, date)
        taskDateText.setText(DateFormat.format("yyyy/MM/dd", c), TextView.BufferType.NORMAL)
    }

    fun String.toDate(pattern: String = "yyyy/MM/dd"): Date? {
        return try {
            SimpleDateFormat(pattern).parse(this)
        } catch (e: IllegalArgumentException) {
            return null
        } catch (e: ParseException) {
            return null
        }
    }


    /**
     * localpush通知用設定.
     */
    private fun setLocalPushManager(calender: Calendar, task: Task) {
        val am = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmBroadcastReceiver::class.java)

        val sdf = SimpleDateFormat("yyyy/MM/dd",Locale.JAPAN)
        intent.putExtra(AlarmBroadcastReceiver.LOCAL_PUSH_CONTENT, task.content)
        intent.putExtra(AlarmBroadcastReceiver.LOCAL_PUSH_DEADLINE, sdf.format(task.deadline))
        calender.add(Calendar.HOUR_OF_DAY, 17)
        //TODO:今は保存５秒後に通知になっているが直す
        val c = Calendar.getInstance()
        c.add(Calendar.SECOND,5)

        val pending = PendingIntent.getBroadcast(this, 0, intent, 0)
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> {
//                val info = AlarmManager.AlarmClockInfo(calender.timeInMillis, null)
                val info = AlarmManager.AlarmClockInfo(c.timeInMillis, null)

                am.setAlarmClock(info, pending)
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT -> {
                am.setExact(AlarmManager.RTC_WAKEUP, calender.timeInMillis, pending)
            }
            else -> {
                am.setExact(AlarmManager.RTC_WAKEUP, calender.timeInMillis, pending)
            }
        }
    }

}
