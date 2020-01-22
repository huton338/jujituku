package com.example.jujitukun.Activity

import android.content.Intent
import android.graphics.Canvas
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.jujitukun.Entity.Task
import com.example.jujitukun.R
import com.example.jujitukun.RecycleAdapter
import com.example.jujitukun.SwipeController
import com.example.jujitukun.SwipeControllerActions
import com.google.android.material.snackbar.Snackbar
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var  realm: Realm
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter : RecyclerView.Adapter<*>
    private lateinit var viewManager : RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        realm = Realm.getDefaultInstance()
        //realm select
        val tasks = selectAll(realm)

        // adapterにリスナーを設定
        val radapter = RecycleAdapter(tasks)
        radapter.setOnItemClickListenr {
            val intent = Intent(this, TaskEditActivity::class.java)
                .putExtra("task_id",it)
            startActivity(intent)
        }

        //RecycleView設定
        viewManager = LinearLayoutManager(this)
        viewAdapter = radapter


        //findbyで取得後設定を行わなくても recycleview名.xxx で設定できる
        recyclerView = findViewById<RecyclerView>(R.id.my_recycle_view).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }

        //Recycleviewをswipe及びtouchした時の設定
        val swipe = SwipeController(object : SwipeControllerActions(){
            //swipe後、ボタンのリスナー
            override fun onLeftClicked(position: Int) {
                updateTaskDone(realm,viewAdapter.getItemId(position))
//                viewAdapter.notifyItemRemoved(position)
//                viewAdapter.notifyItemRangeChanged(position,viewAdapter.itemCount)
       }
            override fun onRightClicked(position: Int) {
                //TODO:Realmから消す作業
                deleteTask(realm,viewAdapter.getItemId(position))
                //本来は下記の処理が必要だがOrderedRealmCollectionを使っているとOrderedRealmCollectionChangeListenerがうまくやってくれるらしい
//                viewAdapter.notifyItemRemoved(position)
                viewAdapter.notifyItemRangeChanged(position,viewAdapter.itemCount)
            }
        })
        val itemTouchHelper = ItemTouchHelper(swipe)
        itemTouchHelper.attachToRecyclerView(recyclerView)
        recyclerView.addItemDecoration(object: RecyclerView.ItemDecoration(){
            override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
                swipe.onDraw(c)
            }
        })

        addButton.setOnClickListener {
            val intent = Intent(this, TaskEditActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }


    /**
     * Task（ステータス：完了）全件取得.
     */
    private fun selectAll(realm: Realm):RealmResults<Task>{
        //ソートしておかないと変更を行った再描画の際のソート順序が変わってしまうためおかしくなる。
        //https://qiita.com/konatsu_p/items/5ab92ebce46c9479876b
        val status :Int = 0
        return realm.where<Task>().equalTo("status",status).findAll().sort("id", Sort.ASCENDING)
    }

    /**
     * Taskを削除.
     */
    private fun deleteTask(realm: Realm,taskId: Long){
        realm.executeTransaction { db:Realm ->
             db.where<Task>().equalTo("id",taskId)?.findFirst()?.deleteFromRealm()
        }
    }

    /**
     * Taskのステータスを完了へ更新.
     */
    private fun updateTaskDone(realm: Realm,taskId: Long){
        realm.executeTransaction { db:Realm ->
            var updTask = db.where<Task>().equalTo("id",taskId)?.findFirst()
            updTask?.status = 1
        }
    }
}
