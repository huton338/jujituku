package com.example.jujitukun

import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.jujitukun.Entity.Notification
import io.realm.OrderedRealmCollection
import io.realm.RealmRecyclerViewAdapter


class NotificationRecycleAdapter(notification: OrderedRealmCollection<Notification>) :
    RealmRecyclerViewAdapter<Notification, NotificationRecycleAdapter.ViewHolder>(
        notification,
        true
    ) {

    //Long型で戻り値なしのリスナー
    private var listener: ((Long?) -> Unit)? = null

    init {
        setHasStableIds(true)
    }

    //リスナーを設定
    fun setOnItemClickListenr(listener: ((Long?) -> Unit)) {
        this.listener = listener
    }

    //1行文のデータクラス
    class ViewHolder(cell: View) : RecyclerView.ViewHolder(cell) {
        val notificaitonDate: TextView = cell.findViewById(R.id.notificationText)
    }


    //表示用のViewHolder(表示項目)作成
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.notification_row, parent, false)
        return ViewHolder(view)
    }


    //NotificationからViewHolder(表示項目)形式へ変換を行う
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val notification: Notification? = getItem(position)
        holder.notificaitonDate.text =
            DateFormat.format("yyyy/MM/dd HH:mm", notification?.notificationDate)
        holder.itemView.setOnClickListener {
            listener?.invoke(notification?.id)
        }
    }


    override fun getItemId(position: Int): Long {
        return getItem(position)?.id ?: 0
    }
}
