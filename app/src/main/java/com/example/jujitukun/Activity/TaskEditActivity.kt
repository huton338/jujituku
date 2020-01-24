package com.example.jujitukun.Activity

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.jujitukun.DatePickerDialogFragment
import com.example.jujitukun.Entity.Task
import com.example.jujitukun.LocalPushBroadcastReceiver
import com.example.jujitukun.R
import com.example.jujitukun.TimePickerDialogFragment
import com.google.android.material.snackbar.Snackbar
import io.realm.Realm
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.activity_task_edit.*
import kotlinx.android.synthetic.main.notification_add_popup.view.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


class TaskEditActivity : AppCompatActivity(),
    DatePickerDialogFragment.OnDateSelectedListener,
    TimePickerDialogFragment.OnTimeSelectedListener {

    private lateinit var realm: Realm
    private lateinit var popupView: View
    private var datePickerId: Int = 0
    private var taskId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_edit)

        realm = Realm.getDefaultInstance()

        //前画面からtaskIdを取得
        taskId = intent.getLongExtra("task_id", -1L)
        if (taskId == -1L) taskId = null

        //更新の場合
        //select task
        var selectTask: Task? = taskId?.let { selectTask(it) }
        //入力項目埋める
        selectTask?.let {
            taskContetText.setText(it.content, TextView.BufferType.NORMAL)
            taskDateText.setText(
                DateFormat.format("yyyy/MM/dd", it.deadline), TextView.BufferType.NORMAL
            )

        }

        //保存
        saveTask.setOnClickListener { view: View ->
            if (taskId == null) {
                //新規
                addNewTask()

                Snackbar.make(view, "追加しました", Snackbar.LENGTH_SHORT)
                    .setAction("戻る") { finish() }
                    .setActionTextColor(Color.YELLOW)
                    .show()

                //ボタンを更新へ変更
                saveTask.setText(R.string.update_text)
            } else {
                //更新
                taskId?.let {
                    updTask(it)

                    Snackbar.make(view, "追加しました", Snackbar.LENGTH_SHORT)
                        .setAction("戻る") { finish() }
                        .setActionTextColor(Color.YELLOW)
                        .show()
                }
            }
        }

        //入力などを行うときはviewを指定しないとMainActivityのViewという解釈になるのでうまく行かない
        notificationAddButton.setOnClickListener {
            popupView = LayoutInflater.from(this).inflate(R.layout.notification_add_popup, null)
            val popupWindow = PopupWindow(popupView, 800, 600).apply {
                isOutsideTouchable = true
                isFocusable = true
            }
            popupWindow.showAsDropDown(notificationAddButton)
            if (taskDateText.text.isNotEmpty()) {
                var notificationDate = taskDateText.text.toString()
                popupView.notificationDateText.setText(notificationDate, TextView.BufferType.NORMAL)
            }

            //カレンダー
            popupView.calIcon.setOnClickListener {
                datePickerId = 1
                val dialog = DatePickerDialogFragment()
                dialog.show(supportFragmentManager, "dialog_date")
            }

            //時間
            popupView.clockIcon.setOnClickListener {
                val dialog = TimePickerDialogFragment()
                dialog.show(supportFragmentManager, "dialog_time")
            }

            //push通知設定依頼
            popupView.addNotificationButton.setOnClickListener {
                if (popupView.notificationDateText.text.isNotEmpty() && popupView.notificationTimeText.text.isNotEmpty()) {

                    var cal = textToCalendar(
                        popupView.notificationDateText.text.toString(),
                        popupView.notificationTimeText.text.toString()
                    )

                    //localpush通知依頼
                    taskId?.let {
                        selectTask(it)?.let {
                            setLocalPushManager(cal, it)
                        }
                    }

                    //popupwindow閉じる
                    popupWindow.dismiss()
                    Toast.makeText(this,"リマインドを追加しました。",Toast.LENGTH_SHORT).show()

                }
            }
        }

        //カレンダー入力
        calIcon.setOnClickListener {
            datePickerId = 0
            val dialog = DatePickerDialogFragment()
            dialog.show(supportFragmentManager, "dialog_date")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }


    //DatePickerDialogFlagmentのinterface実装
    override fun OnSelectedDate(year: Int, month: Int, date: Int) {
        val c = Calendar.getInstance()
        c.set(year, month, date)
        when (datePickerId) {
            //期限
            0 -> taskDateText.setText(
                DateFormat.format("yyyy/MM/dd", c),
                TextView.BufferType.NORMAL
            )
            //通知日時
            1 -> popupView.notificationDateText.setText(
                DateFormat.format("yyyy/MM/dd", c),
                TextView.BufferType.NORMAL
            )
        }
    }

    //TimePickerDialogFlagmentのinterface実装
    override fun onSelectedTime(hourOfDay: Int, minute: Int) {
        popupView.notificationTimeText.setText(
            "%1$02d:%2$02d".format(hourOfDay, minute),
            TextView.BufferType.NORMAL
        )
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
        val intent = Intent(this, LocalPushBroadcastReceiver::class.java)

        val sdf = SimpleDateFormat("yyyy/MM/dd", Locale.JAPAN)
        intent.putExtra(LocalPushBroadcastReceiver.LOCAL_PUSH_CONTENT, task.content)
        intent.putExtra(LocalPushBroadcastReceiver.LOCAL_PUSH_DEADLINE, sdf.format(task.deadline))

        val pending = PendingIntent.getBroadcast(this, 0, intent, 0)
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> {
                val info = AlarmManager.AlarmClockInfo(calender.timeInMillis, null)

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


    private fun inputTextClear() {
        //入力項目をクリア
        taskContetText.setText("", TextView.BufferType.NORMAL)
        taskDateText.setText("", TextView.BufferType.NORMAL)
    }

    //TODO:入力項目のバリデーション
    private fun addNewTask() {
        realm.executeTransaction { db: Realm ->
            val maxId = db.where<Task>().max("id")
            val nextId = (maxId?.toLong() ?: 0) + 1
            val task = db.createObject<Task>(nextId)
            val date = taskDateText.text.toString().toDate("yyyy/MM/dd")
            if (date != null) task.deadline = date

            //LocalPush通知設定
            val c = Calendar.getInstance()
            c.time = date
            setLocalPushManager(c, task)
            //保持しているタスクIDを更新
            taskId = nextId
        }
    }

    //TODO:入力項目のバリデーション
    private fun updTask(taskId: Long) {
        realm.executeTransaction { db: Realm ->

            var task = db.where<Task>().equalTo("id", taskId)?.findFirst()
            task?.content = taskContetText.text.toString()
            val date = taskDateText.text.toString().toDate("yyyy/MM/dd")
            if (date != null) task?.deadline = date
        }
    }

    private fun selectTask(taskId: Long): Task? {
        val query = realm.where(Task::class.java).equalTo("id", taskId)
        return query.findFirst()
    }

    private fun textToCalendar(dateStr: String, timeStr: String): Calendar {

        var year = dateStr.substring(0, 4).toInt()
        //0が1月のため-1している
        var month = dateStr.substring(5, 7).toInt() - 1
        var date = dateStr.substring(8, 10).toInt()
        var hour = timeStr.substring(0, 2).toInt()
        var minute = timeStr.substring(3, 5).toInt()

        var cal = Calendar.getInstance()
        cal.set(year, month, date, hour, minute, 0)

        return cal
    }
}
