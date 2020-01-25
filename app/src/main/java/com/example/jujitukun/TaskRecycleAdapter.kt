package com.example.jujitukun

import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.jujitukun.Entity.Task
import io.realm.OrderedRealmCollection
import io.realm.RealmRecyclerViewAdapter

class TaskRecycleAdapter(task : OrderedRealmCollection<Task>):RealmRecyclerViewAdapter<Task,TaskRecycleAdapter.ViewHolder>(task,true){

    //Long型で戻り値なしのリスナー
    private var listener : ((Long?) -> Unit)? = null

    init {
        setHasStableIds(true)
    }

    //リスナーを設定
    fun setOnItemClickListenr(listener:((Long?) -> Unit)){
        this.listener = listener
    }

    //1行文のデータクラス
    class ViewHolder(cell: View): RecyclerView.ViewHolder(cell){
        val content : TextView = cell.findViewById(R.id.contentText)
        val deadLine : TextView = cell.findViewById(R.id.deadLineText)
//        val content : TextView = cell.findViewById(android.R.id.text1)
//        val deadLine : TextView = cell.findViewById(android.R.id.text2)
    }


    //表示用のViewHolder(表示項目)作成
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.task_row,parent,false)
//        val view = inflater.inflate(android.R.layout.simple_list_item_2,parent,false)
        return ViewHolder(view)
    }


    //TaskからViewHolder(表示項目)形式へ変換を行う
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val task : Task? = getItem(position)
        holder.content.text = task?.content
        holder.deadLine.text = DateFormat.format("yyyy/MM/dd",task?.deadline)
        holder.itemView.setOnClickListener {
            listener?.invoke(task?.id)
        }
    }

    override fun getItemId(position: Int): Long {
        return getItem(position)?.id ?:0
    }

}