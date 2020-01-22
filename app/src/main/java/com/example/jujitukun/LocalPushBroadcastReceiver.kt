package com.example.jujitukun

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

/**
 * ローカルPUSH通知クラス.
 */
class LocalPushBroadcastReceiver : BroadcastReceiver() {

    //定数
    companion object { //singleton
        const val LOCAL_PUSH_CONTENT = "local_push_content"
        const val LOCAL_PUSH_DEADLINE = "local_push_deadline"

    }

    override fun onReceive(context: Context, intent: Intent) {

        var content = intent.getStringExtra(LOCAL_PUSH_CONTENT)
        var deadline = intent.getStringExtra(LOCAL_PUSH_DEADLINE)

        //android8 以上対応(チャンネル作成)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel =
                NotificationChannel("default", "Default", NotificationManager.IMPORTANCE_HIGH)
            channel.description = ("Default Channel")
            notificationManager.createNotificationChannel(channel)
        }

        //通知情報設定
        val notification = NotificationCompat.Builder(context, "default")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("タスクの期限のお知らせ")
            .setContentText("${content}の期限は ${deadline}までです。")
            //タップで通知領域から削除
            .setAutoCancel(true)
            .build()

        //通知
        val manager = NotificationManagerCompat.from(context)
        manager.notify(0, notification)
    }


}
