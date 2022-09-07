package com.thakurnitin2684.screentimerank.ui

import com.google.android.gms.auth.api.signin.GoogleSignInAccount

interface AuthListener {
        fun onStarted()
        fun onSuccess(acct: GoogleSignInAccount)
        fun onFailure()
}