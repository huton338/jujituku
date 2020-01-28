package com.example.jujitukun.Activity

import android.content.Intent
import android.graphics.Canvas
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.jujitukun.Entity.Purpose
import com.example.jujitukun.Entity.Task
import com.example.jujitukun.R
import com.example.jujitukun.TaskRecycleAdapter
import com.example.jujitukun.SwipeController
import com.example.jujitukun.SwipeControllerActions
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.activity_main.*

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
        setPurposeOfLifeText(realm)

        //--------RyceclerView start----------
        // adapterにリスナーを設定
        val radapter = TaskRecycleAdapter(tasks)
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
                deleteTask(realm,viewAdapter.getItemId(position))
                //本来は下記の処理が必要だがOrderedRealmCollectionを使っているとOrderedRealmCollectionChangeListenerがうまくやってくれるらしい
//                viewAdapter.notifyItemRemoved(position)
//                viewAdapter.notifyItemRangeChanged(position,viewAdapter.itemCount)
            }
        })
        val itemTouchHelper = ItemTouchHelper(swipe)
        itemTouchHelper.attachToRecyclerView(recyclerView)
        recyclerView.addItemDecoration(object: RecyclerView.ItemDecoration(){
            override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
                swipe.onDraw(c)
            }
        })

        //--------RyceclerView end----------


        //＋ボタン
        saveButton.setOnClickListener {
            val intent = Intent(this, TaskEditActivity::class.java)
            startActivity(intent)
        }

        //人生の目的TextVIew
        purposeOfLifeText.setOnClickListener {
            val  intent = Intent(this,LifeOfPurposeActivity::class.java)
            startActivity(intent)
        }

        //カレンダーボタン
        calendarIconView.setOnClickListener {
            val intent = Intent(this,CalendarActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        setPurposeOfLifeText(realm)
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }

    /**
     * 人生の目的(purposeOfLifeText)設定.
     */
    private fun setPurposeOfLifeText(realm:Realm){
        selectOnePurpose(realm)?.let {
            purposeOfLifeText.setText(it.content)
        }
    }


     //-----realm access----------
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

    /**
     * Purposeの取得
     */
    private fun selectOnePurpose(realm: Realm): Purpose?{
        //最新
        var  status: Int = 0
        return realm.where<Purpose>().equalTo("status",status)?.findFirst()
    }
}
