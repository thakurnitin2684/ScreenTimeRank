package com.thakurnitin2684.screentimerank.data

import android.os.Parcelable
import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.parcelize.Parcelize




@Parcelize
data class Room( var members: ArrayList<String>,
                 var name: String,
                ) : Parcelable {

    companion object {
        fun DocumentSnapshot.toRoom(): Room? {
            return try {
                val members = (get("members") as ArrayList<String>?)!!
                val name = getString("name")!!
                Room( members, name)
            } catch (e: Exception) {
                Log.e(TAG, "Error converting room ", e)
//                FirebaseCrashlytics.getInstance().log("Error converting user profile")
//                FirebaseCrashlytics.getInstance().setCustomKey("userId", id)
//                FirebaseCrashlytics.getInstance().recordException(e)
                null
            }
        }
        private const val TAG = "Room"
    }
}