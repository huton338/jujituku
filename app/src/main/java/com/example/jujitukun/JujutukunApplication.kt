package com.example.jujitukun

import android.app.Application
import android.content.Context
import com.jakewharton.threetenabp.AndroidThreeTen
import io.realm.Realm


class JujutukunApplication: Application() {


    //singletonインスタンス作成
    init {
        instance = this
    }

    companion object {
        private var instance: JujutukunApplication? = null

        fun applicationContext() : Context {
            return instance!!.applicationContext
        }
    }

    override fun onCreate() {
        super.onCreate()
        Realm.init(this)
        AndroidThreeTen.init(this)
    }

}