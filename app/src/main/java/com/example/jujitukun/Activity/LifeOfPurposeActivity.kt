package com.example.jujitukun.Activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import com.example.jujitukun.Entity.Purpose
import com.example.jujitukun.R
import io.realm.Realm
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.activity_life_of_purpose.*
import kotlinx.android.synthetic.main.activity_main.*

class LifeOfPurposeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_life_of_purpose)

        val realm = Realm.getDefaultInstance()

        selectOnePurpose(realm)?.let {
            purposeOfTextView.setText(it.content,TextView.BufferType.NORMAL)
        }


        savePurposeButton.setOnClickListener {
            //nullチェック
            if(!purposeOfTextView.text.isNullOrBlank()){
                addPurpose(realm)
                Toast.makeText(this,"保存しました。",Toast.LENGTH_SHORT).show()
            }
        }

    }


    private fun addPurpose(realm: Realm){
        realm.executeTransaction { db :Realm ->
            var maxId = db.where<Purpose>().max("id")
            var nextId =(maxId?.toLong()?:0L) + 1

            //一つ前の目的を履歴へ
            var oldMaxPurpose = db.where<Purpose>().equalTo("id",maxId?.toLong() ?:0L)?.findFirst()

            //入力文字変更チェック
            if(!oldMaxPurpose?.content.equals(purposeOfTextView.text.toString())){
                //ステータス：履歴へ更新
                oldMaxPurpose?.let {
                    it.status = 1
                }
                //追加
                var purpose = db.createObject(Purpose::class.java, nextId)
                purpose.content = purposeOfTextView.text.toString()
                purpose.status = 0
            }
        }
    }


    private fun selectOnePurpose(realm: Realm):Purpose?{
        //最新
        var  status: Int = 0
        return realm.where<Purpose>().equalTo("status",status)?.findFirst()
    }
}
