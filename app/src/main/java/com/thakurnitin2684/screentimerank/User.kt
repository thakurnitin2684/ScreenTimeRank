package com.thakurnitin2684.screentimerank

import kotlin.properties.Delegates

class User{
    lateinit var name:String
    lateinit var email:String
    lateinit var url:String
    var screenTime by Delegates.notNull<Long>()
    lateinit var rooms : ArrayList<String>

    constructor(){}
    constructor(name:String,email:String,url:String,screenTime:Long, rooms : ArrayList<String>){
        this.name = name
        this.email = email
        this.url=url
        this.screenTime=screenTime
        this.rooms=rooms
    }

}