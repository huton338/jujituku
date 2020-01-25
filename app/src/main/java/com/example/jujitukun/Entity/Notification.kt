package com.example.jujitukun.Entity

import io.realm.RealmObject
import io.realm.RealmResults
import io.realm.annotations.LinkingObjects
import io.realm.annotations.PrimaryKey
import java.util.*

open class Notification :RealmObject(){

    @PrimaryKey
    var id:Long = 0
    var notificationDate:Date = Date()
    //for one to many(this object is one)
    @LinkingObjects("notifications")
    val tasks:RealmResults<Task>? = null
    // 0:未完了, 1:完了 ,9:削除
    var status : Int = 0
}