package com.thakurnitin2684.screentimerank.data

import android.util.Log
import androidx.sqlite.db.SupportSQLiteCompat.Api16Impl.cancel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.thakurnitin2684.screentimerank.data.Room.Companion.toRoom
import com.thakurnitin2684.screentimerank.data.User.Companion.toUser
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlin.properties.Delegates

object FirebaseProfileService {
    private const val TAG = "FirebaseProfileService"


    private val database: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }


    suspend fun getUserDetails(userId: String): User? {
        return try {
            database.collection("users")
                .document(userId).get().await().toUser()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user details", e)
//            FirebaseCrashlytics.getInstance().log("Error getting user details")
//            FirebaseCrashlytics.getInstance().setCustomKey("user id", xpertSlug)
//            FirebaseCrashlytics.getInstance().recordException(e)
            null
        }
    }


    suspend fun getRoomDetails(roomId: String): Room? {
        return try {
            database.collection("rooms")
                .document(roomId).get().await().toRoom()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user details", e)
//            FirebaseCrashlytics.getInstance().log("Error getting user details")
//            FirebaseCrashlytics.getInstance().setCustomKey("user id", xpertSlug)
//            FirebaseCrashlytics.getInstance().recordException(e)
            null
        }
    }


     fun addDbForFirstTime(uId: String, user: User) {

        val usersRef = database.collection("users").document(uId)

        usersRef.get()
            .addOnSuccessListener { document ->
                val data = hashMapOf(
                    "name" to user.name,
                    "email" to user.email,
                    "url" to user.email,
                    "screenTime" to user.screenTime,
                    "rooms" to user.rooms
                )
                if (document != null) {
                    if (document.get("email") == null) {
                        database.collection("users").document(uId).set(data)
                        Log.d(TAG, "old User1 $uId")


                    } else {
                        database.collection("users").document(uId).update("name", user.name)
                        database.collection("users").document(uId).update("email", user.email)
                        database.collection("users").document(uId).update("url", user.url)
                        Log.d(TAG, "old User2 $uId")

                    }

                } else {
                    database.collection("users").document(uId).set(data)

                    Log.d(TAG, "New User")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }

    }

     fun createNewRoom(uId: String, name: String) {
        val data = hashMapOf(
            "name" to name,
            "members" to arrayListOf(uId)
        )
        database.collection("rooms").add(data)
            .addOnSuccessListener { documentReference ->
                database.collection("users").document(uId)
                    .update("rooms", FieldValue.arrayUnion(documentReference.id))
                Log.d(TAG, "DocumentSnapshot written with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.d(TAG, "Error adding document", e)
            }
    }


     fun addForRoom(userId: String?, roomId: String) {


        database.collection("users").document(userId!!)
            .update("rooms", FieldValue.arrayUnion(roomId))
        database.collection("rooms").document(roomId)
            .update("members", FieldValue.arrayUnion(userId))

    }

    suspend fun updateTime(userId: String?, time: Long) {
        val database = FirebaseFirestore.getInstance()
        database.collection("users").document(userId!!).update("screenTime", time).await()

    }


     fun deleteRoom(userId: String, roomId: String) {
        database.collection("rooms").document(roomId).get()
            .addOnSuccessListener { documentSnapshot ->
                val members = documentSnapshot.get("members") as ArrayList<String>

                if (members.size == 1) {
                    database.collection("users").document(userId)
                        .update("rooms", FieldValue.arrayRemove(roomId))
                    database.collection("rooms").document(roomId).delete()
                } else {
                    database.collection("rooms").document(roomId)
                        .update("members", FieldValue.arrayRemove(userId))
                    database.collection("users").document(userId)
                        .update("rooms", FieldValue.arrayRemove(roomId))
                }
            }
    }

}