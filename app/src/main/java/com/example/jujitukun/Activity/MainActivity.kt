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

        //realm select
        realm = Realm.getDefaultInstance()
        val tasks = realm.where<Task>().findAll()

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
                //TODO:Realmを更新して完了ステータスへ
                viewAdapter.notifyItemRemoved(position)
                viewAdapter.notifyItemRangeChanged(position,viewAdapter.itemCount)
       }
            override fun onRightClicked(position: Int) {
                //TODO:Realmから消す作業
                viewAdapter.notifyItemRemoved(position)
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
}
