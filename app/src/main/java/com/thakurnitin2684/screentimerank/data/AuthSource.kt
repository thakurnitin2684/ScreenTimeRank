package com.thakurnitin2684.screentimerank.data


import android.util.Log
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private const val TAG = "AuthSource"

object AuthSource {

    private val firebaseAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    suspend fun signIn(credential: AuthCredential): Boolean {
        return suspendCoroutine { continuation ->
            firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        continuation.resume(true)

                    } else {
                        continuation.resume(false)
                        Log.d(TAG, "signInWithCredential:failure", task.exception)
                    }
                }
        }
    }


    fun signOut() = firebaseAuth.signOut()

    fun currentUser() = firebaseAuth.currentUser

}