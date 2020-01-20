package com.example.jujitukun

import android.app.Application
import io.realm.Realm

class JujutukunApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        Realm.init(this)
    }
}