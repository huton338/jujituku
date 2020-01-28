package com.example.jujitukun.Entity

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Purpose :RealmObject(){

    @PrimaryKey
    var id :Long = 0
    //目的内容
    var content : String = ""
    //　0:最新 1:履歴
    var status : Int = 0

    //TODO:共通項目のupdate/createdで時間を確認できるの

}