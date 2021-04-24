package com.thakurnitin2684.screentimerank

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.properties.Delegates

private const val TAG = "Database"

class Database {
    private lateinit var database: FirebaseFirestore
    fun open() {
        database = FirebaseFirestore.getInstance()
    }

    fun addDbForFirstTime(uId: String, name: String?, email: String?, imageUrl: String?) {

        val usersRef = database.collection("users").document(uId)

        usersRef.get()
            .addOnSuccessListener { document ->
                val data = hashMapOf(
                    "name" to name,
                    "email" to email,
                    "url" to imageUrl,
                    "screenTime" to totalTime2,
                    "rooms" to arrayListOf<String>()
                )
                if (document != null) {
                    if (document.get("email") == null) {
                        database.collection("users").document(uId).set(data)
                        Log.d(TAG, "old User1 $uId")


                    } else {
                        database.collection("users").document(uId).update("name", name)
                        database.collection("users").document(uId).update("email", email)
                        database.collection("users").document(uId).update("url", imageUrl)
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

    fun updateTime(userId: String?, time: Long) {
        database.collection("users").document(userId!!).update("screenTime", time)

    }




    fun deleteRoom(userId: String, roomId: String) {
        var size by Delegates.notNull<Int>()
        database.collection("rooms").document(roomId).get()
            .addOnSuccessListener { documentSnapshot ->
                val members = documentSnapshot.get("members") as ArrayList<String>
                size = members.size

                if (size == 1) {
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