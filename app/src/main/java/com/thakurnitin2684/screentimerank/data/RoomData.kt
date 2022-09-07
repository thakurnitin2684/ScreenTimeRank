package com.thakurnitin2684.screentimerank.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
@Parcelize
data class RoomData(
     var roomId: String = "0",
     var roomName: String = "Room",
     var rank: String = "0",
     var members: ArrayList<String> = ArrayList(),
    var membersName: ArrayList<String> = ArrayList(),
    var membersUrl: ArrayList<String> = ArrayList()
) : Parcelable
