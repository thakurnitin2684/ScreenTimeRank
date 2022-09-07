package com.thakurnitin2684.screentimerank.data

import android.os.Parcelable
import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(val name: String?,
                val email: String?,
                val url: String,
                val screenTime: Long,
                var rooms : ArrayList<String>) : Parcelable {

    companion object {
        fun DocumentSnapshot.toUser(): User? {
            return try {
                val name = getString("name")!!
                val email = getString("email")!!
                val url = getString("url")!!
                val screenTime = getLong("screenTime")!!
                val rooms = (get("rooms") as ArrayList<String>?)!!
                User( name, email, url, screenTime, rooms)
            } catch (e: Exception) {
                Log.e(TAG, "Error converting user profile", e)
//                FirebaseCrashlytics.getInstance().log("Error converting user profile")
//                FirebaseCrashlytics.getInstance().setCustomKey("userId", id)
//                FirebaseCrashlytics.getInstance().recordException(e)
                null
            }
        }
        private const val TAG = "User"
    }
}