package com.example.jujitukun.Entity

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

open class Task : RealmObject(){

    @PrimaryKey
    var id :Long = 0
    var content : String = ""
    var deadline : Date = Date()
    //for one to many(this object is many)
    var notifications :RealmList<Notification> = RealmList()
    // 0:未完了, 1:完了
    var status : Int = 0
}