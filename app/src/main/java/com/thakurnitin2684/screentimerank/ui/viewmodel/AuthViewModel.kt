package com.thakurnitin2684.screentimerank.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseUser
import com.thakurnitin2684.screentimerank.data.AuthSource
import com.thakurnitin2684.screentimerank.ui.AuthListener
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class AuthViewModel  : ViewModel() {



    //auth listener
    var authListener: AuthListener? = null






    fun signIn(credential: AuthCredential,acct: GoogleSignInAccount){
        GlobalScope.launch {
          val result=  AuthSource.signIn(credential)
            if(result){
         authListener?.onSuccess(acct)
            }else{
          authListener?.onFailure()
            }
        }
    }

    fun signOut(){
        AuthSource.signOut()
    }
     fun getCurrentUser(): FirebaseUser? {
       return  AuthSource.currentUser()
     }
}