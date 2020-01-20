package com.example.jujitukun.Entity

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

open class Task : RealmObject(){

    @PrimaryKey
    var id :Long = 0
    var content : String = ""
    var deadline : Date = Date()
}